package com.sternchen.learn.levels.matching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.sternchen.learn.levels.shared.optionSizeFor
import com.sternchen.learn.levels.shared.respond
import kotlin.random.Random

/** Distinct, high-saturation palette for option colour (CVI-friendly contrast). */
private val PALETTE = listOf(
    0xFFE53935.toInt().toColor(), // red
    0xFF1E88E5.toInt().toColor(), // blue
    0xFF43A047.toInt().toColor(), // green
    0xFFFDD835.toInt().toColor(), // yellow
)

private fun Int.toColor() = Color(this)

/**
 * Matching level.
 *
 * A target object is shown at the top. Three large options are shown below; one
 * is identical (same shape and colour) and two are different objects. Tapping
 * the matching option gives positive feedback and advances; a wrong pick gives a
 * gentle "try again" with no flashing. This trains visual discrimination with
 * large, low-complexity, high-contrast targets (CVI phase I–III).
 */
@Composable
fun MatchingScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    val shapes = remember { ShapeKind.entries }
    var round by remember { mutableStateOf(1) }

    // Deterministic per-round generation; a new round is produced on advance.
    val target by remember(round) { mutableStateOf(randomTarget(shapes, round)) }
    val options by remember(round) { mutableStateOf(makeOptions(target, shapes, round)) }

    val correctLabel = stringResource(R.string.feedback_correct)
    val tryAgainLabel = stringResource(R.string.feedback_try_again)
    val prompt = stringResource(R.string.matching_prompt)
    val instruction = stringResource(R.string.matching_instruction)
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }

    // Speak the target's name as the prompt for this round (audio narration).
    val targetWord = stringResource(target.shape.nameRes)
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            speech.say("$prompt, $targetWord")
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.matching_instruction,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 88.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Target object (slightly larger than options).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                SelectableOption(item = target, size = 200f, onClick = {})
            }

            Spacer(Modifier.height(24.dp))

            val optSize = optionSizeFor(options.size)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                options.forEach { option ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        SelectableOption(
                            item = option,
                            size = optSize,
                            onClick = {
                                respond(
                                    profile = profile,
                                    speech = speech,
                                    haptics = haptics,
                                    isCorrect = option == target,
                                    correctLabel = correctLabel,
                                    tryAgainLabel = tryAgainLabel,
                                    onCorrect = { round++ },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun randomTarget(shapes: List<ShapeKind>, seed: Int): ObjectItem {
    val rng = Random(seed * 31)
    val shape = shapes[rng.nextInt(shapes.size)]
    val color = PALETTE[rng.nextInt(PALETTE.size)]
    return ObjectItem(shape = shape, color = color)
}

private fun makeOptions(target: ObjectItem, shapes: List<ShapeKind>, seed: Int): List<ObjectItem> {
    val rng = Random(seed * 97)
    val others = shapes.filter { it != target.shape }
    val d1 = others[rng.nextInt(others.size)]
    val d2 = others.filter { it != d1 }[rng.nextInt(others.size - 1)]
    val distractors = listOf(
        ObjectItem(shape = d1, color = PALETTE[rng.nextInt(PALETTE.size)]),
        ObjectItem(shape = d2, color = PALETTE[rng.nextInt(PALETTE.size)]),
    )
    // Mix the correct option in with the distractors.
    val all = (distractors + target).shuffled(rng)
    return all
}
