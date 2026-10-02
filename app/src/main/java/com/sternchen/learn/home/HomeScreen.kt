package com.sternchen.learn.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sternchen.learn.R
import com.sternchen.learn.access.Debouncer
import com.sternchen.learn.config.InputMode
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.backgroundColor
import com.sternchen.learn.config.preferredColor

/**
 * Home hub: the level-selection menu.
 *
 * Large, singly-focussed buttons in the learner's preferred color on the plain
 * low-complexity background. Each entry opens one level. Text contrast is kept
 * high. Input is debounced to accommodate ataxia.
 */
@Composable
fun HomeScreen(
    profile: LearnerProfile,
    onCauseEffect: () -> Unit,
    onObjectNaming: () -> Unit,
    onMatching: () -> Unit,
    onColorsShapes: () -> Unit,
    onCounting: () -> Unit,
    onFindByName: () -> Unit,
    onSameDifferent: () -> Unit,
    onLetters: () -> Unit,
    onBack: () -> Unit,
) {
    val debouncer = remember {
        Debouncer(if (profile.inputMode == InputMode.SWITCH_SCAN) 0L else profile.debounceMillis)
    }
    val backLabel = stringResource(R.string.back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profile.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (!profile.audioOnlyMode) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backLabel,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(4.dp)
                        .zIndex(1f)
                        .clickable(onClick = onBack),
                )
            } else {
                // Audio-only: keep the way back to Setup reachable without the
                // arrow. Sized generously because it is invisible and unhinted.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(96.dp)
                        .zIndex(1f)
                        .semantics {
                            contentDescription = backLabel
                            role = Role.Button
                        }
                        .clickable(onClickLabel = backLabel, onClick = onBack),
                )
            }
        }

        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(8.dp))

        HomeButton(
            label = stringResource(R.string.home_level_cause_effect),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onCauseEffect() }
        HomeButton(
            label = stringResource(R.string.home_level_object_naming),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onObjectNaming() }
        HomeButton(
            label = stringResource(R.string.home_level_matching),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onMatching() }
        HomeButton(
            label = stringResource(R.string.home_level_colors_shapes),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onColorsShapes() }
        HomeButton(
            label = stringResource(R.string.home_level_counting),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onCounting() }
        HomeButton(
            label = stringResource(R.string.home_level_find_by_name),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onFindByName() }
        HomeButton(
            label = stringResource(R.string.home_level_same_different),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onSameDifferent() }
        HomeButton(
            label = stringResource(R.string.home_level_letters),
            accent = profile.preferredColor,
        ) { if (debouncer.shouldAccept()) onLetters() }
    }
}

@Composable
private fun HomeButton(
    label: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(accent, RoundedCornerShape(20.dp))
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
    }
}
