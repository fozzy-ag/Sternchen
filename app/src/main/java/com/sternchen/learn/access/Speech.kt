package com.sternchen.learn.access

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

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
 * Queue policy: each new utterance FLUSHES the engine, so stale audio can never
 * pile up and keep playing after the round/screen has moved on. Callers that
 * need the reward to finish before the next prompt use [sayAndWait], which
 * suspends until the utterance completes (via the TTS completion listener).
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
                registerCompletionListener()
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

    // Callbacks keyed by utterance id, invoked when that utterance finishes.
    // Used to resume [sayAndWait] coroutines. Thread-safe for TTS callbacks.
    private val utteranceCallbacks = ConcurrentHashMap<String, () -> Unit>()

    private val defaultLocale: Locale = Locale.getDefault()

    /** True once the TTS engine is initialised and speaking. */
    val isReady: Boolean get() = ready && !ttsError

    /** Set the narration voice language to match the app's UI language. */
    fun setLanguage(locale: Locale) {
        pendingLocale = locale
        if (ready) applyLanguage(locale)
    }

    /** Speak a phrase (flushing any current/queued audio); if TTS cannot speak,
     *  plays a tone so there is sound. */
    fun say(text: String) {
        if (engine != null && ready && !ttsError) {
            if (text.isNotBlank()) speak(text)
        } else if (engine != null && !ready && !ttsError) {
            pending.add(text)
        } else {
            playTone()
        }
    }

    /**
     * Speak [text], flushing current audio, and suspend until that utterance's
     * onDone fires (or a safety timeout / engine unavailable cancels the wait).
     * Returns true if the utterance actually completed via TTS.
     */
    suspend fun sayAndWait(text: String): Boolean {
        val t = engine ?: run { if (text.isNotBlank()) playTone(); return false }
        if (!ready || ttsError) {
            // TTS not usable: fall back to a tone and do not block the caller.
            if (text.isNotBlank()) playTone()
            return false
        }
        return withTimeoutOrNull(8000L) {
            suspendCancellableCoroutine { cont ->
                val id = "sternchen_${System.nanoTime()}"
                utteranceCallbacks[id] = { cont.resume(true) }
                cont.invokeOnCancellation {
                    utteranceCallbacks.remove(id)
                }
                runCatching {
                    t.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
                }.onFailure {
                    utteranceCallbacks.remove(id)
                    cont.resume(false)
                }
            }
        } ?: false
    }

    /** Always play a short audible blip (reliable reward/feedback cue). */
    fun tone() {
        playTone()
    }

    private fun registerCompletionListener() {
        val t = engine ?: return
        try {
            t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    utteranceId?.let { id ->
                        utteranceCallbacks.remove(id)?.invoke()
                    }
                }
                @Deprecated("Deprecated by TTS for API < 21; still delivered on some engines.")
                override fun onError(utteranceId: String?) {
                    utteranceId?.let { id ->
                        utteranceCallbacks.remove(id)?.invoke()
                    }
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    utteranceId?.let { id ->
                        utteranceCallbacks.remove(id)?.invoke()
                    }
                }
            })
        } catch (_: Exception) {
        }
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

    /** Speak with a flush: replace any current/queued audio, tagged with an id. */
    private fun speak(text: String) {
        val t = engine ?: return
        try {
            val id = "sternchen_${System.nanoTime()}"
            t.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
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

    /** Stop any ongoing speech and drop pending completion callbacks. */
    fun stop() {
        utteranceCallbacks.forEach { (id, cb) -> utteranceCallbacks.remove(id) }
        if (ready) {
            try { engine?.stop() } catch (_: Exception) { }
        }
    }

    fun shutdown() {
        stop()
        if (ready) {
            try { engine?.shutdown() } catch (_: Exception) { }
        }
        try { toneGenerator?.release() } catch (_: Exception) { }
    }
}
