# Sternchen

**Offline-first, CVI-safe learning app for children with PIGT-related cerebellar underdevelopment (MCAHS3).**

> German is the default and primary language of the app (English optional via an in-app
> caregiver setting, independent of the device locale).

---

## Versioning

| Field       | Value   |
|-------------|---------|
| Version     | `0.5.1` |
| VersionCode | `1`     |
| Scheme      | Semantic Versioning (MAJOR.MINOR.PATCH) |

Version fields live in `app/build.gradle.kts` (`defaultConfig.versionCode` /
`versionName`). Every user-facing change gets a changelog entry below.

### Changelog

#### 0.5.1 (2026-10-02) — audio-only mode reveals the picture after the sound
- **Audio-only mode no longer leaves the screen blank forever**. On Cause & Effect and
  Object Naming the object was suppressed for the whole session, so the level could never
  be named visually and tapping the "tap for sound" prompt produced audio with still
  nothing on screen. Both now reveal the object once its name has actually finished
  playing: Cause & Effect on the first effect, Object Naming when the spoken name
  completes. The reveal is a 450 ms fade, or instant with reduced motion.
- The object stays revealed for the rest of the round, and tapping it still replays the
  sound, so nothing is lost once the picture is up.
- Tapping during the Object Naming reveal also reveals it, in case the child acts before
  the narration ends.
- With audio narration switched off there is no sound to wait for, so audio-only mode
  shows the object immediately rather than leaving the level unusable.

#### 0.5.0 (2026-10-02) — fixes for haptics persistence, back navigation and audio-only exit
- **Haptic setting now persists**: "Vibrate on wrong answer" was shown in Setup and
  honoured at runtime, but never written to storage, so it silently reverted to on
  after every restart. A caregiver who turned it off got buzzes back.
- **Back is now a navigation stack**: previously nothing intercepted the system back
  gesture, so it closed the app from any screen. Now levels return to the hub, the
  hub returns to Setup, and Setup exits.
- **Audio-only mode is no longer a dead end**: with the back arrow hidden, the child
  had no in-app way out of a level and back dropped them at the launcher. An invisible
  96 dp corner zone now replaces the arrow — reachable by touch and by switch access,
  announced as "Back" — and arriving back at the hub speaks its title.
- **First automated tests**: `ProfileStoreTest` round-trips every profile field and
  fails if a new setting is added to the UI without being persisted. `ProfileStore`
  now takes a `SharedPreferences` so it is testable on the JVM.
- **Corrected documentation**: audio-only mode was described as hiding all visuals; it
  only drops the back button and makes Cause & Effect and Object Naming audio-only.

#### 0.4.0 (2026-08-29) — count up to 5 or 10
- **Counting level range**: the count now spans 1–5 (default) or up to 10, chosen
  by a new "Count up to" control (3 / 5 / 10) on the Setup screen.
- Counting objects and numeral options resize automatically and split into two
  rows when more than 5 are shown, keeping targets large.

#### 0.3.2 (2026-08-29) — app icon
- **Launcher icon**: a glossy 3D yellow star with twinkle sparkles on a soft
  light-blue background (adaptive icon incl. Android 13 themed-icon silhouette).
  Replaces the default Android icon the app previously shipped with.

#### 0.3.1 (2026-08-29) — Naming: advance only after the spoken name completes
- In the Object Naming level, tapping to hear the name now keeps the current
  object on screen and only advances to the next object once the name has been
  fully spoken (same sequencing as the correct-answer reward). Previously the
  display swapped immediately and the next item's prompt could cut off the tail
  of the previous name.

#### 0.3.0 (2026-08-29) — full setup for the learner profile
- **Learner name**: caregivers can enter the child's name (used as a friendly label).
- **Background color**: pick from four CVI-safe backgrounds (near-black default, dark
  blue, dark grey, off-white); applied to every screen via `backgroundArgb`.
- **Object size**: a global scale slider (0.7×–1.4×) now sizes objects/options in every
  level (Cause & Effect, Letters, Counting, Matching, Find-By-Name, Same/Different,
  Colors & Shapes), not just Cause & Effect.
