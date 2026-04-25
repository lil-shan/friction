package com.frictionwellbeing.app;

public final class OverlayRepeatMode {
    public static final String LIGHT = "light";
    public static final String HEAVY = "heavy";
    public static final String ULTRA_FOCUS = "ultra_focus";

    public static final int DEFAULT_LIGHT_MINUTES = 10;
    public static final int DEFAULT_HEAVY_MINUTES = 2;
    public static final int DEFAULT_ULTRA_FOCUS_MINUTES = 25;

    public static final long LIGHT_ALLOW_WINDOW_MILLIS = minutesToMillis(DEFAULT_LIGHT_MINUTES);
    public static final long HEAVY_ALLOW_WINDOW_MILLIS = minutesToMillis(DEFAULT_HEAVY_MINUTES);
    public static final long ULTRA_FOCUS_DURATION_MILLIS =
            minutesToMillis(DEFAULT_ULTRA_FOCUS_MINUTES);

    private OverlayRepeatMode() {
    }

    public static long allowWindowMillis(String mode) {
        if (ULTRA_FOCUS.equals(mode)) {
            return 0L;
        }
        if (LIGHT.equals(mode)) {
            return LIGHT_ALLOW_WINDOW_MILLIS;
        }
        return HEAVY_ALLOW_WINDOW_MILLIS;
    }

    public static boolean isUltraFocus(String mode) {
        return ULTRA_FOCUS.equals(mode);
    }

    public static long minutesToMillis(int minutes) {
        return Math.max(1L, minutes) * 60L * 1000L;
    }
}
