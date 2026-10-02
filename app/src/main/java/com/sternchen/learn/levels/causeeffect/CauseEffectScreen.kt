package com.sternchen.learn.levels.causeeffect

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sternchen.learn.access.Debouncer
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.InputMode
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.backgroundColor
import com.sternchen.learn.config.preferredColor
import com.sternchen.learn.R
import kotlinx.coroutines.delay

/**
 * Cause & Effect level.
 *
 * A single, large, salient object sits on a plain (low-complexity) background in
 * the learner's preferred color. Tapping/selecting it produces a clear effect:
 * brief size + color change and a spoken sound name. This is the classic
 * cause/effect task that supports the earliest stage of visual attention and
 * switch use (CVI Phase I-II and first motor intent).
 *
 * Accessibility accommodations:
 *  - No flashing: feedback is a single slow, non-repeating pulse; with reduced
 *    motion it is an instant, still change (never rapid/repeating).
 *  - Debounce: rapid/accidental presses (ataxia/tremor) are ignored.
 *  - Switch Access / TalkBack: the object carries a content description and a
 *    click action, so the platform switch scanning can traverse and select it.
 *  - Audio: every effect speaks the object's name; audio-only mode hides visuals.
 */
@Composable
fun CauseEffectScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    val debouncer = remember {
        // Switch scanning already paces input; only debounce direct touch.
        Debouncer(if (profile.inputMode == InputMode.SWITCH_SCAN) 0L else profile.debounceMillis)
    }

    var trigger by remember { mutableIntStateOf(0) }
    var showPulse by remember { mutableStateOf(false) }

    val objectName = stringResource(R.string.cause_object)
    val tapForSound = stringResource(R.string.cause_tap_for_sound)
    val backLabel = stringResource(R.string.back)
    val playSoundLabel = stringResource(R.string.cause_tap_for_sound) // accessibility action label

    // Narration each time an effect fires.
    LaunchedEffect(trigger) {
        if (trigger > 0 && profile.audioNarration) {
            speech.say(objectName)
        }
    }

    // Brief, single (non-repeating) visual pulse; auto-clears.
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            showPulse = true
            delay(650)
            showPulse = false
        }
    }

    val currentColor by animateColorAsState(
        targetValue = if (showPulse) Color.White else profile.preferredColor,
        animationSpec = tween(durationMillis = if (profile.reducedMotion) 0 else 160),
        label = "feedback",
    )

    fun fireEffect() {
        if (debouncer.shouldAccept()) {
            trigger++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(profile.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (!profile.audioOnlyMode) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backLabel,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(28.dp)
                    .zIndex(1f)
                    .clickable(onClick = onBack),
            )
        } else {
            // The arrow is hidden here, so the full-screen sound target below
            // would leave no way back to the hub. zIndex keeps this above it.
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(96.dp)
                    .zIndex(1f)
                    .semantics {
                        contentDescription = backLabel
                        role = Role.Button
                    }
                    .clickable(onClickLabel = backLabel, onClick = onBack),
            )
        }

        if (profile.audioOnlyMode) {
            // Audio-only fallback for visual fatigue: a single large tap target.
            Text(
                text = tapForSound,
                color = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClickLabel = playSoundLabel, onClick = ::fireEffect),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        } else {
            val baseSize = (170f * profile.objectScale).dp
            val effectiveSize = if (showPulse) baseSize * 1.25f else baseSize

            Box(
                modifier = Modifier
                    .size(effectiveSize)
                    .background(currentColor, CircleShape)
                    .testTag("causeEffectObject")
                    .semantics { contentDescription = objectName }
                    .clickable(onClickLabel = playSoundLabel, onClick = ::fireEffect),
            )
        }
    }
}