- **Repeats of the task** (`stimulusRepetitions`): slider exposed and persisted.
- **Switch scanning speed** (`scanDwellMillis`): slider exposed and persisted for future
  in-app scan pacing; platform switch access still drives scanning today.

#### 0.2.1 (2026-08-29) — TTS queue fix + shorter/clearer feedback
- **Fixed stale/queued speech**: `Speech` now flushes the engine before each new
  utterance and speech is stopped on every screen change, so audio no longer piles up,
  keeps playing after a round has moved on, or carries into the next level.
- **Correct reward sequencing**: the one-word reward ("Super!"/"Great!") is spoken
  first and the next round only advances once it finishes, via a TTS completion hook.
- **Ataxia debounce** now also guards the choice-level tap handlers.
- **Shorter, clearer feedback**: correct answers are now a single word
  ("Super!"/"Great!"); wrong answers repeat the shortened target word (the letter,
  number, item or colour) instead of "try again"; the Counting level now voices the
  number sequence only (no "How many?").

#### 0.2.0 (2026-08-29) — CVI letter bubbling + wrong-only haptics
- **CVI letter bubbling**: every letter/number tile (`SelectableTextOption`) now has a
  thick high-contrast white outline that separates the preferred-color panel from the
  plain dark background, reducing visual-crowding/contour interference.
- **Wrong-only haptic feedback**: a short, gentle vibration on wrong responses only;
  correct responses keep their visual + audio reward (no haptic reinforcement, per
  research). New caregiver toggle "Vibrate on wrong answer" in Setup
  (`hapticFeedback`, default on), persisted in `LearnerProfile`.
- Added `access/Haptics.kt` (crash-guarded, like `Speech`), `VIBRATE` permission, and a
  shared `respond()` helper now used by all 6 choice-based levels.

- Added `MIT License` (`LICENSE` file).
- Replaced the proprietary notice with MIT; added comprehensive disclaimers to the
  README (private project, not medical advice, no liability, generative-AI-assisted
  development). No application code changed in this release.

#### 0.1.0 (2026-08-28) — first build
- Caregiver setup screen: preferred color, language (German/English), reduce motion,
  input mode (touch / switch scan), debounce, latency, audio narration, audio-only mode,
  TTS voice status + install/test helpers.
- Home hub with 8 levels:
  1. Cause & Effect
  2. Object Naming
  3. Matching
  4. Colors
  5. Counting (1–3)
  6. Find by Name (receptive)
  7. Same / Different
  8. Letters (A–F)
- Accessibility core: CVI-safe visuals (plain dark background, preferred color, large
  single-focus targets, no flashing), ataxia debounce, switch-scan-ready semantics,
  TTS narration with guaranteed tone fallback, crash-proof `Speech` initialization.
- In-app language switching (German default) via `AppCompatDelegate.setApplicationLocales`.
- Edge-to-edge / Android 15–16 system-bar handling.

---

## Purpose

Sternchen ("little star") is a mixed-age, single flexible tool: the same app supports
early cause-and-effect engagement and pre-academic skills, tailored at runtime to one
learner's abilities via the caregiver setup screen. It is designed around the
characteristic needs of children with **MCAHS3** (PIGT-related
Multiple Congenital Anomalies–Hypotonia–Seizures Syndrome 3):

- **Cerebellar underdevelopment / atrophy** → slow processing, motor inaccuracy (ataxia),
  cerebellar cognitive-affective syndrome (CCAS) profiles.
- **High prevalence of cortical/cerebral visual impairment (CVI)** → color preference,
  clutter sensitivity, visual latency, single-focus presentation.
- **Epilepsy** → strict no-flashing / photosensitivity safety (WCAG 2.3).
- **Hypotonia / ataxia** → debounced input, switch access, generous response time.

All learning content works **fully offline**: no network, no accounts, no external
services. State is persisted on-device (SharedPreferences).

## Design principles (each maps to research — see [Research background](#research-background))

