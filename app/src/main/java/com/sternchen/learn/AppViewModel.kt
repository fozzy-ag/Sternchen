package com.sternchen.learn

import androidx.lifecycle.ViewModel
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.ProfileStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Top-level destinations. */
enum class Screen {
    SETUP, HOME, CAUSE_EFFECT, OBJECT_NAMING, MATCHING, COLORS_SHAPES,
    COUNTING, FIND_BY_NAME, SAME_DIFFERENT, LETTERS,
}

/**
 * App-level state holder: owns the active [LearnerProfile] and navigation.
 *
 * The profile is loaded from the offline [ProfileStore] at startup and updated
 * whenever the caregiver adjusts settings on the setup screen. All levels read
 * from this single source of truth.
 */
class AppViewModel(private val store: ProfileStore) : ViewModel() {

    private val _profile = MutableStateFlow(store.load())
    val profile: StateFlow<LearnerProfile> = _profile.asStateFlow()

    private val _screen = MutableStateFlow(Screen.SETUP)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    fun updateProfile(updated: LearnerProfile) {
        _profile.value = updated
        store.save(updated)
    }

    fun navigate(screen: Screen) {
        _screen.value = screen
    }

    companion object {
        /** Factory hook for creation with the [ProfileStore]. */
        fun factory(store: ProfileStore) = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(store) as T
            }
        }
    }
}
