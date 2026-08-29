package com.sternchen.learn.access

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback helper.
 *
 * Provides a short, gentle vibration on WRONG responses. Research on feedback
 * for learners with additional needs leans toward NOT haptic-reinforcing correct
 * responses (they already receive a strong visual + audio reward, and over-
 * rewarding desensitises the cue); instead a soft vibration on wrong answers
 * helps children who may miss or be delayed on the visual/audio error signal.
 *
 * Like [Speech], construction is fully guarded so a device without a vibrator
 * can never crash boot.
 */
class Haptics(context: Context) {

    private val vibrator: Vibrator? = runCatching {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }.getOrNull()

    /** True if a vibrator is available and usable. */
    val isAvailable: Boolean
        get() = vibrator?.hasVibrator() == true

    /** Play a short, low-amplitude vibration (used for wrong responses). */
    fun vibrateOnWrong() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(70)
            }
        } catch (_: Exception) {
            // Vibrator can fail; nothing more we can do silently.
        }
    }

    /** Cancel any ongoing vibration. */
    fun cancel() {
        val v = vibrator ?: return
        try { v.cancel() } catch (_: Exception) { }
    }
}
