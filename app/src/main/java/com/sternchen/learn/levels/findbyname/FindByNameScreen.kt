package com.sternchen.learn.levels.findbyname

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sternchen.learn.R
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.preferredColor
import com.sternchen.learn.levels.shared.LevelShell
import com.sternchen.learn.levels.shared.ObjectItem
import com.sternchen.learn.levels.shared.SelectableOption
import com.sternchen.learn.levels.shared.ShapeKind
import kotlin.random.Random

/**
 * Find by Name (receptive vocabulary) level.
 *
 * An object's name is announced ("Tap the ball"). Three large, distinct shapes
 * are shown, all in the learner's preferred colour so discrimination is by shape
 * alone. The child taps the named one. Correct picks advance; wrong picks give a
 * gentle "try again". Builds receptive language with low-complexity, CVI-safe
 * targets.
 */
@Composable
fun FindByNameScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(1) }

    val shapes = remember { ShapeKind.entries }
    val optionShapes by remember(round) { mutableStateOf(pickDistinctShapes(shapes, 3, round)) }
    val targetIndex by remember(round) { mutableStateOf(Random(round * 7).nextInt(optionShapes.size)) }
    val targetShape = optionShapes[targetIndex]

    val options = remember(optionShapes, profile.preferredColor) {
        optionShapes.map { ObjectItem(shape = it, color = profile.preferredColor) }
    }

    val correctLabel = stringResource(R.string.feedback_correct)
    val tryAgainLabel = stringResource(R.string.feedback_try_again)
    val prompt = stringResource(R.string.findbyname_prompt)
    val targetWord = stringResource(targetShape.nameRes)

    // Announce: "Tap the picture: <name>" e.g. "Tap the picture: ball".
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            speech.say("$prompt: $targetWord")
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.findbyname_instruction,
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
                options.forEachIndexed { index, option ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        SelectableOption(
                            item = option,
                            size = 180f,
                            onClick = {
                                if (index == targetIndex) {
                                    if (profile.audioNarration) speech.say(correctLabel)
                                    round++
                                } else {
                                    if (profile.audioNarration) speech.say(tryAgainLabel)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Returns [n] distinct shapes so the options differ only by shape. */
private fun pickDistinctShapes(shapes: List<ShapeKind>, n: Int, seed: Int): List<ShapeKind> {
    return shapes.shuffled(Random(seed * 37)).take(n)
}
