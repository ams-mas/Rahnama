# Rahnama Android

Rahnama is an installable Android app for Afghan real-estate management. The interface is in Persian/Dari with right-to-left layout. Room stores people, properties and customer requests locally on the phone, so the basic system works offline.

## Technology
- Kotlin, Jetpack Compose and Material 3
- Room local SQLite database
- Gradle Kotlin DSL
- Minimum Android API 26

## Implemented in the current foundation
- RTL dashboard with live counts from local records
- Create and list property records
- System-generated property codes
- Sale, rent and growy transaction types
- Separate price/growy principal and monthly-rent fields
- Create and list contacts and customer requests
- First-pass matching of active requests with available properties
- No online payments or payment gateway

## Build the debug APK
1. Install Android Studio and the Android SDK.
2. Clone this repository and open the project in Android Studio.
3. Check out branch `android-apk-foundation`.
4. Wait for Gradle sync and select **Build > Build APK(s)**.
5. The debug APK normally appears at `app/build/outputs/apk/debug/app-debug.apk`.

A GitHub Actions workflow also attempts to build the debug APK and attach it as a workflow artifact. Check the [Actions page](https://github.com/ams-mas/Rahnama/actions?query=branch%3Aandroid-apk-foundation) for the latest run status.

## Not yet implemented / limitations
- SMS provider integration and delivery status
- Cloud sync across multiple devices
- Export/restore backup workflow
- Edit/delete property and contact flows
- Release signing and production deployment
- The current first-pass growy matching rules should be validated against the actual office rules

This is an early development build, not yet a production-ready property-management system. Build and test it on a real phone before entering important customer data.
