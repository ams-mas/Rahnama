# Rahnama Android

Rahnama is a native Android app for managing real-estate listings and customer requests for Afghan real-estate agencies. The interface is Persian/Dari and right-to-left. The app uses Room/SQLite to store records locally on the phone. No online payment processing is included.

## Project structure

```text
Rahnama/
├── .github/
│   └── workflows/
│       └── android-build.yml       # Builds debug APK in GitHub Actions
├── app/
│   ├── build.gradle.kts             # Android app dependencies and SDK settings
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/amsmas/rahnama/
│       │       ├── HomeActivity.kt  # Main Persian RTL app UI and forms
│       │       ├── MainActivity.kt  # Earlier starter screen
│       │       └── data/
│       │           ├── Entities.kt  # Room entities: people, properties, requests
│       │           ├── RahnamaDao.kt
│       │           ├── RahnamaDatabase.kt
│       │           ├── MatchingEngine.kt
│       │           └── PropertyCodeGenerator.kt
│       └── test/
│           └── java/com/amsmas/rahnama/data/
│               └── MatchingEngineTest.kt
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── build.gradle.kts                 # Root Gradle plugins
├── gradle.properties
├── gradlew                          # Linux/macOS Gradle wrapper
├── gradlew.bat                      # Windows Gradle wrapper
├── settings.gradle.kts
└── README.md
```

## Open on Windows in Android Studio

1. Clone this repository and branch `android-apk-foundation`.
2. In Android Studio, choose **Open** and select the `Rahnama` project folder.
3. Use JDK 17 and let Gradle sync complete.
4. Select **Build → Build APK(s)** or run `gradlew.bat assembleDebug` in the project terminal.
5. The debug APK is normally created at `app/build/outputs/apk/debug/app-debug.apk`.

Clone command:

```powershell
git clone -b android-apk-foundation https://github.com/ams-mas/Rahnama.git
```

To update an existing clone:

```powershell
git switch android-apk-foundation
git pull origin android-apk-foundation
```

## Included starter functionality

- Persian/Dari RTL dashboard
- Local database for people, property listings, and customer requests
- System-generated property codes
- Sale, rent, and growy listing types, with a separate monthly rent field
- First-pass matching between active requests and available properties
- Debug APK build workflow

## Known limitations

This is an early development build. Validate the build and test the screens on a real Android device before entering important data. Growy matching rules need validation against the agency's actual practices. SMS integration, cloud synchronization, backup/restore, edit/delete workflows, and release signing are not yet implemented.
