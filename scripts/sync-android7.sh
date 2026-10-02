#!/bin/sh
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
# Written for POSIX sh (no bashisms) so it runs on Termux, Linux and macOS.
# NOTE: sed -i is GNU-style here (Termux/Linux); on macOS use `brew install gnu-sed`.
#
# Env knobs (override for your machine):
#   MAX_WORKERS=8
#   GRADLE_MEM_ARGS="-Xmx4g -Dfile.encoding=UTF-8"
#   APK_OUT_DIR=/some/dir
#   MAIN_REF=origin/main   # the ref this branch tracks
#
set -eu

BRANCH="android7-support"
MAIN_REF="${MAIN_REF:-origin/main}"   # override to test against a local branch
REPO="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO"

PUSH=0
if [ "${1:-}" = "--push" ]; then PUSH=1; fi

MAX_WORKERS="${MAX_WORKERS:-2}"
GRADLE_MEM_ARGS="${GRADLE_MEM_ARGS:--Xmx1600m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8}"
KOTLIN_MEM_ARGS="${KOTLIN_MEM_ARGS:--Xmx900m}"
APK_OUT_DIR="${APK_OUT_DIR:-$HOME}"

say() { printf '\n==> %s\n' "$1"; }
has() { printf '%s\n' "$1" | grep -qx "$2"; }

# ---------------------------------------------------------------- preflight
if [ -n "$(git status --porcelain)" ]; then
    echo "!! Working tree has uncommitted changes — commit or stash them first." >&2
    git status --short >&2
    exit 1
fi

say "Fetching origin"
git fetch origin --prune

MAIN_VERSION="$(git show "$MAIN_REF":app/build.gradle.kts |
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

# Never let a sync move the branch's version backwards: if the branch is already
# ahead of main (e.g. main was renamed or reverted), keep the existing version.
# Compares dotted MAJOR.MINOR.PATCH numerically -- [ -gt ] cannot, since these
# are not integers.
is_num() { case "$1" in '' | *[!0-9]*) return 1 ;; *) return 0 ;; esac; }
ver_gt() {
    a_major=${1%%.*}; a_rest=${1#*.}; a_minor=${a_rest%%.*}; a_patch=${a_rest#*.}
    b_major=${2%%.*}; b_rest=${2#*.}; b_minor=${b_rest%%.*}; b_patch=${b_rest#*.}
    is_num "$a_major" && is_num "$a_minor" && is_num "$a_patch" || return 1
    is_num "$b_major" && is_num "$b_minor" && is_num "$b_patch" || return 1
    if [ "$a_major" -gt "$b_major" ]; then return 0; fi
    if [ "$a_major" -lt "$b_major" ]; then return 1; fi
    if [ "$a_minor" -gt "$b_minor" ]; then return 0; fi
    if [ "$a_minor" -lt "$b_minor" ]; then return 1; fi
    if [ "$a_patch" -gt "$b_patch" ]; then return 0; fi
    return 1
}

CURRENT_VERSION="$(sed -n 's/.*versionName = "\([^"]*\)".*/\1/p' app/build.gradle.kts)"
CURRENT_NUM="${CURRENT_VERSION%%-*}"
if ver_gt "$CURRENT_NUM" "$MAIN_VERSION"; then
    VARIANT_VERSION="$CURRENT_VERSION"
    say "Branch is at $CURRENT_VERSION, ahead of 's $MAIN_VERSION — keeping it"
fi

# ---------------------------------------------------------------- merge main
say "Merging $MAIN_REF ($MAIN_VERSION) into $BRANCH"
if ! git merge --no-edit "$MAIN_REF"; then
    conflicted="$(git diff --name-only --diff-filter=U)"
    # build.gradle.kts: take main's, then re-apply our Android 7 overrides below.
    # README.md: take main's, so its fresh changelog survives.
    unexpected="$(printf '%s\n' "$conflicted" | grep -vx 'app/build.gradle.kts' | grep -vx 'README.md' || true)"
    if [ -n "$unexpected" ]; then
        echo "!! Conflicts outside build.gradle.kts/README.md — resolve manually:" >&2
        printf '%s\n' "$unexpected" >&2
        exit 1
    fi
    if has "$conflicted" 'app/build.gradle.kts'; then
        git checkout --theirs app/build.gradle.kts
        git add app/build.gradle.kts
    fi
    if has "$conflicted" 'README.md'; then
        git checkout --theirs README.md
        git add README.md
    fi
    GIT_EDITOR=true git merge --continue --no-edit >/dev/null
    echo "   (resolved build.gradle.kts / README.md automatically)"
fi
TIP_AFTER_MERGE="$(git rev-parse HEAD)"

# ------------------------------------------------- re-apply Android 7 overrides
say "Applying Android 7 overrides (minSdk 24, version $VARIANT_VERSION)"
sed -i 's/^\([[:space:]]*\)minSdk = .*/\1minSdk = 24/' app/build.gradle.kts
sed -i "s/^\([[:space:]]*\)versionName = .*/\1versionName = \"$VARIANT_VERSION\"/" app/build.gradle.kts
sed -i "s/^\(| Version[[:space:]]*|\)[^|]*\(|\)$/\1 \`$VARIANT_VERSION\` \2/" README.md
if ! grep -q "versionName = \"$VARIANT_VERSION\"" app/build.gradle.kts; then
    echo "!! versionName override failed — check the sed pattern." >&2
    exit 1
fi
if ! grep -q "\`$VARIANT_VERSION\`" README.md; then
    echo "   (warning: README version table not updated — check its formatting)"
fi

# ---------------------------------------------------------------- build
say "Building debug APK (this can take several minutes)"
./gradlew assembleDebug --console=plain \
    --max-workers="$MAX_WORKERS" \
    -Dorg.gradle.jvmargs="$GRADLE_MEM_ARGS" \
    -Dkotlin.daemon.jvmargs="$KOTLIN_MEM_ARGS"

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
    echo "!! APK not found at $APK" >&2
    exit 1
fi

# ---------------------------------------------------------------- verify + deliver
if command -v aapt2 >/dev/null 2>&1; then
    badging="$(aapt2 dump badging "$APK" 2>/dev/null || true)"
    printf '%s\n' "$badging" | grep -E '^minSdkVersion|^package:' | sed 's/^/   /'
    if ! printf '%s\n' "$badging" | grep -q "minSdkVersion:'24'"; then
        echo "!! minSdkVersion is not 24 — aborting." >&2
        exit 1
    fi
    if ! printf '%s\n' "$badging" | grep -q "versionName='$VARIANT_VERSION'"; then
        echo "   (warning: versionName in APK does not match $VARIANT_VERSION)"
    fi
fi

DEST="$APK_OUT_DIR/sternchen-$VARIANT_VERSION.apk"
cp "$APK" "$DEST"
say "APK delivered: $DEST"

# ---------------------------------------------------------------- commit / push
if git diff --quiet; then
    if [ "$TIP_BEFORE" = "$TIP_AFTER_MERGE" ]; then
        say "Already up to date: $BRANCH matches $MAIN_REF ($MAIN_VERSION)"
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
