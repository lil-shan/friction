package com.frictionwellbeing.app;

public final class FrictionStateCalculatorTest {
    public static void main(String[] args) {
        blocksContinueDuringCountdown();
        blocksContinueWithoutIntention();
        allowsContinueAfterCountdownWithIntention();
        detectsAtOrOverLimit();
    }

    private static void blocksContinueDuringCountdown() {
        assertFalse(
                FrictionStateCalculator.canContinue(1, "Check one thing"),
                "continue should be blocked while countdown remains"
        );
    }

    private static void blocksContinueWithoutIntention() {
        assertFalse(
                FrictionStateCalculator.canContinue(0, "   "),
                "continue should be blocked without intention text"
        );
    }

    private static void allowsContinueAfterCountdownWithIntention() {
        assertTrue(
                FrictionStateCalculator.canContinue(0, "Reply to a message"),
                "continue should be allowed after countdown with intention text"
        );
    }

    private static void detectsAtOrOverLimit() {
        assertFalse(
                FrictionStateCalculator.isAtOrOverLimit(9, 10),
                "usage below limit should not be at-or-over"
        );
        assertTrue(
                FrictionStateCalculator.isAtOrOverLimit(10, 10),
                "usage equal to limit should be at-or-over"
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
}
