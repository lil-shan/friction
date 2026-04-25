# Roadmap

## Completed

- [x] Initialize documentation files.
- [x] Scaffold minimal native Android project structure.
- [x] Add Gradle wrapper files.
- [x] Add Kotlin + Jetpack Compose app module.
- [x] Add local storage for selected package names and the default daily limit.
- [x] Add initial MVP screens: dashboard, app selection, Usage Access, and settings.
- [x] Implement usage stats reading for selected apps after Usage Access is granted.
- [x] Add dependency-free usage limit logic test wired into Gradle `check`.
- [x] Configure Android SDK API 35 locally and verify `./gradlew build`.
- [x] Add friction flow preparation screen with countdown and intention prompt.
- [x] Add dependency-free friction state logic test wired into Gradle `check`.
- [x] Finish reusable friction screen for Core Mode demo use.
- [x] Add optional experimental Overlay Blocker settings screen.
- [x] Add package-level AccessibilityService foreground detection for selected target apps, starting with Instagram package support.
- [x] Add Display over other apps permission handling and setup path.
- [x] Add overlay friction prompt with countdown, challenge/intention text, leave action, and 2-minute local allow window.
- [x] Switch the app and overlay toward a dark modern visual style.
- [x] Validate friction challenge answers before enabling Continue.
- [x] Add Light, Heavy, and Ultra Focus overlay repeat modes.
- [x] Schedule overlay repeat checks so friction can return while the target app remains foreground.
- [x] Add dependency-free overlay eligibility and cooldown logic test wired into Gradle `check`.

## Product Modes

### Core Mode

- App selection.
- Usage tracking.
- Daily limits.
- Friction screen.
- Later notification inbox.

### Launcher Mode

- Optional custom Android launcher.
- Shows installed apps.
- Target app icons can move or randomize positions inside Friction Wellbeing's launcher.
- Opening target apps triggers friction first.
- Non-target apps open normally.
- Must not try to move icons on the user's existing launcher.

### Overlay Blocker Mode

- Optional experimental mode.
- Uses `AccessibilityService` to detect selected target apps by foreground package name.
- Starts with package-level Instagram support for `com.instagram.android`.
- Uses overlay permission to show a friction overlay.
- Detection is heuristic and not guaranteed.
- Instagram and YouTube UI changes may break detection.
- May be risky for Google Play review.
- Must be clearly disclosed and opt-in.
- Must not claim perfect Reels/Shorts blocking.
- Does not detect Reels or Shorts specifically yet.

## Current Implementation Order

1. Harden reusable friction and overlay blocker behavior.
2. Add optional launcher mode.
3. Add notification inbox later.

## Next

- [ ] Test Overlay Blocker Mode on physical Android devices and document OEM-specific behavior.
- [ ] Add optional Launcher Mode after overlay blocker hardening.
- [ ] Add notification inbox later.
- [ ] Add basic UI tests where practical.
- [ ] Replace the temporary dependency-free logic tests with a standard Android/JUnit test setup when dependency policy allows.
- [ ] Keep notification reading out of scope until explicitly planned.
