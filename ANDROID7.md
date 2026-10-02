# Android 7 side branch (`android7-support`)

`main` is the primary line of development and targets **Android 8.0+ (`minSdk` 26)**.
This branch is the **Android 7 compatible variant** (`minSdk` 24 = Android 7.0).

It differs from `main` in exactly two ways:

| Change | Why |
|---|---|
| `minSdk = 24` | Lets the app install and run on Android 7.0/7.1 |
| Legacy PNG launcher icons (`mipmap-*dpi`) | Adaptive icons only exist from API 26; below that the icon has to be a bitmap |

Everything else — all levels, settings, accessibility behaviour — is identical to `main`.
It is **not** merged back into `main`; it is maintained as a parallel line that tracks `main`.

## Keeping it in sync

When `main` moves ahead, run:

```bash
scripts/sync-android7.sh          # merge main -> branch, rebuild, deliver APK
scripts/sync-android7.sh --push   # ...and commit + push the branch
```

The script:

1. refuses to run with a dirty working tree;
2. fetches `origin` and reads `main`'s `versionName`;
3. checks out `android7-support` and merges `origin/main` into it
   (auto-resolving the expected conflicts in `app/build.gradle.kts` and `README.md`,
   and stopping for a human on anything unexpected);
4. re-applies the two Android 7 overrides and stamps a **variant version**
   `<main version>-android7` (e.g. main `0.5.0` → `0.5.0-android7`);
5. builds the debug APK, asserts `minSdkVersion 24` in the built APK, and copies it to
   `~/sternchen-<version>-android7.apk`;
6. commits, and pushes only when `--push` is given.

So the routine after every `main` release is one command, and each run produces a
freshly built, distinctly versioned Android 7 APK.

### Machine-specific build settings

The defaults are tuned for a small/low-RAM machine (large Gradle heaps can deadlock
the dex merge). Override via environment variables on a bigger machine:

```bash
MAX_WORKERS=8 GRADLE_MEM_ARGS="-Xmx4g -Dfile.encoding=UTF-8" scripts/sync-android7.sh --push
```

| Variable | Default |
|---|---|
| `MAX_WORKERS` | `2` |
| `GRADLE_MEM_ARGS` | `-Xmx1600m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8` |
| `KOTLIN_MEM_ARGS` | `-Xmx900m` |
| `APK_OUT_DIR` | `$HOME` |

### Fully automatic syncing (optional)

To sync the branch every time you commit on `main`, install a git hook:

```bash
printf '#!/bin/sh\nexec "$HOME/sternchen/scripts/sync-android7.sh" --push\n' \
  > .git/hooks/post-commit && chmod +x .git/hooks/post-commit
```

The hook is local-only (not committed) and fires on every commit on any branch, so it
is worth keeping the workflow explicit instead — run `scripts/sync-android7.sh --push`
after a `main` release.

## Notes

- Haptics needed no change: `access/Haptics.kt` already falls back to the legacy
  vibration API below API 26.
- Compose falls back to software rendering below API 29, so the app is slower on
  Android 7 than on newer devices — the layout is unchanged, only the frame rate
  differs.
- Verified on a physical Android 7 device: installs, launches and plays.
