#!/usr/bin/env bash
#
# Sync the Android 7 side branch with main, rebuild it, and deliver a fresh APK.
#
# Usage:
#   scripts/sync-android7.sh            # merge main -> branch, build, deliver APK
#   scripts/sync-android7.sh --push     # ...and also commit + push the branch
#
# The branch keeps main's history and adds only what Android 7 (API 24) needs:
# minSdk 24 and legacy launcher icons. This script merges the latest main into it,
# re-applies those overrides, and stamps a variant version (<main>-android7) so the
# two APKs are easy to tell apart.
#
# Env knobs (override for your machine):
#   MAX_WORKERS=8
#   GRADLE_MEM_ARGS="-Xmx4g -Dfile.encoding=UTF-8"
#   APK_OUT_DIR=/some/dir
#
set -euo pipefail

BRANCH="android7-support"
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO"

PUSH=0
[ "${1:-}" = "--push" ] && PUSH=1

MAX_WORKERS="${MAX_WORKERS:-2}"
GRADLE_MEM_ARGS="${GRADLE_MEM_ARGS:--Xmx1600m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8}"
KOTLIN_MEM_ARGS="${KOTLIN_MEM_ARGS:--Xmx900m}"
APK_OUT_DIR="${APK_OUT_DIR:-$HOME}"

say() { printf '\n==> %s\n' "$1"; }

# ---------------------------------------------------------------- preflight
if [ -n "$(git status --porcelain)" ]; then
    echo "!! Working tree has uncommitted changes — commit or stash them first." >&2
    git status --short >&2
    exit 1
fi

say "Fetching origin"
git fetch origin --prune

MAIN_VERSION="$(git show origin/main:app/build.gradle.kts |
    sed -n 's/.*versionName = "\([^"]*\)".*/\1/p')"
if [ -z "$MAIN_VERSION" ]; then
    echo "!! Could not read versionName from origin/main." >&2
    exit 1
fi
VARIANT_VERSION="${MAIN_VERSION}-android7"

say "Checking out $BRANCH"
git checkout "$BRANCH" >/dev/null
git pull --ff-only origin "$BRANCH" >/dev/null
TIP_BEFORE="$(git rev-parse HEAD)"

# ---------------------------------------------------------------- merge main
say "Merging origin/main ($MAIN_VERSION) into $BRANCH"
if ! git merge --no-edit origin/main; then
    conflicted="$(git diff --name-only --diff-filter=U)"
    # build.gradle.kts: take main's, then re-apply our Android 7 overrides below.
    # README.md: take main's, so its fresh changelog survives.
    unexpected="$(printf '%s\n' "$conflicted" | grep -vx 'app/build.gradle.kts' | grep -vx 'README.md' || true)"
    if [ -n "$unexpected" ]; then
        echo "!! Conflicts outside build.gradle.kts/README.md — resolve manually:" >&2
        printf '%s\n' "$unexpected" >&2
        exit 1
    fi
    printf '%s\n' "$conflicted" | grep -qx 'app/build.gradle.kts' &&
        { git checkout --theirs app/build.gradle.kts; git add app/build.gradle.kts; }
    printf '%s\n' "$conflicted" | grep -qx 'README.md' &&
        { git checkout --theirs README.md; git add README.md; }
    GIT_EDITOR=true git merge --continue --no-edit >/dev/null
    echo "   (resolved build.gradle.kts / README.md automatically)"
fi
TIP_AFTER_MERGE="$(git rev-parse HEAD)"

# ------------------------------------------------- re-apply Android 7 overrides
say "Applying Android 7 overrides (minSdk 24, version $VARIANT_VERSION)"
sed -i 's/^\([[:space:]]*\)minSdk = .*/\1minSdk = 24/' app/build.gradle.kts
sed -i "s/^\([[:space:]]*\)versionName = .*/\1versionName = \"$VARIANT_VERSION\"/" app/build.gradle.kts
sed -i "s/^\(| Version[[:space:]]*|\)[^|]*\(|\)$/\1 \`$VARIANT_VERSION\` \2/" README.md
grep -q "versionName = \"$VARIANT_VERSION\"" app/build.gradle.kts ||
    { echo "!! versionName override failed — check the sed pattern." >&2; exit 1; }
grep -q "| Version .*| \`$VARIANT_VERSION\` |" README.md ||
    echo "   (warning: README version table not updated — check its formatting)"

# ---------------------------------------------------------------- build
say "Building debug APK (this can take several minutes)"
./gradlew assembleDebug --console=plain \
    --max-workers="$MAX_WORKERS" \
    -Dorg.gradle.jvmargs="$GRADLE_MEM_ARGS" \
    -Dkotlin.daemon.jvmargs="$KOTLIN_MEM_ARGS"

APK="app/build/outputs/apk/debug/app-debug.apk"
[ -f "$APK" ] || { echo "!! APK not found at $APK" >&2; exit 1; }

# ---------------------------------------------------------------- verify + deliver
if command -v aapt2 >/dev/null 2>&1; then
    badging="$(aapt2 dump badging "$APK" 2>/dev/null || true)"
    echo "$badging" | grep -E "^minSdkVersion|^package:" | sed 's/^/   /'
    echo "$badging" | grep -q "minSdkVersion:'24'" ||
        { echo "!! minSdkVersion is not 24 — aborting." >&2; exit 1; }
    echo "$badging" | grep -q "versionName='$VARIANT_VERSION'" ||
        echo "   (warning: versionName in APK does not match $VARIANT_VERSION)"
fi

DEST="$APK_OUT_DIR/sternchen-$VARIANT_VERSION.apk"
cp "$APK" "$DEST"
say "APK delivered: $DEST"

# ---------------------------------------------------------------- commit / push
if git diff --quiet; then
    if [ "$TIP_BEFORE" = "$TIP_AFTER_MERGE" ]; then
        say "Already up to date: $BRANCH matches main ($MAIN_VERSION)"
    else
        say "Merged main into $BRANCH; Android 7 overrides already correct"
    fi
else
    git add app/build.gradle.kts README.md
    git commit -m "Sync main ($MAIN_VERSION) into $BRANCH (Android 7, $VARIANT_VERSION)" >/dev/null
    say "Committed: $(git log --oneline -1)"
    if [ "$PUSH" -eq 1 ]; then
        git push origin "$BRANCH"
        say "Pushed $BRANCH to origin"
    else
        say "Not pushed. Re-run with --push, or: git push origin $BRANCH"
    fi
fi
