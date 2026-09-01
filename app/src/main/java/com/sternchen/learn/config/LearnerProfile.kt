package com.sternchen.learn.config

import androidx.compose.ui.graphics.Color

/**
 * Per-learner accessibility profile.
 *
 * Based on the research-informed requirements for children with PIGT-related
 * cerebellar underdevelopment (MCAHS3) and, importantly, the high prevalence of
 * cortical/cerebral visual impairment (CVI), seizure sensitivity (epilepsy),
 * hypotonia/ataxia (motor access needs), and slow information processing.
 */
data class LearnerProfile(
    val name: String = "Learner",

    /** App display/narration language. Defaults to German, the child's primary
     *  language, independent of the device locale. English is available as an
     *  optional caregiver choice. */
    val language: AppLanguage = AppLanguage.GERMAN,

    // ---- Vision (CVI-focused) ----
    /** The child's preferred/salient color. Objects are rendered in this color. */
    val preferredColorArgb: Long = 0xFFE53935.toLong(), // strong saturated red default
    /** Plain, low-complexity background (reduces visual clutter). */
    val plainBackground: Boolean = true,
    /** Background color for content screens (plain/dark or neutral). */
    val backgroundArgb: Long = 0xFF000000.toLong(), // near-black default to reduce glare
    /** Larger objects / higher contrast for lower visual function. */
    val objectScale: Float = 1f, // 1.0 = large, used for magnified targets
    /** Very simple single-focus layout (one salient item per screen). */
    val simpleLayout: Boolean = true,

    // ---- Seizure / photosensitivity safety ----
    /** Disable all flashing, blinking and moving content (WCAG 2.3 compliance). */
    val reducedMotion: Boolean = true,

    // ---- Motor / ataxia access ----
    /** How the child provides input: Direct touch vs switch scanning. */
    val inputMode: InputMode = InputMode.TOUCH,
    /** Extra delay added to ignore accidental rapid repeat presses (ataxia/tremor). */
    val debounceMillis: Long = 400L,
    /** Slower scanning dwell for switch users. */
    val scanDwellMillis: Long = 2500L,

    // ---- Cognitive / processing ----
    /** Extra wait-time given to respond before advancing (slow processing). */
    val latencyMillis: Long = 3000L,
    /** Repeat a target prompt this many times before moving on (implicit/procedural support). */
    val stimulusRepetitions: Int = 3,
    /** Maximum number shown in the counting level (3, 5 or 10). */
    val countingMax: Int = 5,
    /** Explicit (vs discovery) instruction — always on for this population. */
    val explicitInstruction: Boolean = true,

    // ---- Audio / AAC ----
    /** Narration / TTS voice on for content and feedback. */
    val audioNarration: Boolean = true,
    /** Pure audio-only mode (CVI/visual fatigue fallback). */
    val audioOnlyMode: Boolean = false,

    // ---- Feedback ----
    /** Gentle vibration on WRONG responses (correct = visual/audio reward only). */
    val hapticFeedback: Boolean = true,
)

enum class InputMode {
    TOUCH,
    SWITCH_SCAN,
}

/** The app's display/narration language (independent of the device locale). */
enum class AppLanguage {
    GERMAN,
    ENGLISH,
}

/** Derived theming values used across the app. */
val LearnerProfile.preferredColor: Color
    get() = Color(preferredColorArgb)

val LearnerProfile.backgroundColor: Color
    get() = Color(backgroundArgb)
