package com.sternchen.learn.config

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Guards the one bug class that silently affects the child: a [LearnerProfile]
 * field that is edited in Setup, read at runtime, but never persisted — so the
 * caregiver's choice reverts to the default on the next cold start.
 *
 * [profileWithEveryFieldSetToANonDefault] deliberately sets EVERY field to a value
 * that differs from its default. A field that is missing from save()/load() makes
 * this test fail, and keeps it failing as fields are added later.
 */
class ProfileStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = ProfileStore(prefs)

    @Test
    fun `every profile field survives a save and load round trip`() {
        store.save(profileWithEveryFieldSetToANonDefault())
        assertEquals(profileWithEveryFieldSetToANonDefault(), store.load())
    }

    @Test
    fun `haptic feedback stays disabled across a restart`() {
        store.save(profileWithEveryFieldSetToANonDefault().copy(hapticFeedback = false))
        assertFalse(store.load().hapticFeedback)
    }

    @Test
    fun `load without any saved data returns the defaults`() {
        assertEquals(LearnerProfile(), store.load())
    }

    @Test
    fun `an unknown persisted language or input mode falls back to the default`() {
        prefs.putString("language", "KLINGON")
        prefs.putString("input_mode", "TELEPATHY")
        val loaded = store.load()
        assertEquals(LearnerProfile().language, loaded.language)
        assertEquals(LearnerProfile().inputMode, loaded.inputMode)
    }

    private fun profileWithEveryFieldSetToANonDefault() = LearnerProfile(
        name = "Nicht der Standardname",
        language = AppLanguage.ENGLISH,
        preferredColorArgb = 0xFF00FF00L,
        plainBackground = false,
        backgroundArgb = 0xFF123456L,
        objectScale = 1.4f,
        simpleLayout = false,
        reducedMotion = false,
        inputMode = InputMode.SWITCH_SCAN,
        debounceMillis = 1234L,
        scanDwellMillis = 3456L,
        latencyMillis = 4567L,
        stimulusRepetitions = 7,
        countingMax = 10,
        explicitInstruction = false,
        audioNarration = false,
        audioOnlyMode = true,
        hapticFeedback = false,
    )
}

/** Minimal in-memory [SharedPreferences] so [ProfileStore] is testable on the JVM. */
private class FakeSharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = values.toMutableMap()

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (values[key] as? Set<String>)?.toMutableSet() ?: defValues

    override fun getString(key: String?, defValue: String?): String? =
        values[key] as? String ?: defValue

    override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        values[key] as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = values.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    fun putString(key: String, value: String) {
        values[key] = value
    }

    private inner class FakeEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()

        override fun putString(key: String?, value: String?) = apply { pending[key!!] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?) =
            apply { pending[key!!] = values }

        override fun putInt(key: String?, value: Int) = apply { pending[key!!] = value }
        override fun putLong(key: String?, value: Long) = apply { pending[key!!] = value }
        override fun putFloat(key: String?, value: Float) = apply { pending[key!!] = value }
        override fun putBoolean(key: String?, value: Boolean) = apply { pending[key!!] = value }

        override fun remove(key: String?) = apply { pending.remove(key) }
        override fun clear() = apply { pending.clear(); this@FakeSharedPreferences.values.clear() }
        override fun commit(): Boolean {
            values.putAll(pending)
            return true
        }

        override fun apply() {
            commit()
        }
    }
}
