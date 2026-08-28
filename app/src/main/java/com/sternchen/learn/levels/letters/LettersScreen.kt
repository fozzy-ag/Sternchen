package com.sternchen.learn.levels.letters

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
import com.sternchen.learn.levels.shared.SelectableTextOption
import kotlin.random.Random

/** Uppercase letters for the letter-identification lesson. */
private val LETTERS = listOf("A", "B", "C", "D", "E", "F")

/**
 * Letters / Sounds level.
 *
 * A letter's name is announced ("Tap the letter: A"). Three large letter
 * options are shown; the child taps the announced one. Correct picks advance;
 * wrong picks give a gentle "try again". A pre-academic, low-complexity,
 * non-flashing phonological-awareness activity using the preferred colour.
 */
@Composable
fun LettersScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    var round by remember { mutableIntStateOf(1) }

    val options by remember(round) { mutableStateOf(LETTERS.shuffled(Random(round * 41)).take(3)) }
    val targetIndex by remember(round) { mutableStateOf(Random(round * 19).nextInt(options.size)) }
    val targetLetter = options[targetIndex]

    val correctLabel = stringResource(R.string.feedback_correct)
    val tryAgainLabel = stringResource(R.string.feedback_try_again)
    val prompt = stringResource(R.string.letters_prompt)

    // Announce: "Tap the letter: A".
    LaunchedEffect(round) {
        if (profile.audioNarration) {
            speech.say("$prompt: $targetLetter")
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.letters_instruction,
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
                options.forEachIndexed { index, letter ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        SelectableTextOption(
                            text = letter,
                            size = 170f,
                            color = profile.preferredColor,
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
