# Architecture

The initial MVP is a single-module native Android app.

- `app/src/main/java/com/frictionwellbeing/app/MainActivity.kt` contains the first Compose UI flow.
- State is loaded from and saved to `SharedPreferences`.
- Navigation is handled by a local enum instead of a navigation dependency.
- Installed apps are discovered through `PackageManager` using launchable activities.
- Usage Access status is checked with `AppOpsManager`; enforcement is not implemented.

All MVP data remains local to the device.
