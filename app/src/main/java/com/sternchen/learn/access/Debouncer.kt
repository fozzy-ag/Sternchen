package com.sternchen.learn.access

/**
 * Debounces rapid input events.
 *
 * Accommodates ataxia/tremor: accidental double/rapid presses are ignored.
 * Only the first press within [debounceMillis] is accepted; subsequent presses
 * within the window are swallowed. Used for touch and switch input.
 */
class Debouncer(private val debounceMillis: Long) {

    private var lastAcceptNanos = 0L

    /** Returns true if this press should be accepted (not swallowed). */
    fun shouldAccept(): Boolean {
        val now = System.nanoTime()
        if (now - lastAcceptNanos < debounceMillis * 1_000_000L) {
            return false
        }
        lastAcceptNanos = now
        return true
    }

    companion object {
        /** No debounce. */
        fun none() = Debouncer(0L)
    }
}
