package com.frictionwellbeing.app;

public final class FrictionChallengeTest {
    public static void main(String[] args) {
        acceptsCorrectAnswersInsideLongerIntentions();
        rejectsWrongAnswers();
        keepsChallengeIndexStableWithinAllowWindow();
    }

    private static void acceptsCorrectAnswersInsideLongerIntentions() {
        assertTrue(
                FrictionChallenge.isAnswerValid(0, "Iceland. I only need to reply to one message."),
                "Reykjavik challenge should accept Iceland in a longer intention"
        );
        assertTrue(
                FrictionChallenge.isAnswerValid(4, "South America - checking a saved post"),
                "Andes challenge should accept South America"
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
