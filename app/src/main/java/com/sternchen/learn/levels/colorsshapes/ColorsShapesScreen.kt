package com.sternchen.learn.levels.colorsshapes

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sternchen.learn.R
import com.sternchen.learn.access.Haptics
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.levels.shared.LevelShell
import com.sternchen.learn.levels.shared.ObjectItem
import com.sternchen.learn.levels.shared.SelectableOption
import com.sternchen.learn.levels.shared.ShapeKind
import com.sternchen.learn.levels.shared.debouncedAction
import com.sternchen.learn.levels.shared.respond
import kotlin.random.Random
import kotlinx.coroutines.launch

/** A named colour entry used to build colour-recognition rounds. */
private data class ColorEntry(val color: Color, @StringRes val nameRes: Int)

private val COLORS = listOf(
    ColorEntry(0xFFE53935.toInt().toColor(), R.string.color_red),
    ColorEntry(0xFF1E88E5.toInt().toColor(), R.string.color_blue),
    ColorEntry(0xFF43A047.toInt().toColor(), R.string.color_green),
    ColorEntry(0xFFFDD835.toInt().toColor(), R.string.color_yellow),
)

private fun Int.toColor() = Color(this)

/**
 * Colors (and shapes) lesson.
 *
 * A target colour is announced ("Tap the shape in this colour: red"). Three
 * large, differently-coloured shapes are shown; the child taps the one that is
 * the announced colour. Correct picks give positive feedback and advance;
 * wrong picks give a gentle "try again". Uses high-saturation, low-complexity
 * targets appropriate for CVI and early colour discrimination.
 */
@Composable
fun ColorsShapesScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    val shapes = remember { ShapeKind.entries }
    var round by remember { mutableStateOf(1) }

    val targetColor by remember(round) { mutableStateOf(randomColor(round)) }
    val options by remember(round) { mutableStateOf(makeColorOptions(shapes, targetColor, round)) }

    val correctLabel = stringResource(R.string.feedback_correct)
    val instructionPrefix = stringResource(R.string.colors_instruction)
    val targetWord = stringResource(targetColor.nameRes)
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    val scope = rememberCoroutineScope()

    // Announce: "Tap the shape in this colour: <colour>".
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            speech.say("$instructionPrefix $targetWord")
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instruction = targetWord,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 88.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                options.forEach { option ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        val handleTap = debouncedAction(profile) {
                            scope.launch {
                                respond(
                                    profile = profile,
                                    speech = speech,
                                    haptics = haptics,
                                    isCorrect = option.color == targetColor.color,
                                    correctLabel = correctLabel,
                                    repeatLabel = targetWord,
                                    onCorrect = { round++ },
                                )
                            }
                        }
                        SelectableOption(
                            item = option,
                            size = 180f,
                            onClick = { handleTap() },
                        )
                    }
                }
            }
        }
    }
}

private fun makeColorOptions(shapes: List<ShapeKind>, target: ColorEntry, seed: Int): List<ObjectItem> {
    val rng = Random(seed * 53)
    val others = COLORS.filter { it != target }
    // Build options: one in the target colour, two in other colours.
    val targetOption = ObjectItem(shape = shapes[rng.nextInt(shapes.size)], color = target.color)
    val distractor1 = ObjectItem(shape = shapes[rng.nextInt(shapes.size)], color = others[0].color)
    val distractor2 = ObjectItem(shape = shapes[rng.nextInt(shapes.size)], color = others[1].color)
    return (listOf(targetOption, distractor1, distractor2)).shuffled(rng)
}

private fun randomColor(seed: Int): ColorEntry =
    COLORS[Random(seed * 17).nextInt(COLORS.size)]
