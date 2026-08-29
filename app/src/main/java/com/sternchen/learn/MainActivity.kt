package com.sternchen.learn

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.sternchen.learn.access.Speech
import com.sternchen.learn.config.AppLanguage
import com.sternchen.learn.config.ProfileStore
import com.sternchen.learn.config.backgroundColor
import com.sternchen.learn.home.HomeScreen
import com.sternchen.learn.levels.causeeffect.CauseEffectScreen
import com.sternchen.learn.levels.colorsshapes.ColorsShapesScreen
import com.sternchen.learn.levels.counting.CountingScreen
import com.sternchen.learn.levels.findbyname.FindByNameScreen
import com.sternchen.learn.levels.letters.LettersScreen
import com.sternchen.learn.levels.matching.MatchingScreen
import com.sternchen.learn.levels.objectnaming.ObjectNamingScreen
import com.sternchen.learn.levels.samediff.SameDifferentScreen
import com.sternchen.learn.setup.SetupScreen
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var speech: Speech
    private var store: ProfileStore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Speech's constructor is fully guarded (TTS + ToneGenerator behind
        // runCatching), so a broken/missing engine can never crash boot.
        speech = Speech(this)

        val store = ProfileStore(applicationContext)
        this.store = store

        // Apply the saved UI language immediately (German is the default).
        applyLanguage(store.load().language)

        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModel.factory(store))
            AppRoot(vm, speech)
        }
    }

    override fun onDestroy() {
        speech.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun AppRoot(vm: AppViewModel, speech: Speech) {
    val profile by vm.profile.collectAsState()
    val screen by vm.screen.collectAsState()

    // Flush any in-flight/queued speech the moment the destination changes, so a
    // level's audio never carries over into the next screen (or back home).
    LaunchedEffect(screen) {
        speech.stop()
    }

    // Keep the UI and narration in the chosen language (German by default).
    SideEffect {
        applyLanguage(profile.language)
        speech.setLanguage(localeFor(profile.language))
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = profile.backgroundColor,
    ) {
        // Keep the system status bar legible: match its background to the app's
        // background color and flip the icon color based on luminance, so the
        // clock/battery/date are never rendered white-on-white or dark-on-dark.
        SystemBarAppearance(background = profile.backgroundColor)

        when (screen) {
            Screen.SETUP -> SetupScreen(profile, onEdit = { t -> vm.updateProfile(t) }, onStart = { vm.navigate(Screen.HOME) }, speech)
            Screen.HOME -> HomeScreen(
                profile,
                onCauseEffect = { vm.navigate(Screen.CAUSE_EFFECT) },
                onObjectNaming = { vm.navigate(Screen.OBJECT_NAMING) },
                onMatching = { vm.navigate(Screen.MATCHING) },
                onColorsShapes = { vm.navigate(Screen.COLORS_SHAPES) },
                onCounting = { vm.navigate(Screen.COUNTING) },
                onFindByName = { vm.navigate(Screen.FIND_BY_NAME) },
                onSameDifferent = { vm.navigate(Screen.SAME_DIFFERENT) },
                onLetters = { vm.navigate(Screen.LETTERS) },
                onBack = { vm.navigate(Screen.SETUP) },
            )
            Screen.CAUSE_EFFECT -> CauseEffectScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.OBJECT_NAMING -> ObjectNamingScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.MATCHING -> MatchingScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.COLORS_SHAPES -> ColorsShapesScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.COUNTING -> CountingScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.FIND_BY_NAME -> FindByNameScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.SAME_DIFFERENT -> SameDifferentScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
            Screen.LETTERS -> LettersScreen(profile, speech, onBack = { vm.navigate(Screen.HOME) })
        }
    }
}

@Composable
private fun SystemBarAppearance(background: Color) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = findViewByIdActivity(view.context)
            if (activity == null) return@SideEffect
            val window = activity.window
            // Color the window/decor background so the app's color extends behind
            // the transparent system bars. On API 35+ (edge-to-edge enforced,
            // incl. Android 16) setStatusBarColor is a no-op, so the decor
            // background is what shows through the bars. This is ignored nowhere.
            window.decorView.setBackgroundColor(background.toArgb())
            // Legacy path (older devices, API < 35): still honoured, harmless no-op on API 35+.
            window.statusBarColor = background.toArgb()
            window.navigationBarColor = background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            val light = background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = light
            controller.isAppearanceLightNavigationBars = light
        }
    }
}

private fun findViewByIdActivity(context: android.content.Context?): android.app.Activity? {
    var current = context
    while (current is android.content.ContextWrapper) {
        if (current is android.app.Activity) return current
        current = current.baseContext
    }
    return null
}

/** Map the app language to a BCP-47 locale tag used for resources and TTS. */
private fun localeFor(language: AppLanguage): Locale = when (language) {
    AppLanguage.GERMAN -> Locale.GERMAN
    AppLanguage.ENGLISH -> Locale.ENGLISH
}

/**
 * Apply the chosen app language to Android resources (so `stringResource` and
 * system-created UIs switch). German is the default regardless of device locale.
 * Only re-applies when the language actually differs, to avoid noisy restarts.
 */
private fun applyLanguage(language: AppLanguage) {
    val locales = LocaleListCompat.forLanguageTags(localeFor(language).toLanguageTag())
    if (!locales.equals(AppCompatDelegate.getApplicationLocales())) {
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
