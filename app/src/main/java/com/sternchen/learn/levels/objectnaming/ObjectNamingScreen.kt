package com.sternchen.learn.levels.objectnaming

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.sternchen.learn.R
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.preferredColor
import com.sternchen.learn.levels.shared.LevelShell
import com.sternchen.learn.levels.shared.ObjectItem
import com.sternchen.learn.levels.shared.ShapeKind
import com.sternchen.learn.levels.shared.ShapeView
import com.sternchen.learn.levels.shared.debouncedAction
import kotlinx.coroutines.delay

/**
 * Object Naming level.
 *
 * A single large object appears and its name is spoken (audio narration). The
 * child can tap it to hear the name again — this supports early vocabulary and
 * name-object association. Tapping also advances to the next object, keeping a
 * simple, single-focus, low-complexity layout (CVI-friendly).
 */
@Composable
fun ObjectNamingScreen(
    profile: LearnerProfile,
    speech: Speech,
    onBack: () -> Unit,
) {
    val objectSequence = remember {
        listOf(
            ShapeKind.BALL,
            ShapeKind.STAR,
            ShapeKind.SQUARE,
            ShapeKind.HEART,
            ShapeKind.TRIANGLE,
            ShapeKind.DIAMOND,
        )
    }
    var index by remember { mutableStateOf(0) }
    val objectName = stringResource(objectSequence[index].nameRes)
    val item = remember(index, profile.preferredColor) {
        ObjectItem(shape = objectSequence[index], color = profile.preferredColor)
    }
    val handleTap = debouncedAction(profile) {
        if (profile.audioNarration) speech.say(objectName)
        // Advance to the next object after the reward is spoken.
        index = (index + 1) % objectSequence.size
    }

    // Speak the object name when it first appears (the prompt).
    LaunchedEffect(index) {
        if (profile.audioNarration) {
            delay(if (index == 0) 400L else 700L)
            speech.say(objectName)
        }
    }

    LevelShell(
        profile = profile,
        onBack = onBack,
        instructionRes = R.string.objectnaming_prompt,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = objectName }
                .clickable(onClickLabel = objectName, onClick = { handleTap() }),
            contentAlignment = Alignment.Center,
        ) {
            if (!profile.audioOnlyMode) {
                ShapeView(item = item, modifier = Modifier.fillMaxSize(0.6f))
            }
        }
    }
}