| Principle | Implementation |
|---|---|
| Preferred/salient color | All primary targets render in the learner's chosen high-saturation color (CVI color preference). |
| Low visual clutter | Plain near-black background, one task per screen, ≤3 large options. |
| Large, single-focus targets | Options 130–220 dp; whole-screen tap targets on early levels. |
| No flashing / no rapid motion | Single slow (650 ms) pulse feedback; `reducedMotion` makes it an instant still change (WCAG 2.3.1). |
| Slow processing support | Configurable response latency (0–8 s) and configurable stimulus repetition. |
| Ataxia-safe input | Configurable debounce (0–3 s) swallows accidental rapid re-presses; disabled in switch-scan mode (scanning paces input). |
| Switch access | Every interactive element carries a content description and click action, so platform Switch Control / scanning traverses real options. |
| Audio-first | TTS narration of prompts, counts and feedback; tone blip guarantees audible output even if no TTS engine/voice is installed; audio-only mode drops the back button and holds the picture back on Cause & Effect and Object Naming until the name has been spoken. |
| German-first, English optional | In-app language toggle; German default regardless of device locale. |
| Caregiver-controlled | All accommodations are explicit caregiver settings, not automatic. |

## Technology stack

- **Kotlin 2.4.10** + **Jetpack Compose** (Material 3), single-module Android app.
- **AGP 8.7.2**, `compileSdk` 35, `minSdk` 26, `targetSdk` 35, JVM target 17.
- **StateFlow** (`AppViewModel`) as single source of truth; no navigation library — a
  `Screen` enum drives the root `when`.
- **SharedPreferences** via a thin `ProfileStore` (deliberately replaceable by Room later).
- **TextToSpeech** + `ToneGenerator` in a crash-proof `Speech` wrapper.
- No test framework yet; no CI yet.

## Project structure

```
sternchen/
├── app/
│   ├── build.gradle.kts              # version 0.1.0, deps, compose
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml       # single MainActivity launcher
│       ├── java/com/sternchen/learn/
│       │   ├── MainActivity.kt       # edge-to-edge, in-app language, system bars
│       │   ├── AppViewModel.kt       # profile + navigation state
│       │   ├── access/
│       │   │   ├── Speech.kt         # TTS + tone fallback (crash-proof)
│       │   │   └── Debouncer.kt      # ataxia debounce
│       │   ├── config/
│       │   │   ├── LearnerProfile.kt # all accessibility settings
│       │   │   └── ProfileStore.kt   # offline persistence (SharedPreferences)
│       │   ├── setup/
│       │   │   └── SetupScreen.kt    # caregiver controls
│       │   ├── home/
│       │   │   └── HomeScreen.kt     # level-selection hub
│       │   └── levels/
│       │       ├── shared/
│       │       │   ├── LevelShell.kt # scaffold, back button, SelectableOption(s), sizes
│       │       │   └── Shapes.kt     # 6 shapes drawn with Compose Canvas
│       │       ├── causeeffect/      ├── objectnaming/
│       │       ├── matching/         ├── colorsshapes/
│       │       ├── counting/         ├── findbyname/
│       │       ├── samediff/         └── letters/
│       └── res/
│           ├── values/strings.xml    # German (default)
│           ├── values-en/strings.xml # English
│           ├── values/colors.xml
│           ├── values/themes.xml     # Theme.AppCompat.NoActionBar
│           └── drawable/ic_launcher_foreground.xml
├── build.gradle.kts                  # AGP/Kotlin plugin versions
├── settings.gradle.kts
├── gradle.properties                 # committed (machine-agnostic)
└── planning-links.html               # research links (clickable)
```

Machine-specific build values (never committed):
- `local.properties` → `sdk.dir` (gitignored).
- `~/.gradle/gradle.properties` → optional local overrides such as `android.aapt2FromMavenOverride`
  (Termux needs the system `aapt2`; stock machines use the Maven-provided one).

## App flow

```
SETUP (caregiver) ──▶ HOME (hub, 8 level buttons) ──▶ any LEVEL ──(back)──▶ HOME
```

