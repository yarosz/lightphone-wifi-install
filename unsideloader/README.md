# Uninstall (Unsideloader)

A tiny Android app for the Light Phone III that removes apps installed outside Light's Tool Library, which on
LightOS 582 neither the phone nor Light's dashboard can remove.

It lists every installed app that isn't part of Android, LightOS, or one of Light's own downloadable Tools
(`com.thelightphone.*`), sorted by name, with its package name underneath. Tapping one opens Android's own
"Do you want to uninstall this app?" prompt; nothing is removed until you tap OK there. It lists itself last.

- Package `io.github.yarosz.unsideloader`, shown as **Uninstall** in the Tools list.
- Permissions: `QUERY_ALL_PACKAGES` (to list the apps) and `REQUEST_DELETE_PACKAGES` (to open Android's
  prompt). It can't remove anything without your confirmation, and it has no network access.
- Plain Java, no libraries, one screen: [`MainActivity.java`](src/io/github/yarosz/unsideloader/MainActivity.java).
- Tested on an LP3 with LightOS 582 and on the SDK emulator (Android 14).

Install it with the LP3 Tool Installer (it offers it), or with adb:
`adb install unsideloader-1.0.0.apk`.

## Building

`unsideloader/build.sh [out.apk]` compiles and signs it with the Android SDK's own tools (javac, d8, aapt2,
zipalign, apksigner); no Gradle. It needs the Android SDK and a JDK. Releases are signed with the maintainer's
key; a build signed with another key can't update an installed copy, so uninstall it first.
