package com.sternchen.learn.levels.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sternchen.learn.R
import com.sternchen.learn.access.Debouncer
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.InputMode
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.backgroundColor

/**
 * Standard full-screen scaffold for a level: it applies the profile's low-
 * complexity background, shows an optional top instruction line, and provides a
 * back-to-hub button. Individual levels fill the [content] slot.
 *
 * The whole background is non-interactive; only [content] carries tappable
 * targets so switch access and TalkBack traverse the actual options.
 */
@Composable
fun LevelShell(
    profile: LearnerProfile,
    onBack: () -> Unit,
    @StringRes instructionRes: Int = 0,
    instruction: String = "",
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(profile.backgroundColor),
    ) {
        if (!profile.audioOnlyMode) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White,
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .size(28.dp)
                    .clickable(onClick = onBack),
            )
        }

        val shownInstruction = when {
            instruction.isNotBlank() -> instruction
            instructionRes != 0 -> stringResource(instructionRes)
            else -> ""
        }
        if (shownInstruction.isNotBlank()) {
            Text(
                text = shownInstruction,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp, start = 48.dp, end = 48.dp),
            )
        }

        content()
    }
}

/**
 * A debounced callback that swallows accidental rapid presses (ataxia).
 * [onSelect] is invoked only when the press is accepted.
 */
@Composable
fun debouncedAction(
    profile: LearnerProfile,
    onSelect: () -> Unit,
): () -> Unit {
    val debouncer = remember {
        Debouncer(if (profile.inputMode == InputMode.SWITCH_SCAN) 0L else profile.debounceMillis)
    }
    return {
        if (debouncer.shouldAccept()) onSelect()
    }
}

/**
 * A large, singly-focused tappable option that renders a [ShapeView] of the
 * given size with platform-accessible semantics (content description spoken /
 * traversed by switch scanning).
 */
@Composable
fun SelectableOption(
    item: ObjectItem,
    size: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(item.shape.nameRes)
    val handleClick = onClick
    Box(
        modifier = modifier
            .size(size.dp)
            .semantics { contentDescription = label }
            .clickable(onClickLabel = label, onClick = handleClick),
        contentAlignment = Alignment.Center,
    ) {
        ShapeView(item = item, modifier = Modifier.fillMaxSize(0.9f))
    }
}

/** Advice about how many option cells to show; keeps targets large for CVI. */
fun optionSizeFor(count: Int): Float = when {
    count <= 2 -> 220f
    count <= 3 -> 170f
    else -> 130f
}

/**
 * A large, tappable option that renders a big symbol (e.g. a numeral or letter)
 * as text on a coloured rounded panel, with accessibility semantics. Used for
 * number and letter identification.
 */
@Composable
fun SelectableTextOption(
    text: String,
    size: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(color, RoundedCornerShape(24.dp))
            .semantics {
                contentDescription = text
                // Make the whole symbol one focusable unit for switch access.
            }
            .clickable(onClickLabel = text, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = (size * 0.55f).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
