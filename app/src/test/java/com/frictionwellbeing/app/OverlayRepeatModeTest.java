package com.frictionwellbeing.app;

public final class OverlayRepeatModeTest {
    public static void main(String[] args) {
        lightModeUsesLongerAllowance();
        heavyModeUsesTwoMinuteAllowance();
        shortsReelsUsesHeavyAllowance();
        offModeDoesNotGrantAllowance();
        ultraFocusDoesNotGrantAllowance();
        convertsMinutesToMillis();
    }

    private static void lightModeUsesLongerAllowance() {
        assertEquals(
                10L * 60L * 1000L,
                OverlayRepeatMode.allowWindowMillis(OverlayRepeatMode.LIGHT),
                "light mode should repeat less often"
        );
    }

    private static void heavyModeUsesTwoMinuteAllowance() {
        assertEquals(
                2L * 60L * 1000L,
                OverlayRepeatMode.allowWindowMillis(OverlayRepeatMode.HEAVY),
                "heavy mode should repeat every two minutes"
        );
    }

    private static void shortsReelsUsesHeavyAllowance() {
        assertEquals(
                2L * 60L * 1000L,
                OverlayRepeatMode.allowWindowMillis(OverlayRepeatMode.SHORTS_REELS),
                "shorts/reels mode should use the heavy repeat window"
        );
    }

    private static void offModeDoesNotGrantAllowance() {
        assertEquals(
                0L,
                OverlayRepeatMode.allowWindowMillis(OverlayRepeatMode.OFF),
                "off mode should not grant an app-opening allowance"
        );
    }

    private static void ultraFocusDoesNotGrantAllowance() {
        assertEquals(
                0L,
                OverlayRepeatMode.allowWindowMillis(OverlayRepeatMode.ULTRA_FOCUS),
                "ultra focus should not grant an app-opening allowance"
        );
    }

    private static void convertsMinutesToMillis() {
        assertEquals(
                3L * 60L * 1000L,
                OverlayRepeatMode.minutesToMillis(3),
                "mode durations should be stored as editable minutes"
        );
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
