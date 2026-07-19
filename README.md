# Expiry Manager

An Android app for tracking products (groceries, medicine, etc.) by expiry date.

## Features

- Main list of products sorted by expiry date, soonest first.
- Add/edit screen for name, type, quantity + unit, and expiry date.
- Barcode scanning (via [ZXing embedded](https://github.com/journeyapps/zxing-android-embedded))
  that remembers and pre-fills the last-used name/type/quantity/unit for a given barcode
  (the expiry date is always entered fresh, since it's per-batch).
- Export the full product list as a SQLite file through the Android share sheet.
- Import a previously exported SQLite file, replacing the current list (with validation,
  a safety backup of the current data, and rollback on failure).
- Daily expiry reminder notifications with a configurable lead time.

## Tech stack

- Java, minSdk 26 (Android 8.0+)
- MVVM: Activity/Fragment + ViewModel + LiveData + Repository
- [Room](https://developer.android.com/training/data-storage/room) for persistence
  (also doubles as the export/import file format — see below)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for
  the daily expiry check
- [zxing-android-embedded](https://github.com/journeyapps/zxing-android-embedded) for barcode
  scanning (no Google Play Services dependency)

## Building

```
./gradlew assembleDebug
```

Requires an Android SDK (`compileSdk`/`targetSdk` 34) available via `local.properties` or the
`ANDROID_HOME`/`ANDROID_SDK_ROOT` environment variable — set up automatically if you open the
project in Android Studio.

## Running tests

```
./gradlew connectedAndroidTest
```

Room DAO tests live under `app/src/androidTest` and run against an in-memory database on a
connected device/emulator.

## Project layout

```
app/src/main/java/bogdrosoft/expirymanager/
  data/            Room entities, DAOs, database, type converters
  repository/       ProductRepository — single source of truth for the UI layer
  ui/main/          Product list screen
  ui/addedit/       Add/edit product screen
  ui/settings/      Reminder lead-time and notification settings
  scan/             Barcode scanning helper (ZXing + CAMERA permission flow)
  export/           SQLite export (share sheet) and import (validate/backup/swap/rollback)
  reminder/         WorkManager expiry check, notifications, permission handling
  util/             Shared constants and SharedPreferences helpers
```

## Notes on export/import

The Room database uses `TRUNCATE` journal mode rather than the default `WAL`, so the on-disk
`.db` file is always a complete, single-file snapshot — no separate `-wal`/`-shm` files and no
manual checkpoint step needed before copying it for export.

Import validates the picked file's schema (via the raw SQLite API, not Room) before touching
anything, backs up the current database, and restores that backup automatically if the swap
fails for any reason. On success, the app relaunches itself in a fresh process — Room's
`LiveData` is bound to the connection it was created with, so this avoids stale in-memory state
after swapping the underlying file.
