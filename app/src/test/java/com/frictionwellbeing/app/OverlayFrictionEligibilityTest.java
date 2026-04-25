package com.frictionwellbeing.app;

public final class OverlayFrictionEligibilityTest {
    public static void main(String[] args) {
        blocksWhenModeIsOff();
        blocksDuringCooldown();
        allowsWhenAtLimitAndPermissionsReady();
        allowsStrictModeBeforeLimit();
        usesTwoMinuteAllowWindow();
        usesModeSpecificAllowWindow();
    }

    private static void blocksWhenModeIsOff() {
        assertFalse(
                OverlayFrictionEligibility.shouldShowOverlay(
                        false,
                        true,
                        true,
                        true,
                        0,
                        1000,
                        15,
                        15,
                        false
                ),
                "overlay should not show when mode is off"
        );
    }

    private static void blocksDuringCooldown() {
        assertFalse(
                OverlayFrictionEligibility.shouldShowOverlay(
                        true,
                        true,
                        true,
                        true,
                        2000,
                        1000,
                        15,
                        15,
                        false
                ),
                "overlay should not show during allow-window cooldown"
        );
    }

    private static void allowsWhenAtLimitAndPermissionsReady() {
        assertTrue(
                OverlayFrictionEligibility.shouldShowOverlay(
                        true,
                        true,
                        true,
                        true,
                        0,
                        1000,
                        15,
                        15,
                        false
                ),
                "overlay should show when target app is at the daily limit"
        );
    }

    private static void allowsStrictModeBeforeLimit() {
        assertTrue(
                OverlayFrictionEligibility.shouldShowOverlay(
                        true,
                        true,
                        true,
                        true,
                        0,
                        1000,
                        1,
                        15,
                        true
                ),
                "strict mode should show overlay before daily limit"
        );
    }

    private static void usesTwoMinuteAllowWindow() {
        assertEquals(
                121000L,
                OverlayFrictionEligibility.allowedUntil(1000L),
                "overlay allow window should be two minutes"
        );
    }

    private static void usesModeSpecificAllowWindow() {
        assertEquals(
                601000L,
                OverlayFrictionEligibility.allowedUntil(1000L, OverlayRepeatMode.LIGHT),
                "light mode should allow for ten minutes"
        );
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
