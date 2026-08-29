package com.sternchen.learn.levels.counting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sternchen.learn.R
import com.sternchen.learn.access.Haptics
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.preferredColor
import com.sternchen.learn.levels.shared.LevelShell
import com.sternchen.learn.levels.shared.ObjectItem
import com.sternchen.learn.levels.shared.SelectableTextOption
import com.sternchen.learn.levels.shared.ShapeKind
import com.sternchen.learn.levels.shared.ShapeView
import com.sternchen.learn.levels.shared.respond
import kotlin.random.Random

/**
 * Counting / Numbers level.
 *
 * A group of 1–3 identical, large, high-contrast objects is shown (preferred
 * colour, plain background). The objects are counted aloud, then the child taps
 * the matching numeral (1, 2 or 3) among large text options. Correct picks
 * give positive feedback and advance; wrong picks give a gentle "try again".
 * Supports early number sense with a low-complexity, non-flashing layout.
 */
@Composable
fun CountingScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(1) }

    val count by remember(round) { mutableStateOf(Random(round * 13).nextInt(1, 4)) }
    val shape = remember(round) { ShapeKind.entries[Random(round * 29).nextInt(ShapeKind.entries.size)] }
    val item = remember(shape, profile.preferredColor) {
        ObjectItem(shape = shape, color = profile.preferredColor)
    }

    val correctLabel = stringResource(R.string.feedback_correct)
    val tryAgainLabel = stringResource(R.string.feedback_try_again)
    val prompt = stringResource(R.string.counting_prompt)
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }

    // Narrate the count aloud, e.g. "1, 2, 3. How many?"
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            val spokenCount = (1..count).joinToString(", ")
            speech.say("$spokenCount. $prompt")
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.counting_instruction,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 88.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // The counted set of large objects.
            val objSize = when (count) { 1 -> 170f; 2 -> 150f; else -> 120f }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(count) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        ShapeView(item = item, modifier = Modifier.size(objSize.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Numeral options 1..3 as large text buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                (1..3).forEach { n ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        SelectableTextOption(
                            text = n.toString(),
                            size = 160f,
                            color = profile.preferredColor,
                            onClick = {
                                respond(
                                    profile = profile,
                                    speech = speech,
                                    haptics = haptics,
                                    isCorrect = n == count,
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
