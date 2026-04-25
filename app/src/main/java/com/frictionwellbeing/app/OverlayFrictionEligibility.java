package com.frictionwellbeing.app;

public final class OverlayFrictionEligibility {
    public static final long DEFAULT_ALLOW_WINDOW_MILLIS = 2L * 60L * 1000L;

    private OverlayFrictionEligibility() {
    }

    public static boolean shouldShowOverlay(
            boolean overlayModeEnabled,
            boolean accessibilityServiceEnabled,
            boolean overlayPermissionGranted,
            boolean selectedTargetApp,
            long allowedUntilMillis,
            long nowMillis,
            int todayUsageMinutes,
            int dailyLimitMinutes,
            boolean strictOverlayMode
    ) {
        if (!overlayModeEnabled
                || !accessibilityServiceEnabled
                || !overlayPermissionGranted
                || !selectedTargetApp) {
            return false;
        }

        if (allowedUntilMillis > nowMillis) {
            return false;
        }

        return strictOverlayMode
                || FrictionStateCalculator.isAtOrOverLimit(todayUsageMinutes, dailyLimitMinutes);
    }

    public static long allowedUntil(long nowMillis) {
        return nowMillis + DEFAULT_ALLOW_WINDOW_MILLIS;
    }
}
