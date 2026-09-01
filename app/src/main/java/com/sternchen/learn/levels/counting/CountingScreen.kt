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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.sternchen.learn.levels.shared.debouncedAction
import com.sternchen.learn.levels.shared.respond
import kotlin.random.Random
import kotlinx.coroutines.launch

/**
 * Counting / Numbers level.
 *
 * A group of 1–[countingMax] identical, large, high-contrast objects is shown
 * (preferred colour, plain background). The objects are counted aloud, then the
 * child taps the matching numeral among large text options. Correct picks give
 * positive feedback and advance; wrong picks give a gentle "try again".
 *
 * Options layout:
 *  - up to 5 options → one row
 *  - 6–10 options → two rows of 5
 */
@Composable
fun CountingScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(1) }
    val countingMax = profile.countingMax.coerceIn(3, 10)

    val count by remember(round) { mutableStateOf(Random(round * 13).nextInt(1, countingMax + 1)) }
    val shape = remember(round) { ShapeKind.entries[Random(round * 29).nextInt(ShapeKind.entries.size)] }
    val item = remember(shape, profile.preferredColor) {
        ObjectItem(shape = shape, color = profile.preferredColor)
    }

    val correctLabel = stringResource(R.string.feedback_correct)
    val context = LocalContext.current
    val haptics = remember { Haptics(context) }
    val scope = rememberCoroutineScope()

    // Narrate the count aloud, e.g. "1, 2, 3".
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            val spokenCount = (1..count).joinToString(", ")
            speech.say(spokenCount)
        }
    }

    // Object sizes scale with the count on screen.
    val objSize = (when {
        count <= 2 -> 150f
        count <= 3 -> 130f
        count <= 5 -> 110f
        else -> 85f
    }) * profile.objectScale

    // Numeral option sizes scale with how many options are shown.
    val optSize = (when {
        countingMax <= 3 -> 160f
        countingMax <= 5 -> 140f
        else -> 120f
    }) * profile.objectScale

    // Build two rows when >5 items; otherwise one row.
    val objRows = if (count <= 5) listOf(count) else listOf(5, count - 5)
    val optRows = (1..countingMax).let { nums -> if (countingMax <= 5) listOf(nums.toList()) else nums.chunked(5) }

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
            objRows.forEach { rowCount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(rowCount) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            ShapeView(item = item, modifier = Modifier.size(objSize.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Numeral options.
            optRows.forEach { rowNumbers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowNumbers.forEach { n ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            val handleTap = debouncedAction(profile) {
                                scope.launch {
                                    respond(
                                        profile = profile,
                                        speech = speech,
                                        haptics = haptics,
                                        isCorrect = n == count,
                                        correctLabel = correctLabel,
                                        repeatLabel = count.toString(),
                                        onCorrect = { round++ },
                                    )
                                }
                            }
                            SelectableTextOption(
                                text = n.toString(),
                                size = optSize,
                                color = profile.preferredColor,
                                onClick = { handleTap() },
                            )
                        }
                    }
                }
            }
        }
    }
}
