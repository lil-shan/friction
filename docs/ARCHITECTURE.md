# Architecture

The initial MVP is a single-module native Android app.

- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt` contains the first Compose UI flow.
- State is loaded from and saved to `SharedPreferences`.
- Navigation is handled by a local enum instead of a navigation dependency.
- Installed apps are discovered through `PackageManager` using launchable activities.
- Usage Access status is checked with `AppOpsManager`.
- Today's foreground usage is queried locally with `UsageStatsManager` when permission is granted, and the dashboard only displays totals for selected apps.
- Usage durations are not persisted in the MVP.
- Usage limit status is calculated by `UsageLimitCalculator` and covered by a temporary dependency-free unit test runner.
- Limit enforcement is not implemented.

All MVP data remains local to the device.
