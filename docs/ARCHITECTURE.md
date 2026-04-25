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
- `OverlayBlockerAccessibilityService` listens for foreground package changes from Accessibility events. Shorts / Reels mode also retrieves window content to inspect visible UI labels for known short-video surfaces.
- The overlay prompt uses Android system overlay windows after the user grants Display over other apps permission.
- Completed overlay friction stores a local per-package allow-until timestamp. The default allow window is 2 minutes.
- Launcher Mode is implemented as a Friction-owned full-screen launcher in `MainActivity.kt`.
- `MainActivity` declares a home intent filter so Android can offer Friction as the default launcher.
- Launcher Mode is hidden from the normal in-app bottom navigation and only appears when Friction receives a Home launcher intent.
- `UnlockShuffleReceiver` listens for phone unlock events and updates a local launcher shuffle seed when icon move-around is enabled.
- Launcher shuffle state and seed are stored locally in `SharedPreferences` through `AppSettings`.
- Launcher Mode supports Off, Focus Launcher, and Shuffle Launcher profiles.
- Launcher Off shows all apps in stable order.
- Focus Launcher shows only needed apps by hiding selected distraction targets.
- Shuffle Launcher shows all launchable apps and reorders the launcher grid after phone unlock.
- Shuffle Launcher is also labeled Icon Swapper in Settings because the effect is only visible after Friction is selected as the default Home app.
- The launcher uses `WallpaperManager` as a best-effort source for the current wallpaper background and falls back to the dark app background if unavailable.
- Android does not expose icon positions from other launcher apps, so Friction cannot import or move an existing Pixel/Samsung/Nova launcher layout.

All MVP data remains local to the device.

## Planned Modes

### Core Mode

Core Mode remains the default app experience. It owns app selection, usage tracking, daily limits, the reusable friction screen, and a later notification inbox.

### Launcher Mode

Launcher Mode is an optional custom Android launcher. It shows installed apps inside Friction Wellbeing's own launcher UI. Focus Launcher hides selected distraction targets so only needed apps appear. Shuffle Launcher shows all apps and randomizes positions inside Friction's launcher after phone unlock. Opening target apps triggers friction first; non-target apps open normally.

Launcher Mode must not attempt to move, rewrite, or control icons on the user's existing launcher.

### Overlay Blocker Mode

Overlay Blocker Mode is an optional experimental mode. It uses an `AccessibilityService` to detect selected target apps by foreground package name, starting with package-level Instagram support for `com.instagram.android`. It uses Display over other apps permission to show a friction overlay.

Shorts / Reels overlay mode detects YouTube Shorts and Instagram Reels through visible accessibility labels, selected tab state, content descriptions, and known internal view IDs. It shows a custom timed disabled overlay only while the detected short-video surface is visible, instead of blocking the whole Instagram or YouTube package. Detection must be treated as heuristic, not guaranteed, and may vary by Android version or OEM. Instagram and YouTube UI changes may break screen-specific detection. This mode may be risky for Google Play review, must be clearly disclosed, must be opt-in, and must not claim perfect Reels/Shorts blocking. After XML changes, Android may require disabling and re-enabling the AccessibilityService before window-content retrieval takes effect.

The service config sets `canRetrieveWindowContent` to `true` for Shorts / Reels mode. The app still does not upload screen content.

## Implementation Order

1. Harden reusable friction and overlay blocker behavior.
2. Continue testing optional launcher mode.
3. Add notification inbox later.
