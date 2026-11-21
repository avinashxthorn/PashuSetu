# PashuSetu — native Android field edition

PashuSetu 2.0 is an offline Android livestock register. Its UI is implemented using native Android views in Java, not a hosted website or WebView. No internet permission is requested.

## Download and install

Open [GitHub Actions](https://github.com/avinashxthorn/PashuSetu/actions). Select the latest successful **Build and test Android APK** run. Download **PashuSetu-APK** under Artifacts and extract PashuSetu.apk. The **Native-screen-QA** artifact contains emulator screenshots and the test result.

This is a debug-signed evaluation APK (Android 8.0+), not a Play Store release. Debug signing keys may differ across builds. Before upgrading from the previous version, export a JSON backup from the old app, install the new version, and restore that backup in Settings. The new native storage does not automatically migrate the old WebView database.

## Features
- Native dashboard, animal passports, history, individual growth and milk charts, species and health filters.
- Three-step registration; record health, vaccination, deworming, breeding, feed, production and financial entries.
- Due-date reminders calculated from saved records, bilingual navigation and core fields.
- Atomic app-private register storage, native backup export and validated restore.
- Actual database read/write indicators (no artificial delay).

## Build
JDK 17, Gradle 8.9, Android SDK 35: `gradle :app:assembleDebug :app:assembleDebugAndroidTest`.
APK: app/build/outputs/apk/debug/app-debug.apk.

## Limits
Evaluation build. No cloud sync, account authentication, automatic background alerts or Play Store signing. Some dropdown values and errors remain English; clinical decisions require a veterinarian. Device testing is still necessary. The earlier index.html is retained for historical source reference only; it is not bundled in the native APK.
