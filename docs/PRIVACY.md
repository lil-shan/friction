# Privacy

Friction Wellbeing currently stores data only on the device.

The MVP stores:

- Selected app package names.
- Default daily limit in minutes.

The MVP locally queries usage stats:

- Today's foreground usage minutes are queried through Android `UsageStatsManager` when Usage Access is granted.
- The dashboard only displays totals for apps the user selected.
- Usage durations are not persisted in the MVP.

The MVP does not:

- Read notifications.
- Upload data to a server.
- Implement launcher mode.
- Enforce limits.

Usage Access is explained and linked from the app. Usage data is read locally for dashboard display only.
