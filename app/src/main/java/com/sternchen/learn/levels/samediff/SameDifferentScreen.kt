package com.sternchen.learn.levels.samediff

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
import androidx.compose.runtime.mutableIntStateOf
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

/** Distinct colours used to make the "different" object stand out. */
private val PALETTE = listOf(
    0xFFE53935.toInt().let { Color(it) },
    0xFF1E88E5.toInt().let { Color(it) },
    0xFF43A047.toInt().let { Color(it) },
    0xFFFDD835.toInt().let { Color(it) },
)

/**
 * Same / Different level.
 *
 * Three large objects appear: two are identical (same shape and colour) and one
 * is different. The child finds and taps the different one. Correct picks
 * advance; a tap on one of the identical objects gives a gentle "try again".
 * Refines visual discrimination beyond simple matching, with large, low-
 * complexity, non-flashing targets.
 */
@Composable
fun SameDifferentScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(1) }

    val shapes = remember { ShapeKind.entries }
    val triple by remember(round) { mutableStateOf(buildTriple(shapes, round)) }
    val differentIndex by remember(round) { mutableStateOf(triple.indexOfFirst { it.isDifferent }) }

    val correctLabel = stringResource(R.string.feedback_correct)
    val prompt = stringResource(R.string.samediff_prompt)
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    val scope = rememberCoroutineScope()

    // Announce the task.
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            speech.say(prompt)
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.samediff_instruction,
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
                triple.forEachIndexed { index, item ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        val handleTap = debouncedAction(profile) {
                            scope.launch {
                                respond(
                                    profile = profile,
                                    speech = speech,
                                    haptics = haptics,
                                    isCorrect = index == differentIndex,
                                    correctLabel = correctLabel,
                                    repeatLabel = prompt,
                                    onCorrect = { round++ },
                                )
                            }
                        }
                        SelectableOption(
                            item = item.item,
                            size = 170f,
                            onClick = { handleTap() },
                        )
                    }
                }
            }
        }
    }
}

private data class Choice(val item: ObjectItem, val isDifferent: Boolean)

private fun buildTriple(shapes: List<ShapeKind>, seed: Int): List<Choice> {
    val rng = Random(seed * 61)
    val sameShape = shapes[rng.nextInt(shapes.size)]
    val sameColor = PALETTE[rng.nextInt(PALETTE.size)]
    val diffShape = shapes.filter { it != sameShape }[rng.nextInt(shapes.size - 1)]
    val diffColor = PALETTE.filter { it != sameColor }[rng.nextInt(PALETTE.size - 1)]

    val same = ObjectItem(shape = sameShape, color = sameColor)
    val different = ObjectItem(shape = diffShape, color = diffColor)

    // Two identical + one different, then shuffle position.
    return (listOf(
        Choice(same, false),
        Choice(same, false),
        Choice(different, true),
    )).shuffled(rng)
}
