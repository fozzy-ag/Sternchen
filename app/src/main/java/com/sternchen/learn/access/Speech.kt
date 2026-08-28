package com.sternchen.learn.access

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Audio narration/feedback helper.
 *
 * Provides spoken narration via TextToSpeech when an engine/voice is available,
 * and guarantees SOME audible output via a [ToneGenerator] blip whenever TTS is
 * not yet ready or cannot speak (e.g. no engine installed / voice missing).
 *
 * Handles the failure modes that would otherwise produce total silence:
 *  - narration before TTS init completes is buffered and flushed once ready;
 *  - if TTS never becomes ready, each narration plays a tone instead, so there
 *    is always sound.
 *
 * IMPORTANT: construction must never throw, even on devices with a broken or
 * missing TTS engine or an unavailable audio routing. Both heavy services are
 * created lazily behind [runCatching] so a boot crash is impossible.
 */
class Speech(context: Context) {

    // TextToSpeech creation can throw on devices where the TTS service fails to
    // bind. Created guarded so a broken/missing engine never crashes the app.
    private val tts: TextToSpeech? = runCatching {
        TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ready = true
                pendingLocale?.let { applyLanguage(it) }
                pending.forEach { speak(it) }
                pending.clear()
            } else {
                // Engine unavailable or failed: fall back to tones.
                ttsError = true
                pending.forEach { playTone() }
                pending.clear()
            }
        }
    }.getOrNull()

    private val engine: TextToSpeech? = tts

    // ToneGenerator creation can throw (e.g. audio routing unavailable), so it is
    // created lazily, guarded, and treated as optional. Never allowed to crash boot.
    private val toneGenerator: ToneGenerator? = runCatching {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    }.getOrNull()

    private var ready = false
    private var ttsError = false
    private val pending = mutableListOf<String>()
    private var pendingLocale: Locale? = null

    private val defaultLocale: Locale = Locale.getDefault()

    /** True once the TTS engine is initialised and speaking. */
    val isReady: Boolean get() = ready && !ttsError

    /** Set the narration voice language to match the app's UI language. */
    fun setLanguage(locale: Locale) {
        pendingLocale = locale
        if (ready) applyLanguage(locale)
    }

    /** Speak a phrase; if TTS cannot speak, play a tone so there is sound. */
    fun say(text: String) {
        if (engine != null && ready && !ttsError) {
            if (text.isNotBlank()) speak(text)
        } else if (engine != null && !ready && !ttsError) {
            pending.add(text)
        } else {
            playTone()
        }
    }

    /** Always play a short audible blip (reliable reward/feedback cue). */
    fun tone() {
        playTone()
    }

    private fun applyLanguage(locale: Locale) {
        val t = engine ?: return
        try {
            val result = t.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Requested voice not installed; fall back so speech still works.
                t.setLanguage(if (defaultLocale.language == locale.language) Locale.US else defaultLocale)
            }
        } catch (_: Exception) {
        }
    }

    private fun speak(text: String) {
        val t = engine ?: return
        try {
            t.speak(text, TextToSpeech.QUEUE_ADD, null, "sternchen_${System.nanoTime()}")
        } catch (_: Exception) {
            playTone()
        }
    }

    private fun playTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 220)
        } catch (_: Exception) {
            // ToneGenerator can also fail; nothing more we can do silently.
        }
    }

    /** Stop any ongoing speech. */
    fun stop() {
        if (ready) {
            try { engine?.stop() } catch (_: Exception) { }
        }
    }

    fun shutdown() {
        if (ready) {
            try { engine?.stop(); engine?.shutdown() } catch (_: Exception) { }
        }
        try { toneGenerator?.release() } catch (_: Exception) { }
    }
}
