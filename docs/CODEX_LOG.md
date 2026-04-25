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
- In the initial MVP commit, Usage Access was only explained and checked; usage reading and enforcement were not implemented yet.

## 2026-04-25 Usage Tracking Slice

### Prompt Summary

Continue the MVP by adding UsageStatsManager usage tracking for selected apps, Usage Access permission handling, dashboard usage minutes and limit status, a unit test for usage limit logic, documentation updates, and a `./gradlew build` run. Do not add dependencies, notification reading, launcher mode, pull requests, or pushes.

### Files Changed

- `README.md`
- `app/build.gradle.kts`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/main/java/com/frictionwellbeing/app/UsageLimitCalculator.java`
- `app/src/test/java/com/frictionwellbeing/app/UsageLimitCalculatorTest.java`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `git diff --check` passed.
- The dependency-free usage limit test passed when compiled and run directly with `javac` and `java`.
- `./gradlew build` failed before compilation with:

```text
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file at '/home/shan/friction/local.properties'.
```

### Known Issues

- Android SDK API 35 must be installed and discoverable via `ANDROID_HOME` or an untracked `local.properties` file before Gradle can compile or run checks.
- Usage stats are read for display only; limit enforcement is not implemented.

## 2026-04-25 Safe Review Fixes

### Prompt Summary

Apply only safe review fixes after the UsageStats dashboard review: target the `PACKAGE_USAGE_STATS` lint failure, clarify privacy wording, align limit wording with equality meaning limit reached, replace deprecated Compose APIs, document the temporary test approach, run `./gradlew build`, and do not add dependencies, change scope, commit, push, or create a pull request.

### Files Changed

- `README.md`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, and the temporary dependency-free usage limit test.

### Known Issues

- Usage stats are read for display only; limit enforcement is not implemented.
- The usage limit test runner is temporary until a standard Android/JUnit test setup is added.
- Gradle reports deprecated Gradle features from the current build setup; this should be reviewed before moving to Gradle 9.
