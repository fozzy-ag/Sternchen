package com.sternchen.learn.levels.objectnaming

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.launch

/**
 * Object Naming level.
 *
 * A single large object appears and its name is spoken (audio narration). The
 * child can tap it to hear the name again — this supports early vocabulary and
 * name-object association. Tapping also advances to the next object, keeping a
 * simple, single-focus, low-complexity layout (CVI-friendly).
 *
 * In audio-only mode the order is reversed for the first appearance of each
 * object: the name is spoken while the screen stays blank, and the object fades
 * in only once that name has finished playing, so the picture always supports a
 * sound the child has already heard. Tapping in that window also reveals it.
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
    val scope = rememberCoroutineScope()

    // Audio-only mode: which object's name has finished playing. The object stays
    // blank until that happens, so it is remembered as an index (not a flag) and
    // resets by itself when the sequence advances to the next object.
    var spokenIndex by remember { mutableIntStateOf(-1) }
    val revealed = !profile.audioOnlyMode ||
        !profile.audioNarration ||
        spokenIndex == index

    val revealAlpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (profile.reducedMotion || !profile.audioOnlyMode) 0 else 450,
        ),
        label = "audioOnlyReveal",
    )

    val handleTap = debouncedAction(profile) {
        scope.launch {
            // Speak the current name fully, then advance — same sequencing as the
            // correct-answer reward, so the old item stays visible while its audio
            // finishes and never overlaps the next item's prompt.
            if (profile.audioNarration) {
                speech.sayAndWait(objectName)
                // Tapping during the audio-only reveal also reveals the object.
                spokenIndex = index
            }
            index = (index + 1) % objectSequence.size
        }
    }

    // Speak the object name when it first appears (the prompt). In audio-only mode
    // this has to be awaited, because the object is revealed on the name's
    // completion rather than on its start.
    LaunchedEffect(index) {
        if (profile.audioNarration) {
            delay(if (index == 0) 400L else 700L)
            if (profile.audioOnlyMode) {
                speech.sayAndWait(objectName)
                spokenIndex = index
            } else {
                speech.say(objectName)
            }
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
            if (revealed) {
                ShapeView(
                    item = item,
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .alpha(revealAlpha),
                )
            }
        }
    }
}
