package com.sternchen.learn.config

import android.content.Context
import android.content.SharedPreferences

/**
 * Offline-first persistence for [LearnerProfile].
 *
 * Uses SharedPreferences so the app is fully functional with no network and no
 * external services. Profiles are local-only. This is intentionally a thin,
 * replaceable layer — it can be swapped for Room/sqlite later for multi-profile
 * management and caregiver analytics without touching UI code.
 *
 * Takes a [SharedPreferences] rather than a [Context] so the round-trip can be
 * exercised by a plain JVM unit test with an in-memory fake. Every field of
 * [LearnerProfile] MUST be written in [save] and read in [load];
 * [ProfileStoreTest] fails if a field is added to one and forgotten in the other.
 */
class ProfileStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    fun load(): LearnerProfile {
        val d = LearnerProfile()
        return LearnerProfile(
            name = prefs.getString(KEY_NAME, d.name) ?: d.name,
            language = runCatching {
                AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, d.language.name)!!)
            }.getOrDefault(d.language),
            preferredColorArgb = prefs.getLong(KEY_COLOR, d.preferredColorArgb),
            plainBackground = prefs.getBoolean(KEY_PLAIN, d.plainBackground),
            backgroundArgb = prefs.getLong(KEY_BG, d.backgroundArgb),
            objectScale = prefs.getFloat(KEY_SCALE, d.objectScale),
            simpleLayout = prefs.getBoolean(KEY_SIMPLE, d.simpleLayout),
            reducedMotion = prefs.getBoolean(KEY_MOTION, d.reducedMotion),
            inputMode = runCatching {
                InputMode.valueOf(prefs.getString(KEY_INPUT, d.inputMode.name)!!)
            }.getOrDefault(d.inputMode),
            debounceMillis = prefs.getLong(KEY_DEBOUNCE, d.debounceMillis),
            scanDwellMillis = prefs.getLong(KEY_SCAN_DWELL, d.scanDwellMillis),
            latencyMillis = prefs.getLong(KEY_LATENCY, d.latencyMillis),
            stimulusRepetitions = prefs.getInt(KEY_REPS, d.stimulusRepetitions),
            countingMax = prefs.getInt(KEY_COUNTING_MAX, d.countingMax),
            explicitInstruction = prefs.getBoolean(KEY_EXPLICIT, d.explicitInstruction),
            audioNarration = prefs.getBoolean(KEY_AUDIO, d.audioNarration),
            audioOnlyMode = prefs.getBoolean(KEY_AUDIO_ONLY, d.audioOnlyMode),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, d.hapticFeedback),
        )
    }

    fun save(profile: LearnerProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_LANGUAGE, profile.language.name)
            .putLong(KEY_COLOR, profile.preferredColorArgb)
            .putBoolean(KEY_PLAIN, profile.plainBackground)
            .putLong(KEY_BG, profile.backgroundArgb)
            .putFloat(KEY_SCALE, profile.objectScale)
            .putBoolean(KEY_SIMPLE, profile.simpleLayout)
            .putBoolean(KEY_MOTION, profile.reducedMotion)
            .putString(KEY_INPUT, profile.inputMode.name)
            .putLong(KEY_DEBOUNCE, profile.debounceMillis)
            .putLong(KEY_SCAN_DWELL, profile.scanDwellMillis)
            .putLong(KEY_LATENCY, profile.latencyMillis)
            .putInt(KEY_REPS, profile.stimulusRepetitions)
            .putInt(KEY_COUNTING_MAX, profile.countingMax)
            .putBoolean(KEY_EXPLICIT, profile.explicitInstruction)
            .putBoolean(KEY_AUDIO, profile.audioNarration)
            .putBoolean(KEY_AUDIO_ONLY, profile.audioOnlyMode)
            .putBoolean(KEY_HAPTIC, profile.hapticFeedback)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "pigt_learn_profiles"
        const val KEY_NAME = "name"
        const val KEY_LANGUAGE = "language"
        const val KEY_COLOR = "preferred_color"
        const val KEY_PLAIN = "plain_bg"
        const val KEY_BG = "bg_color"
        const val KEY_SCALE = "object_scale"
        const val KEY_SIMPLE = "simple_layout"
        const val KEY_MOTION = "reduced_motion"
        const val KEY_INPUT = "input_mode"
        const val KEY_DEBOUNCE = "debounce"
        const val KEY_SCAN_DWELL = "scan_dwell"
        const val KEY_LATENCY = "latency"
        const val KEY_REPS = "reps"
        const val KEY_COUNTING_MAX = "counting_max"
        const val KEY_EXPLICIT = "explicit"
        const val KEY_AUDIO = "audio"
        const val KEY_AUDIO_ONLY = "audio_only"
        const val KEY_HAPTIC = "haptic"
    }
}
