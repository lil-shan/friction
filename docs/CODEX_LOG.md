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

## 2026-04-25 Friction Preparation Slice

### Prompt Summary

Implement the next MVP slice for friction screen preparation: add a Compose friction screen with a 10-second countdown, intention prompt, disabled continue button until countdown and prompt are complete, cancel back to dashboard, demo path from dashboard for selected apps at or over the daily limit, at least one dependency-free state logic test, documentation updates, and `./gradlew build`. Do not add dependencies, notification reading, launcher mode, commits, or pushes.

### Files Changed

- `README.md`
- `app/build.gradle.kts`
- `app/src/main/java/com/frictionwellbeing/app/FrictionStateCalculator.java`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/test/java/com/frictionwellbeing/app/FrictionStateCalculatorTest.java`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, the usage limit logic test, and the friction state logic test.

### Known Issues

- The friction flow is demo-only. It does not launch, block, or replace selected apps.
- Friction intention prompt text is not persisted.
- The friction state test runner is temporary until a standard Android/JUnit test setup is added.

## 2026-04-25 Product Direction Documentation

### Prompt Summary

Update documentation only to reflect the new product direction with three major modes: Core Mode, optional Launcher Mode, and optional experimental Overlay Blocker Mode. Document Launcher Mode constraints, Overlay Blocker risks and opt-in requirements, and the current implementation order. Do not change app code, add dependencies, implement launcher mode, implement AccessibilityService, implement overlay blocker, commit, or push.

### Files Changed

- `README.md`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed after the documentation-only update.
- Gradle ran Android compilation, lint, standard unit test tasks, the usage limit logic test, and the friction state logic test.

### Known Issues

- Launcher Mode is not implemented.
- Overlay Blocker Mode is not implemented.
- Notification inbox is not implemented.
- Overlay Blocker Mode would require careful opt-in disclosure and may be risky for Google Play review.

## 2026-04-25 Reusable Friction Screen Slice

### Prompt Summary

Implement the reusable friction screen so it can later be used by dashboard demo, Launcher Mode, and Overlay Blocker Mode. Requirements include a Compose friction screen, 10-second countdown, intention prompt, disabled continue button until countdown completion and non-blank text, cancel back to the previous screen, dashboard demo path for selected apps at or over the daily limit, dependency-free friction state logic and test, documentation updates, and `./gradlew build`. Do not add dependencies, implement Launcher Mode, implement AccessibilityService, implement Overlay Blocker Mode, implement notification reading, commit, or push.

### Files Changed

- `README.md`
- `app/build.gradle.kts`
- `app/src/main/java/com/frictionwellbeing/app/FrictionStateCalculator.java`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/test/java/com/frictionwellbeing/app/FrictionStateCalculatorTest.java`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, the usage limit logic test, and the friction state logic test.

### Known Issues

- The dashboard path is still a demo path; it does not launch or block target apps.
- Launcher Mode is not implemented.
- Overlay Blocker Mode, AccessibilityService, and overlay permission are not implemented.
- Notification inbox is not implemented.
- Friction intention prompt text is not persisted.

## 2026-04-25 Overlay Blocker Enforcement Slice

### Prompt Summary

Implement the next real app slice for opt-in full friction enforcement, starting with Instagram. Improve the dashboard, add Overlay Blocker settings, add AccessibilityService foreground app detection, add overlay permission handling, show a friction overlay when selected target apps are at or over the daily limit or strict mode is enabled, add cooldown handling, add dependency-free tests, update docs, and run `./gradlew build`. Do not implement Reels/Shorts detection, notification reading, network blocking, launcher mode, new dependencies, commits, pushes, or pull requests.

### Files Changed

- `README.md`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/frictionwellbeing/app/AppSettings.kt`
- `app/src/main/java/com/frictionwellbeing/app/FrictionStateCalculator.java`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/main/java/com/frictionwellbeing/app/OverlayBlockerAccessibilityService.kt`
- `app/src/main/java/com/frictionwellbeing/app/OverlayFrictionEligibility.java`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/xml/overlay_blocker_accessibility_service.xml`
- `app/src/test/java/com/frictionwellbeing/app/FrictionStateCalculatorTest.java`
- `app/src/test/java/com/frictionwellbeing/app/OverlayFrictionEligibilityTest.java`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PRIVACY.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- First `./gradlew build` failed after the UI refactor because `FrictionPreferences` did not retain its constructor `Context`, and `DashboardCard` used the `Column` composable as a receiver type instead of `ColumnScope`.
- Fixed once with a targeted Kotlin patch.
- Second `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, and the temporary dependency-free usage limit, friction state, and overlay eligibility tests.

### Known Issues

- Overlay Blocker Mode is experimental and must be tested on physical Android devices.
- It currently detects selected apps by package name only; it does not detect Reels or Shorts specifically.
- Instagram support starts at package-level blocking for `com.instagram.android`.
- Overlay behavior may vary by Android version and OEM.
- The overlay uses sensitive AccessibilityService and Display over other apps capabilities and may be risky for Google Play review.
- Usage durations and intention prompt text are not persisted.

## 2026-04-25 Overlay UI and Repeat Tuning

### Prompt Summary

Make the overlay text more interesting with geography, puzzle, and focus prompts; move the app and overlay toward a dark modern UI; and make the overlay repeat sooner, around every 2 minutes.

### Files Changed

- `README.md`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/main/java/com/frictionwellbeing/app/OverlayBlockerAccessibilityService.kt`
- `app/src/main/java/com/frictionwellbeing/app/OverlayFrictionEligibility.java`
- `app/src/test/java/com/frictionwellbeing/app/OverlayFrictionEligibilityTest.java`
- `docs/ARCHITECTURE.md`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/PROGRESS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, and the temporary dependency-free usage limit, friction state, and overlay eligibility tests.

### Known Issues

- Overlay UI still uses Android Views because it runs from the accessibility service overlay, while the main app uses Compose.

## 2026-04-25 Challenge Validation

### Prompt Summary

Update the friction overlay toward the expected dark polished UI direction and make challenge questions actually validated before Continue is enabled.

### Files Changed

- `README.md`
- `app/build.gradle.kts`
- `app/src/main/java/com/frictionwellbeing/app/FrictionChallenge.java`
- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt`
- `app/src/main/java/com/frictionwellbeing/app/OverlayBlockerAccessibilityService.kt`
- `app/src/test/java/com/frictionwellbeing/app/FrictionChallengeTest.java`
- `docs/CODEX_LOG.md`
- `docs/DECISIONS.md`
- `docs/ROADMAP.md`

### Build/Test Result

- `./gradlew build` passed.
- Gradle ran Android compilation, lint, standard unit test tasks, and the temporary dependency-free usage limit, friction state, overlay eligibility, and friction challenge tests.

### Known Issues

- Challenge validation is intentionally simple text matching, not a full quiz engine.
- Overlay UI still uses Android Views because it runs from the accessibility service overlay, while the main app uses Compose.
