# Friction Wellbeing

Friction Wellbeing is a native Android wellbeing app for adding intentional friction before opening high-distraction apps. The product is now organized around three modes: Core Mode, optional Launcher Mode, and optional experimental Overlay Blocker Mode.

## Product Modes

### Core Mode

Core Mode is the default app experience:

- App selection.
- Usage tracking.
- Daily limits.
- Friction screen.
- Later notification inbox.

### Launcher Mode

Launcher Mode is an optional custom Android launcher:

- Shows installed apps.
- Target app icons can move or randomize positions inside Friction Wellbeing's launcher.
- Opening target apps triggers friction first.
- Non-target apps open normally.
- The app must not try to move icons on the user's existing launcher.

### Overlay Blocker Mode

Overlay Blocker Mode is an optional experimental mode:

- Uses `AccessibilityService` to detect selected target apps by foreground package name.
- Starts with package-level Instagram support for `com.instagram.android`.
- Uses Display over other apps permission to show a friction overlay.
- Detection is heuristic and not guaranteed.
- Instagram and YouTube UI changes may break detection.
- This may be risky for Google Play review.
- Must be clearly disclosed and opt-in.
- Must not claim perfect Reels/Shorts blocking.
- Does not detect Reels or Shorts specifically yet.

## Current Implementation

- Kotlin Android app with Jetpack Compose UI.
- Package name: `com.frictionwellbeing.app`.
- Minimum SDK: 26.
- Compile/target SDK: 35.
- Local-only settings with `SharedPreferences`.
- Installed launchable app list from `PackageManager`.
- Usage Access explanation screen with a button to open Android settings.
- Dashboard usage totals for selected apps when Usage Access is granted.
- Below-limit / at-or-over daily limit status against the configured daily limit.
- Reusable friction flow for selected apps that are at or over the daily limit.
- 10-second pause with an intention prompt before the continue action is enabled.
- Dashboard demo path for exercising the reusable friction flow before launcher or overlay modes exist.
- Cleaner dashboard cards for selected apps, usage, limit, friction status, and permission status.
- Optional Overlay Blocker settings screen with local on/off state, strict mode, and setup links for Usage Access, Accessibility, and Display over other apps.
- Experimental `AccessibilityService` that detects selected foreground target apps by package name.
- Experimental overlay friction prompt for selected target apps when the app is at or over the daily limit, or when strict overlay mode is enabled.
- Temporary local allow window after completing overlay friction: 2 minutes per package.
- Dark modern UI theme with challenge-style friction prompts.

## Implementation Order

1. Harden reusable friction and overlay blocker behavior.
2. Add optional launcher mode.
3. Add notification inbox later.

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
