package com.frictionwellbeing.app;

public final class FrictionChallengeTest {
    public static void main(String[] args) {
        acceptsCorrectAnswersInsideLongerIntentions();
        lightModeAcceptsSimpleAnswers();
        heavyModeRejectsSimpleAnswers();
        rejectsWrongAnswers();
        keepsChallengeIndexStableWithinAllowWindow();
    }

    private static void lightModeAcceptsSimpleAnswers() {
        assertTrue(
                FrictionChallenge.isAnswerValid(
                        0,
                        "7 - quick check before opening",
                        OverlayRepeatMode.LIGHT
                ),
                "light mode should accept simple arithmetic answers"
        );
    }

    private static void heavyModeRejectsSimpleAnswers() {
        assertFalse(
                FrictionChallenge.isAnswerValid(
                        0,
                        "7 - quick check before opening",
                        OverlayRepeatMode.HEAVY
                ),
                "heavy mode should not use the light-mode simple question"
        );
    }

    private static void acceptsCorrectAnswersInsideLongerIntentions() {
        assertTrue(
                FrictionChallenge.isAnswerValid(0, "Iceland. I only need to reply to one message."),
                "Reykjavik challenge should accept Iceland in a longer intention"
        );
        assertTrue(
                FrictionChallenge.isAnswerValid(4, "Cannot be a Vel - checking one saved post"),
                "logic challenge should accept no in a longer intention"
        );
    }

    private static void rejectsWrongAnswers() {
        assertFalse(
                FrictionChallenge.isAnswerValid(1, "64 and I want to scroll"),
                "pattern challenge should reject the wrong answer"
        );
    }

    private static void keepsChallengeIndexStableWithinAllowWindow() {
        int first = FrictionChallenge.indexFor("com.instagram.android", 1000L);
        int second = FrictionChallenge.indexFor("com.instagram.android", 119000L);
        assertEquals(first, second, "challenge should remain stable inside the two-minute window");
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

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