The app always boots to **Setup** so a caregiver can confirm/adjust the learner profile;
profile changes persist and apply immediately (theme, language, input, audio).

## Caregiver settings (Setup screen)

| Setting | Values / range | Purpose |
|---|---|---|
| Preferred color | 6 saturated colors (red default) | CVI salient color for all primary targets. |
| Learner's name | free text | Friendly label for the child. |
| Background color | 4 CVI-safe colors (near-black default) | Plain, low-complexity background on every screen. |
| Object size | 0.7×–1.4× slider | Global scale for objects/options in all levels. |
| Switch scanning speed | 0.2–5 s slider | Dwell time for switch scanning (persisted; platform scanning drives today). |
| Repeats of the task | 1–5 slider | Promised repetition count of each task prompt (persisted). |
| Count up to | 3 / 5 / 10 | Counting-level difficulty (number range). |
| Language | Deutsch (default) / Englisch | In-app UI + TTS language, independent of device locale. |
| Reduce motion | on (default) / off | Turns the feedback pulse into an instant still change; seizure safety. |
| Input method | Touch (default) / Switch/Scan | Debounce is auto-disabled in switch-scan mode. |
| Debounce | 0–3 s slider | Ignores accidental rapid double-presses (ataxia/tremor). |
| Latency | 0–8 s slider | Extra response time (slow processing). |
| Audio narration | on (default) / off | TTS prompts + spoken feedback. |
| Audio-only mode | on / off | Hides the back button (an invisible labelled corner zone replaces it for switch access and touch). On Cause & Effect and Object Naming the screen is blank until the name has been spoken, then the picture fades in — sound first, visual as support. Tapping during that window reveals it too. |
| Voice / narration (diagnostic) | status + **Test** + **Install** buttons | Shows whether a TTS voice is ready; opens system TTS settings to install a German voice. |

Persisted profile fields that are not yet surfaced in the UI (available for future
settings): `plainBackground`, `simpleLayout`, `explicitInstruction`.

## Levels

All levels share the `LevelShell` scaffold (plain background, top instruction line,
z-raised back button), debounced touch input, TTS narration, and the same reward model:
correct → spoken praise + advance to a new round; wrong → gentle spoken "try again",
no penalty, no flashing.

| # | Level | Skill target | Task |
|---|-------|--------------|------|
| 1 | **Cause & Effect** | Visual attention, motor intent, switch Stage 1 | One large circle in preferred color; tap → single slow pulse + spoken name. |
| 2 | **Object Naming** | Expressive vocabulary | One large object, name spoken; tap to re-hear and advance (cycles 6 objects). |
| 3 | **Matching** | Visual discrimination | Target object on top; pick the identical one (same shape + color) among 3. |
| 4 | **Colors** | Color discrimination | "Tap the shape in this colour: red" — pick the announced color among 3. |
| 5 | **Counting** | Early number sense (1–3) | 1–3 identical objects counted aloud; tap the matching numeral. |
| 6 | **Find by Name** | Receptive vocabulary | "Tap the picture: ball" — all options in preferred color, discrimination by shape alone. |
| 7 | **Same / Different** | Refined visual discrimination | Two identical + one different; tap the odd one out. |
| 8 | **Letters** | Letter identification (A–F) | "Tap the letter: A" — 3 large letter tiles. |

Rounds are generated deterministically per round index (`Random(seed)`), so a round is
stable while the learner works on it.

Object vocabulary (6 shapes drawn with Compose `Canvas`): ball, star, square, heart,
triangle, diamond. Color palette: red, blue, green, yellow (high-saturation).

## Audio system

`access/Speech.kt` guarantees "there is always sound":

- `TextToSpeech` is created inside `runCatching`; if it throws or the engine never
  becomes ready, the code degrades instead of crashing (a missing TTS engine must never
  take the app down).
- `ToneGenerator` is likewise nullable/guarded (it can throw on devices with broken
  audio routing).
- Narration requested before TTS readiness is buffered and flushed once ready.
- If TTS is unavailable, every narration plays a short tone blip instead — so a device
  with no voice still gives audible feedback.
