# Friction Wellbeing

Friction Wellbeing is a native Android wellbeing app. The initial MVP lets a user choose target apps, set a default daily limit, and review the Usage Access permission flow.

## Current MVP

- Kotlin Android app with Jetpack Compose UI.
- Package name: `com.frictionwellbeing.app`.
- Minimum SDK: 26.
- Compile/target SDK: 35.
- Local-only settings with `SharedPreferences`.
- Installed launchable app list from `PackageManager`.
- Usage Access explanation screen with a button to open Android settings.
- Dashboard usage totals for selected apps when Usage Access is granted.
- Below-limit / at-or-over daily limit status against the configured daily limit.

## Build

This repo expects the standard Android toolchain:

- JDK 17.
- Android SDK with API 35 installed.

Build command:

```bash
./gradlew build
```

If the Android SDK is not discoverable, set `ANDROID_HOME` or add a local, untracked `local.properties` file:

```properties
sdk.dir=/path/to/Android/Sdk
```

At the time of verification in this workspace, `./gradlew build` is blocked because no Android SDK location is configured.
