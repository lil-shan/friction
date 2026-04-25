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
- Launcher Off shows all apps in stable order.
- Focus Launcher hides selected distraction targets.
- Shuffle Launcher / Icon Swapper randomizes positions inside Friction Wellbeing's launcher after phone unlock.
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
- Shorts / Reels mode detects visible YouTube Shorts and Instagram Reels surfaces heuristically and shows a timed disabled overlay.

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
- Mode-based overlay repeat behavior:
  - Off: overlay friction is disabled.
  - Light: friction can return after 10 minutes.
  - Heavy: friction can return after 2 minutes.
  - Shorts / Reels: detected YouTube Shorts or Instagram Reels surfaces are disabled behind a timed overlay.
    - After updates, Android may require turning the Friction accessibility service off and on again before Shorts/Reels detection can read visible labels.
  - Ultra Focus: selected target apps stay blocked for a 25-minute focus window.
- Dark modern UI theme with challenge-style friction prompts.
- Challenge prompts now require the expected answer before Continue is enabled.
- Production visual pass with a black UI, white secondary text, yellow accent, stronger hierarchy, four bottom tabs, rounded cards, status chips, usage bubbles, and a richer overlay blocker panel.
- Branded typography pass with stronger display/title/body hierarchy and permission setup folded into Settings instead of a primary tab.
- Friction can register as an Android home app candidate and show a full-screen launcher when selected as the default launcher.
- Launcher Mode is not shown as a normal in-app tab; it appears only when Android starts Friction as Home.
- Launcher Mode has two profiles:
  - Launcher Off: all apps stay visible in a stable order.
  - Focus Launcher: only needed apps are shown; selected distraction targets are hidden.
  - Shuffle Launcher: all apps are shown and reshuffled after phone unlock.
    - Also shown as Icon Swapper in Settings.
- The launcher uses the current wallpaper as a best-effort background when Android exposes it.
- Target apps opened from Friction's launcher go through the friction challenge first; non-target apps open normally.
- Android does not expose icon positions from other launchers, so Friction cannot import or move the user's existing Pixel/Samsung/Nova home-screen layout.
- Performance pass:
  - Launchable app metadata is cached after first load.
  - App icons are downscaled before display.
  - Wallpaper loading is asynchronous and downscaled.
  - Shorts/Reels accessibility scans are throttled and capped.

## Phone Setup

### Install Debug Build

Build and install with Gradle when the device is stable:

```bash
./gradlew installDebug
```

If Gradle loses the device during install, build first and install with ADB directly:

```bash
./gradlew assembleDebug
adb devices -l
adb -s <device-id> install -r app/build/outputs/apk/debug/app-debug.apk
```

### Required Android Settings

Open Friction, then configure the permissions you need:

- Usage Access: required for daily usage totals.
- Display over other apps: required for overlay prompts.
- Accessibility Service: required for Overlay Blocker and Shorts / Reels mode.
- Default Home app: required for Friction Launcher Mode and Icon Swapper.

After installing a build that changes the AccessibilityService XML, turn the Friction Accessibility Service off and on again. Android may not apply new accessibility capabilities until the service is restarted.

### Launcher Setup

To make Friction act like a real launcher:

1. Open Friction.
2. Go to `Settings`.
3. Under `Launcher mode`, tap `Open Default Home settings`.
4. Select `Friction` as the default Home app.
5. Press the phone Home button.

Launcher modes:

- `Launcher Off`: stable app grid.
- `Focus Launcher`: hides selected distraction apps.
- `Shuffle Launcher / Icon Swapper`: shows all apps and reshuffles Friction's launcher grid after phone unlock.

### Friction Setup

In the app:

1. Use `Apps` to select distraction targets.
2. Use `Home` to choose a friction mode.
3. Use `Settings` to enable overlay permissions and optional strict mode.

Friction modes:

- `Friction Off`: disables overlay friction.
- `Light`: easier challenge, longer repeat window.
- `Heavy`: harder challenge, shorter repeat window.
- `Shorts / Reels`: disables detected YouTube Shorts or Instagram Reels behind a timed overlay.
- `Ultra Focus`: blocks selected target apps until its focus timer ends.

## Troubleshooting

### `installDebug` says no device or device not found

Check ADB:

```bash
adb devices -l
```

If the phone is missing or unauthorized:

```bash
adb kill-server
adb start-server
adb devices -l
```

Then accept the USB debugging prompt on the phone. If Gradle still loses the device, install the APK directly:

```bash
adb -s <device-id> install -r app/build/outputs/apk/debug/app-debug.apk
```

### Shorts / Reels does not trigger

- Confirm `Shorts / Reels` is selected in Friction mode.
- Confirm Accessibility Service and Display over other apps are enabled.
- Turn the Friction Accessibility Service off and on again after installing a new build.
- Detection is heuristic and depends on the current YouTube/Instagram UI labels.

### Launcher toggle appears to do nothing

Launcher modes only affect the actual home screen after Friction is selected as the default Home app. Inside the normal Friction app, the launcher controls only configure behavior.

## Implementation Order

1. Harden reusable friction and overlay blocker behavior.
2. Continue testing optional launcher mode.
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
