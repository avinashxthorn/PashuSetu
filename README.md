# PashuSetu 3.0 — Compose edition

Native Android application for rural Indian livestock records. The Compose edition is developed on `compose-v3`; the previous 2.0 APK remains available from main.

## Design and architecture
- Kotlin + Jetpack Compose, Material 3 typography, colors and navigation; system splash and actual asynchronous loading/saving states.
- Offline-first Room/SQLite, separate repository and ViewModel/StateFlow layer, relational animal and care entities with foreign keys, transaction validation, and atomic backup restore.
- Guided registration, searchable animal passports, individual growth and milk history, care records, farm finance, date-filtered analytics, English/Hindi core UI.
- Legacy native 2.0 JSON data is imported once on first launch. Export an old backup before updating; debug signing keys can differ. WebView version backups can be imported from Settings.

## Build and quality gate
Use the `compose-v3` GitHub Actions workflow **PashuSetu 3 — Compose APK and device QA**. An installable debug APK is uploaded only after assembly and signature verification; the overall run succeeds only after Android emulator tests and screenshot collection. Open the successful run and download `PashuSetu-3.0-APK`; actual screenshots are in `Compose-device-QA`.

## Limits
Evaluation build; no account login, cloud sync, automatic background alerts or Play Store signing. Clinical decisions require a veterinarian. Some option labels/errors remain English. Physical-device usability and durability testing remain necessary.
