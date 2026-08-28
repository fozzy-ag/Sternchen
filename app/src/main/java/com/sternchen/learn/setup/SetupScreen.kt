package com.sternchen.learn.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Intent
import com.sternchen.learn.R
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.AppLanguage
import com.sternchen.learn.config.InputMode
import com.sternchen.learn.config.LearnerProfile
import com.sternchen.learn.config.backgroundColor

/**
 * Caregiver/therapist-facing setup screen.
 *
 * Controls the research-informed accessibility parameters for this learner:
 * preferred color, reduced motion (seizure safety), input mode (touch vs switch
 * scanning), debounce (ataxia), extra latency (slow processing), and audio.
 *
 * Changes are persisted via onEdit and reflected immediately in the app theme.
 * All text is localized (German default, English option).
 */
@Composable
fun SetupScreen(
    profile: LearnerProfile,
    onEdit: (LearnerProfile) -> Unit,
    onStart: () -> Unit,
    speech: Speech,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profile.backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.setup_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )

        // Preferred colour
        SectionLabel(stringResource(R.string.setup_preferred_color))
        val speechColor = stringResource(R.string.speech_color)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                0xFFE53935.toInt(), // red
                0xFF43A047.toInt(), // green
                0xFF1E88E5.toInt(), // blue
                0xFFFDD835.toInt(), // yellow
                0xFFFF7043.toInt(), // orange
                0xFFAB47BC.toInt(), // purple
            ).forEach { argb ->
                val color = Color(argb)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(if (profile.preferredColorArgb == argb.toLong()) 44.dp else 36.dp)
                        .background(color)
                        .clickable {
                            onEdit(profile.copy(preferredColorArgb = argb.toLong()))
                            speech.say(speechColor)
                        },
                )
            }
        }

        // Language (German default; English optional)
        SectionLabel(stringResource(R.string.setup_language))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppLanguage.entries.forEach { language ->
                val selected = profile.language == language
                val label = when (language) {
                    AppLanguage.GERMAN -> stringResource(R.string.setup_language_german)
                    AppLanguage.ENGLISH -> stringResource(R.string.setup_language_english)
                }
                if (selected) {
                    Button(onClick = { /* already selected */ }) { Text(label) }
                } else {
                    OutlinedButton(
                        onClick = { onEdit(profile.copy(language = language)) }
                    ) { Text(label) }
                }
            }
        }

        // Reduced motion
        switchRow(
            label = stringResource(R.string.setup_reduce_motion),
            checked = profile.reducedMotion,
            onChecked = { enabled -> onEdit(profile.copy(reducedMotion = enabled)) },
        )

        // Input mode
        SectionLabel(stringResource(R.string.setup_input_method))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InputMode.entries.forEach { mode ->
                val selected = profile.inputMode == mode
                if (selected) {
                    Button(onClick = { /* already selected */ }) { Text(mode.label()) }
                } else {
                    OutlinedButton(
                        onClick = { onEdit(profile.copy(inputMode = mode)) }
                    ) { Text(mode.label()) }
                }
            }
        }

        // Debounce slider
        sliderRow(
            label = stringResource(R.string.setup_debounce),
            value = profile.debounceMillis / 1000f,
            valueRange = 0f..3f,
            display = "${profile.debounceMillis / 1000f}s",
            onValue = { v -> onEdit(profile.copy(debounceMillis = (v * 1000).toLong())) },
        )

        // Latency slider
        sliderRow(
            label = stringResource(R.string.setup_latency),
            value = profile.latencyMillis / 1000f,
            valueRange = 0f..8f,
            display = "${profile.latencyMillis / 1000f}s",
            onValue = { v -> onEdit(profile.copy(latencyMillis = (v * 1000).toLong())) },
        )

        // Audio narration
        switchRow(
            label = stringResource(R.string.setup_audio_narration),
            checked = profile.audioNarration,
            onChecked = { enabled -> onEdit(profile.copy(audioNarration = enabled)) },
        )
        switchRow(
            label = stringResource(R.string.setup_audio_only),
            checked = profile.audioOnlyMode,
            onChecked = { enabled -> onEdit(profile.copy(audioOnlyMode = enabled)) },
        )

        // Voice / narration (diagnostic and install helper)
        SectionLabel(stringResource(R.string.setup_voice))
        val voiceStatus = if (speech.isReady) {
            stringResource(R.string.setup_voice_ok)
        } else {
            stringResource(R.string.setup_voice_missing)
        }
        Text(
            text = voiceStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = if (speech.isReady) Color(0xFF66BB6A) else Color(0xFFFFB74D),
        )
        val testLabel = stringResource(R.string.setup_voice_test)
        val installLabel = stringResource(R.string.setup_voice_install)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val context = LocalContext.current
            OutlinedButton(
                onClick = {
                    speech.setLanguage(
                        if (profile.language == AppLanguage.GERMAN) java.util.Locale.GERMAN else java.util.Locale.ENGLISH
                    )
                    speech.say(testLabel)
                }
            ) { Text(testLabel) }
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent("com.android.settings.TTS_SETTINGS"))
                }
            ) { Text(installLabel) }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.setup_start))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
    )
}

@Composable
private fun switchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun sliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    onValue: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(display, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
        Slider(value = value, onValueChange = onValue, valueRange = valueRange)
    }
}

@Composable
private fun InputMode.label(): String = when (this) {
    InputMode.TOUCH -> stringResource(R.string.setup_input_touch)
    InputMode.SWITCH_SCAN -> stringResource(R.string.setup_input_switch)
}
