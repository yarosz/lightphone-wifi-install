#!/usr/bin/env bash
# Build and sign the Unsideloader APK with the Android SDK's own tools (no Gradle, no libraries).
#
#   unsideloader/build.sh [out.apk]
#
# Needs ANDROID_HOME (or ~/Library/Android/sdk) with build-tools and platform 34+, and a JDK. Signs with
# UNSIDELOADER_KEYSTORE (default ~/.config/lightphone-keys/unsideloader.jks, alias unsideloader); the password
# comes from UNSIDELOADER_KEYSTORE_PASS or, on macOS, the Keychain item lightphone-unsideloader-keystore.
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
out=${1:-$here/build/unsideloader.apk}
sdk=${ANDROID_HOME:-$HOME/Library/Android/sdk}
bt=$(ls -d "$sdk"/build-tools/* | sort -V | tail -1)
jar=$(ls -d "$sdk"/platforms/android-* | sort -V | tail -1)/android.jar
ks=${UNSIDELOADER_KEYSTORE:-$HOME/.config/lightphone-keys/unsideloader.jks}
pass=${UNSIDELOADER_KEYSTORE_PASS:-$(security find-generic-password -a unsideloader -s lightphone-unsideloader-keystore -w)}

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/classes" "$work/dex" "$(dirname "$out")"
javac --release 11 -nowarn -cp "$jar" -d "$work/classes" $(find "$here/src" -name '*.java')
"$bt/d8" --release --lib "$jar" --min-api 26 --output "$work/dex" $(find "$work/classes" -name '*.class')
"$bt/aapt2" link -o "$work/unsigned.apk" --manifest "$here/AndroidManifest.xml" -I "$jar"
(cd "$work/dex" && zip -q "$work/unsigned.apk" classes.dex)
"$bt/zipalign" -f 4 "$work/unsigned.apk" "$work/aligned.apk"
"$bt/apksigner" sign --ks "$ks" --ks-key-alias unsideloader --ks-pass "pass:$pass" --out "$out" "$work/aligned.apk"
"$bt/apksigner" verify "$out"
echo "$out"
shasum -a 256 "$out" | awk '{print "sha256 " $1}'
