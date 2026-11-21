# PashuSetu — पशुसेतु

Installable Android livestock and veterinary register (Android 8.0+). The app packages the existing interface into an offline WebView with native backup export and a launcher icon. No internet permission is required.

## Download the APK
Open [GitHub Actions](https://github.com/avinashxthorn/PashuSetu/actions), select Build Android APK, and open a successful run. Download PashuSetu-APK under Artifacts, unzip it, and install PashuSetu.apk on Android. If no run appears, use Run workflow. The APK is debug-signed for testing, not Play Store distribution.

## Build locally
Use JDK 17, Gradle 8.9 and Android SDK 35. Run `gradle :app:assembleDebug`. Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Features
Animal registration, editing and search; health, vaccination, breeding, production, feed and finance records; live charts; due-date reminders; partial Hindi localization; JSON and CSV export; sample data.

## Limitations
Prototype with device-local WebView storage. No login, sync, backup import or background alerts. Do not commit real owner data to this public repository. Clearing app data or uninstalling removes local records. Export regularly. Clinical decisions require a veterinarian. Device testing and production signing are still required.
