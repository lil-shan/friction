# Privacy

Friction Wellbeing currently stores data only on the device.

The MVP stores:

- Selected app package names.
- Default daily limit in minutes.
- Overlay Blocker Mode on/off state.
- Strict overlay mode on/off state.
- Overlay repeat mode.
- Ultra Focus end timestamp.
- Temporary per-package overlay allow-until timestamps.
- Launcher icon move-around on/off state.
- Launcher mode.
- Launcher shuffle seed.

The MVP locally queries usage stats:

- Today's foreground usage minutes are queried through Android `UsageStatsManager` when Usage Access is granted.
- The dashboard only displays totals for apps the user selected.
- Usage durations are not persisted in the MVP.
- Friction intention prompt text is used only on screen and is not persisted in the MVP.

The MVP does not:

- Read notifications.
- Upload data to a server.
- Upload screen content.
- Persist usage durations or friction intention text.

Usage Access is explained and linked from the app. Usage data is read locally for dashboard display only.

## Planned Mode Privacy Notes

### Core Mode

Core Mode should continue to keep data local by default. The later notification inbox must not be implemented until its privacy model is documented.

### Launcher Mode

Launcher Mode is optional. It displays and arranges installed apps inside Friction Wellbeing's own launcher UI when Friction is selected as the Android launcher. It may use the current wallpaper as a best-effort local background. It must not attempt to move, randomize, modify, or import icons from the user's existing launcher.

### Overlay Blocker Mode

Overlay Blocker Mode is optional and experimental. It requires:

- Accessibility permission to detect selected target apps by foreground package name and, in Shorts / Reels mode, inspect visible UI labels for known short-video surfaces.
- Overlay permission to show a friction overlay.

This mode is clearly disclosed and opt-in. It currently supports package-level selected-app blocking and an opt-in Shorts / Reels mode for `com.instagram.android` and `com.google.android.youtube`. Shorts / Reels mode uses Accessibility window-content retrieval to inspect visible UI labels, selected tab state, content descriptions, and known view IDs for Shorts/Reels surfaces. Detection must be described as heuristic, not guaranteed, and may vary by Android version or OEM. Instagram and YouTube UI changes may break screen-specific detection. The app must not claim perfect Reels/Shorts blocking.

Overlay Blocker Mode may be risky for Google Play review because it relies on sensitive Android capabilities.

No usage data, selected app list, overlay cooldown state, or intention text is uploaded.
