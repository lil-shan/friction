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

The MVP locally queries usage stats:

- Today's foreground usage minutes are queried through Android `UsageStatsManager` when Usage Access is granted.
- The dashboard only displays totals for apps the user selected.
- Usage durations are not persisted in the MVP.
- Friction intention prompt text is used only on screen and is not persisted in the MVP.

The MVP does not:

- Read notifications.
- Upload data to a server.
- Implement launcher mode.
- Detect Reels or Shorts specifically.
- Read private screen text for overlay blocking.
- Persist usage durations or friction intention text.

Usage Access is explained and linked from the app. Usage data is read locally for dashboard display only.

## Planned Mode Privacy Notes

### Core Mode

Core Mode should continue to keep data local by default. The later notification inbox must not be implemented until its privacy model is documented.

### Launcher Mode

Launcher Mode will be optional. It may display and arrange installed apps inside Friction Wellbeing's own launcher UI. It must not attempt to move, randomize, or modify icons on the user's existing launcher.

### Overlay Blocker Mode

Overlay Blocker Mode is optional and experimental. It requires:

- Accessibility permission to detect selected target apps by foreground package name.
- Overlay permission to show a friction overlay.

This mode is clearly disclosed and opt-in. It currently starts with package-level Instagram support for `com.instagram.android`; it does not detect Reels or Shorts specifically. Detection must be described as heuristic, not guaranteed, and may vary by Android version or OEM. Instagram and YouTube UI changes may break future screen-specific detection. The app must not claim perfect Reels/Shorts blocking.

Overlay Blocker Mode may be risky for Google Play review because it relies on sensitive Android capabilities.

No usage data, selected app list, overlay cooldown state, or intention text is uploaded.
