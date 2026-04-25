package com.frictionwellbeing.app;

public final class FrictionStateCalculator {
    public static final int COUNTDOWN_SECONDS = 10;

    private FrictionStateCalculator() {
    }

    public static boolean canContinue(int remainingSeconds, String intentionText) {
        return remainingSeconds <= 0 && intentionText != null && !intentionText.trim().isEmpty();
    }

    public static boolean isAtOrOverLimit(int usageMinutes, int dailyLimitMinutes) {
        return dailyLimitMinutes > 0 && usageMinutes >= dailyLimitMinutes;
    }
}
