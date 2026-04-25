# Progress

## 2026-04-25

- Inspected the repo. It contained only empty documentation files and no Android or Gradle project files.
- Created a minimal native Android project scaffold for Friction Wellbeing.
- Implemented a Compose MVP with:
  - Placeholder dashboard.
  - Launchable app selection using `PackageManager`.
  - Usage Access explanation and Android settings shortcut.
  - Basic settings screen with a default 15 minute daily limit.
  - Local persistence using `SharedPreferences`.
- Repaired the missing Gradle wrapper by downloading Gradle 8.10.2 to `/tmp` and generating `gradlew`, `gradlew.bat`, and `gradle/wrapper` files.
- Ran `./gradlew build`.
- First build failed because `kotlin { jvmToolchain(17) }` requested a JDK 17 installation, while this machine has JDK 21.
- Removed the explicit JDK 17 toolchain request and kept Kotlin bytecode targeting JVM 17.
- Second build failed because no Android SDK location is configured. Gradle requires `ANDROID_HOME` or `sdk.dir` in `local.properties`.
- Reviewed the uncommitted MVP changes and found no implementation issue requiring changes before the first commit.
- Re-ran `./gradlew build`; it still fails because no Android SDK location is configured.
- Added UsageStatsManager-based reading for today's selected-app foreground usage.
- Updated the dashboard to show selected-app usage minutes, total minutes, and below-limit / at-or-over daily limit status.
- Added graceful dashboard handling for the permission-denied state with a path to the Usage Access screen.
- Added dependency-free usage limit logic in `UsageLimitCalculator` and a plain Java test runner wired into Gradle `check`.
- Verified the usage limit test directly with `javac` and `java` because the Android build cannot configure without an SDK.
- Re-ran `./gradlew build`; it still fails before compilation because no Android SDK location is configured.
- Installed Android command-line tools and SDK packages under `~/Android/Sdk`, then added local `sdk.dir` in untracked `local.properties`.
- Applied safe review fixes: targeted `PACKAGE_USAGE_STATS` lint suppression, privacy wording, at-or-over limit wording, `HorizontalDivider`, and `mutableIntStateOf`.
- Ran `./gradlew build`; build passed with lint and the temporary usage limit test.
