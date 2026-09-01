package com.sternchen.learn.config

import android.content.Context

/**
 * Offline-first persistence for [LearnerProfile].
 *
 * Uses SharedPreferences so the app is fully functional with no network and no
 * external services. Profiles are local-only. This is intentionally a thin,
 * replaceable layer — it can be swapped for Room/sqlite later for multi-profile
 * management and caregiver analytics without touching UI code.
 */
class ProfileStore(context: Context) {

    private val prefs = context.getSharedPreferences("pigt_learn_profiles", Context.MODE_PRIVATE)

    fun load(): LearnerProfile {
        return LearnerProfile(
            name = prefs.getString(KEY_NAME, "Learner") ?: "Learner",
            language = runCatching {
                AppLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AppLanguage.GERMAN.name)!!)
            }.getOrDefault(AppLanguage.GERMAN),
            preferredColorArgb = prefs.getLong(KEY_COLOR, LearnerProfile().preferredColorArgb),
            plainBackground = prefs.getBoolean(KEY_PLAIN, true),
            backgroundArgb = prefs.getLong(KEY_BG, LearnerProfile().backgroundArgb),
            objectScale = prefs.getFloat(KEY_SCALE, 1f),
            simpleLayout = prefs.getBoolean(KEY_SIMPLE, true),
            reducedMotion = prefs.getBoolean(KEY_MOTION, true),
            inputMode = runCatching {
                InputMode.valueOf(prefs.getString(KEY_INPUT, InputMode.TOUCH.name)!!)
            }.getOrDefault(InputMode.TOUCH),
            debounceMillis = prefs.getLong(KEY_DEBOUNCE, 400L),
            scanDwellMillis = prefs.getLong(KEY_SCAN_DWELL, 2500L),
            latencyMillis = prefs.getLong(KEY_LATENCY, 3000L),
            stimulusRepetitions = prefs.getInt(KEY_REPS, 3),
            countingMax = prefs.getInt(KEY_COUNTING_MAX, 5),
            explicitInstruction = prefs.getBoolean(KEY_EXPLICIT, true),
            audioNarration = prefs.getBoolean(KEY_AUDIO, true),
            audioOnlyMode = prefs.getBoolean(KEY_AUDIO_ONLY, false),
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
            .apply()
    }

    private companion object {
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
    }
}