- `setLanguage()` keeps the voice in the in-app UI language; missing-voice fallback
  (e.g. German voice absent → device default) keeps speech working.
- Setup screen shows TTS status and offers **test** + **open system TTS settings**
  (needed when the device has no TTS engine/voice installed).

## Accessibility details

- **Switch access**: `contentDescription` + `clickable(onClickLabel = …)` on every
  target; only actual options are focusable (background is non-interactive) so platform
  scanning traverses the meaningful set.
- **Debounce**: first press within the window is accepted, repeats swallowed
  (`access/Debouncer.kt`); 0 ms in switch-scan mode.
- **Photosensitivity**: no flashing, no strobing, no repeating pulses; max one slow
  color pulse per interaction (instant when reduced motion).
- **System bars**: edge-to-edge with decor background matched to the app background and
  light/dark icons chosen by luminance (Android 15/16 `setStatusBarColor` no-ops handled).
- **Language**: `AppCompatDelegate.setApplicationLocales` applies the in-app choice at
  startup and on change; TTS follows it.

## Build & install

### Stock machine (Android SDK present)

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` must point at an SDK (`sdk.dir=…`). No other configuration needed.

### Termux (the build machine for this project)

- Android SDK lives outside the repo (`local-sdk`), referenced via gitignored
  `local.properties`.
- Termux needs the system `aapt2`, set as a **user-level** Gradle property in
  `~/.gradle/gradle.properties` (not committed):
  `android.aapt2FromMavenOverride=$PREFIX/bin/aapt2`.
- `org.gradle.jvmargs=-Xmx3072m` (committed in `gradle.properties`) avoids daemon OOM.

Install on device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Research background

Full clickable list: [`planning-links.html`](planning-links.html).

### 1) The condition: PIGT / MCAHS3
PIGT (GPI-transamidase subunit) biallelic variants cause MCAHS3: neonatal hypotonia,
intractable seizures, severe global developmental delay with regression, dysmorphic
features, and **progressive cerebral + cerebellar atrophy**; cortical visual impairment
and nystagmus are common. Only ~40–41 cases reported worldwide.

- [OMIM #615398 – MCAHS3 (PIGT)](https://omim.org/entry/615398)
- [Spectrum of MCAHS3, systematic review of 41 cases (PMC)](https://pmc.ncbi.nlm.nih.gov/articles/PMC11187727/)
- [GARD – MCAHS3 overview](https://rarediseases.info.nih.gov/diseases/17584/multiple-congenital-anomalies-hypotonia-seizures-syndrome-3)
- [ClinGen condition curation (MONDO:0014165)](https://search.clinicalgenome.org/kb/conditions/MONDO:0014165)
- [Novel PIGT variant expands MCAHS3 phenotype (MDPI Genes)](https://www.mdpi.com/2073-4425/7/12/108)
- [Homozygous PIGT mutation leads to MCAHS3 (PMC)](https://pmc.ncbi.nlm.nih.gov/articles/PMC5951959/)
- [NORD – MCAHS3](https://rarediseases.org/mondo-disease/multiple-congenital-anomalies-hypotonia-seizures-syndrome-3/)

### 2) Cerebellar cognitive-affective syndrome (CCAS / Schmahmann)
Cerebellar underdevelopment produces deficits in executive function (working memory,
set-shifting, fluency), visuospatial cognition, language, and emotion-affect — the
target domains for the app's academic levels.

- [CCAS Task Force paper (PubMed)](https://pubmed.ncbi.nlm.nih.gov/31522332/)
- [CCAS bedside scale (Brain 2017)](https://academic.oup.com/brain/article/141/1/248/4676034)
- [Task Force paper open PDF (Berkeley)](https://ivrylab.berkeley.edu/files/organized_pubs_pdfs/2020_argyropoulos_et_al_the_ce.pdf)
- [CCAS meta-analysis (The Cerebellum 2019)](https://link.springer.com/article/10.1007/s12311-019-01060-2)
- [CCAS in a 12-year-old (ScienceDirect)](https://www.sciencedirect.com/science/article/abs/pii/S1090379816000064)
- [CCAS in Ataxia-Telangiectasia, children (PubMed)](https://pubmed.ncbi.nlm.nih.gov/30338439/)

### 3) CVI learning apps & frameworks (design comparators)
Perkins CVI framework / Roman-Lantzy phases: color preference, single high-contrast
target, low array/sensory complexity, visual latency, movement & light preference.

- [VisionLearn – Kotlin Multiplatform CVI learning app (GitHub)](https://github.com/arulagarwal/VisionLearn)
- [CViConnect – CVI Range & Phases I–III](https://cviconnect.co/category/cviconnect/)
- [Vizzbu – CVI app (IIT Bombay)](https://rnd.iitb.ac.in/index.php/node/1335)
- [Bubbly – CVI literacy app (word "bubbling")](https://bubbly-cvi-literacy.appstor.io/)
- [Dynamic Difficulty Adjustment in serious games for CVI (IEEE 2023)](https://doi.org/10.1109/segah57547.2023.10253769)
- [Vision Nanny – CVI platform (NITI)](https://frontiertech.niti.gov.in/story/making-learning-visible-ai-powered-platform-helps-children-with-visual-processing-disorders-thrive-in-classrooms/)

### 4) Inclusive design for ataxia / motor + cognitive disability (CP)
Large targets, adjustable sensory settings, predictable navigation, reduced clutter,
WCAG 2.2/2.3 (no flashing, ≥3 flashes threshold), haptic + audio + visual feedback.

- [Learning Software Application Design Model for CP (IjIC)](https://doi.org/10.11113/ijic.v4n1.63)
- [Usability barriers & inclusive design in mobile learning apps (TUNI)](https://trepo.tuni.fi/handle/10024/232646)
- [Visual perceptual skills with AT-supported app for CP (APH 2024)](https://doi.org/10.12700/aph.21.3.2024.3.3)
- [RESNA 2025 – switch-accessible tablet game design](https://www.resna.org/sites/default/files/conference/2025/StudentDesignChallenge/117.html)
- [Digital accessibility for CP](https://www.accessibility.com/blog/digital-accessibility-for-people-with-cerebral-palsy-3-things-to-know)
- [Digital accessibility for CP, WCAG detail (LinkedIn)](https://www.linkedin.com/pulse/digital-accessibility-cerebral-palsy-narayanan-palani-x3rme)

### 5) Switch access
- [Switch access to technology – comprehensive guide (White Rose)](https://eprints.whiterose.ac.uk/id/eprint/10291/1/)
- [Seven stages of switch development (AAC London)](https://aac.london/wp-content/uploads/2023/11/Seven-Stages-of-Switch-Development-for-children.pdf)

## Known limitations

- **Fixed difficulty**: every array level always shows 3 options; no entry-level or
  in-game adaptivity yet.
- **No progress tracking** (no Room yet; `ProfileStore` is the designated replacement point).
- **`scanDwellMillis`, `latencyMillis` and `stimulusRepetitions` are settable but not yet
  driving behavior**: all three sliders persist their values; scan pacing still comes from
  platform switch access, there is no visual-latency delay, and rounds don't yet repeat
  the prompt N times.
- **Haptics are wrong-only** (by design: correct responses use visual + audio reward only;
  see [Roadmap](#roadmap--future-additions) and the feedback section above).
- **Fixed letter sets**: letters A–F (deliberately small). Counting is now caregiver-ranged (3/5/10).
- **Single profile**: one learner per device.
- **Tests cover persistence only** (JVM unit tests, no CI yet); the levels and the audio
  and haptics wrappers are untested.
- TTS quality depends on the device's installed engine/voice (the app degrades to tones).

## Roadmap / future additions

Prioritized from the research (see sections 2–5 above):

### Near-term (small, high-impact)
1. ~~**CVI "letter/word bubbling"**~~ — **Done in 0.2.0** (thick contrast outline on letter/number
   tiles). Future: extend bubbling to word-level targets.
2. ~~**Haptic feedback**~~ — **Done in 0.2.0** (wrong-only, caregiver toggle).
3. ~~**Wire `scanDwellMillis` into an in-app scan pacing + setup slider**~~ — **Done in 0.3.0**
   (dwell-time slider exposed & persisted). Future: an in-app scan loop (0.3–5 s, acceptance
   delay, acknowledgement blip on each registered press); platform switch access drives
   scanning today.
4. ~~**Surface remaining profile fields** in setup: background choice, object scale,
   stimulus repetitions, learner name~~ — **Done in 0.3.0**.
5. **Caregiver option to hide levels** on the home hub (reduce choice / predictable
   routines; CVI array complexity).

### Mid-term
6. **Adaptive difficulty** — start at 1–2 options, grow/shrink with performance
   (iVision DDA RCT).
7. **Progress tracking** — per-level correct/wrong counters + simple caregiver summary
   (VisionLearn IEP tracking; swap `ProfileStore` for Room).
8. **Explicit visual-latency setting** — delay before stimulus removal/advance as a
   distinct CVI characteristic (Roman-Lantzy).

### Further levels (CCAS target domains)
9. **Figure-ground search** — target embedded in low-level clutter, increasing clutter
   (visuospatial domain; meta-analysis effect).
10. **Sequencing** — put 2–3 steps in order (executive domain; Vizzbu has sequencing).
11. **Working-memory pairs** — match-after-brief-delay (executive domain).
12. Expand letters (full alphabet, lower/uppercase) and counting (4–10).
13. **Movement-preferred targets** (CVI movement characteristic) with optional gentle
    motion, gated by the reduced-motion safety setting.
14. **Multi-profile support + caregiver analytics export** (Room; IEP documentation).
15. **Light/field preference** options (CVI light preference, visual-field preference).
16. **Automated tests + CI** (Compose UI tests, Lint) before any public release.

## License

MIT License. See the [LICENSE](LICENSE) file for the full text.

---

## Disclaimer

**This is a private project.** It was created privately, for a specific family
and a specific child, and does not represent a general medical, therapeutic,
or educational product, nor an official recommendation by any clinician,
institution, or organization.

**Not medical advice.** This application is a learning/engagement tool only. It
is not a medical device, does not diagnose, treat, or manage any condition, and
should never replace advice, assessment, or intervention from qualified
healthcare, therapy, vision, or education professionals.

**Not a substitute for professional guidance.** The design choices are informed
by publicly available research literature (see [Research background](#research-background)),
but they are made by a lay developer for one specific learner. Every child is
different, and what is appropriate here may be inappropriate, ineffective, or
harmful elsewhere. Please consult the child's own medical, vision, occupational,
speech, and education teams before using or adapting this.

**Seizure & sensory safety are the caregiver/clinical team's responsibility.**
While every effort is made to avoid flashing and rapid motion, the suitability
of this app for a child who may be photosensitive or seizure-prone must be
evaluated by that child's own medical/clinical team, on a case-by-case basis,
before use.

**No liability.** This software is provided on an "AS IS" and "AS AVAILABLE"
basis, without warranty of any kind, express or implied, including but not
limited to the warranties of merchantability, fitness for a particular purpose,
and noninfringement. In no event shall the author or copyright holder be liable
for any claim, damages, or other liability (whether in an action of contract,
tort, or otherwise) arising from, out of, or in connection with the software or
its use, including any injury, harm, loss of data, or other consequences
relating to its use or misuse, by the original user or by any third party.

**Generative AI was used.** This project was substantially created with the
assistance of generative AI (an AI coding assistant) under the direction and
review of the human author. AI-generated and AI-assisted code and text may
contain errors, and output should always be reviewed by qualified humans.

**No warranty of correctness or completeness.** The source code, documentation,
and research summaries are provided without guarantee of accuracy,
completeness, or fitness for any purpose. The research links are provided for
convenience; the author does not endorse or verify the content of any external
site, and external content may change, disappear, or be inaccurate.

By using, copying, modifying, or distributing this project you acknowledge that
you do so at your own risk and agree to all of the above terms.

