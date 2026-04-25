# Codex Log

## 2026-04-25

### Prompt Summary

Build the initial Android MVP for Friction Wellbeing after documentation initialization. If no Android project exists, scaffold a minimal native Kotlin + Jetpack Compose project and implement app selection, Usage Access explanation, settings, local storage, simple navigation, and a placeholder dashboard.

### Files Changed

- `.gitignore`
- `AGENTS.md`
- `README.md`
- `build.gradle.kts`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`
- `settings.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/styles.xml`
- `docs/ARCHITECTURE.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` initially failed because the generated project requested a Java 17 toolchain and this machine only exposes JDK 21.
- Fixed once by removing `kotlin { jvmToolchain(17) }` and setting `kotlinOptions.jvmTarget = "17"`.
- `./gradlew build` then failed with:

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file at '/home/shan/friction/local.properties'.
```
- During review, `./gradlew build` was run again and failed with the same Android SDK location error before Kotlin/Android compilation began.

### Known Issues

- Android SDK API 35 must be installed and discoverable via `ANDROID_HOME` or an untracked `local.properties` file.
- Usage Access is only explained and checked; no usage reading or enforcement is implemented yet.
