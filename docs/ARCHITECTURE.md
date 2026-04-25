# Architecture

The initial MVP is a single-module native Android app.

## Current Core Mode Implementation

- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt` contains the first Compose UI flow.
- State is loaded from and saved to `SharedPreferences`.
- Navigation is handled by a local enum instead of a navigation dependency.
- Installed apps are discovered through `PackageManager` using launchable activities.
- Usage Access status is checked with `AppOpsManager`.
- Today's foreground usage is queried locally with `UsageStatsManager` when permission is granted, and the dashboard only displays totals for selected apps.
- Usage durations are not persisted in the MVP.
- Usage limit status is calculated by `UsageLimitCalculator` and covered by a temporary dependency-free unit test runner.
- `FrictionScreen` is a reusable Compose flow currently reached from the dashboard demo for selected apps at or over the daily limit.
- Friction state rules are calculated by `FrictionStateCalculator` and covered by a temporary dependency-free unit test runner.
- Overlay friction eligibility, daily-limit checks, and cooldown behavior are calculated by `OverlayFrictionEligibility` and covered by a temporary dependency-free unit test runner.
- Overlay Blocker Mode settings are stored locally in `SharedPreferences` through `AppSettings`.
- `OverlayBlockerAccessibilityService` listens for foreground package changes from Accessibility events without retrieving window content.
- The overlay prompt uses Android system overlay windows after the user grants Display over other apps permission.
- Completed overlay friction stores a local per-package allow-until timestamp. The default allow window is 2 minutes.

All MVP data remains local to the device.

## Planned Modes

### Core Mode

Core Mode remains the default app experience. It owns app selection, usage tracking, daily limits, the reusable friction screen, and a later notification inbox.

### Launcher Mode

Launcher Mode is planned as an optional custom Android launcher. It should show installed apps inside Friction Wellbeing's own launcher UI. Target app icons may move or randomize positions only inside that launcher. Opening target apps should trigger friction first; non-target apps should open normally.

Launcher Mode must not attempt to move, rewrite, or control icons on the user's existing launcher.

### Overlay Blocker Mode

Overlay Blocker Mode is an optional experimental mode. It uses an `AccessibilityService` to detect selected target apps by foreground package name, starting with package-level Instagram support for `com.instagram.android`. It uses Display over other apps permission to show a friction overlay.

The current implementation does not detect Reels or Shorts specifically. Detection must be treated as heuristic, not guaranteed, and may vary by Android version or OEM. Instagram and YouTube UI changes may break future screen-specific detection. This mode may be risky for Google Play review, must be clearly disclosed, must be opt-in, and must not claim perfect Reels/Shorts blocking.

The service config sets `canRetrieveWindowContent` to `false`; this slice only needs package-level foreground app detection.

## Implementation Order

1. Harden reusable friction and overlay blocker behavior.
2. Add optional launcher mode.
3. Add notification inbox later.
