# Rahnama Android

Rahnama is an installable Android app for Afghan real-estate management. The initial UI uses Persian/Dari and right-to-left layout. Room provides local SQLite storage on the phone.

## Technology
- Kotlin, Jetpack Compose and Material 3
- Room local database
- Gradle Kotlin DSL
- Minimum Android API 26

## Current starter scope
- RTL dashboard
- Property, customer request, and contacts navigation
- Initial Room data entities and DAO
- First-pass matching engine and tests
- Sale, rent, and growy data fields, including separate monthly rent
- No online payment functionality

## Build the APK
1. Install Android Studio and the Android SDK.
2. Clone this repository and open it in Android Studio.
3. Check out branch `android-apk-foundation`.
4. Wait for Gradle sync, then choose **Build > Build APK(s)**.
5. The debug APK usually appears at `app/build/outputs/apk/debug/app-debug.apk`.

## Important limitations
This is an early foundation, not a completed production app. Entry dialogs currently demonstrate fields but do not save data. Room models and the matching engine exist but have not yet been connected to the UI. SMS sending, export/backup, cloud synchronization, and automated release APK signing are not implemented. Please build and test in Android Studio before installing on a phone.
