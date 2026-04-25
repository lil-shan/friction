package com.frictionwellbeing.app;

import java.text.Normalizer;
import java.util.Locale;

public final class FrictionChallenge {
    public static final int CHALLENGE_COUNT = 5;

    private FrictionChallenge() {
    }

    public static int indexFor(String packageName, long nowMillis) {
        long timeBucket = nowMillis / OverlayFrictionEligibility.DEFAULT_ALLOW_WINDOW_MILLIS;
        int seed = (int) (timeBucket + packageName.hashCode());
        return Math.floorMod(seed, CHALLENGE_COUNT);
    }

    public static String promptFor(int index) {
        return promptFor(index, OverlayRepeatMode.HEAVY);
    }

    public static String promptFor(int index, String mode) {
        if (OverlayRepeatMode.LIGHT.equals(mode)) {
            return lightPromptFor(index);
        }
        return hardPromptFor(index);
    }

    private static String lightPromptFor(int index) {
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return "Quick check: What is 3 + 4?";
            case 1:
                return "Quick check: What color do you get from red and blue?";
            case 2:
                return "Quick check: What day comes after Monday?";
            case 3:
                return "Quick check: Type the opposite of cold.";
            default:
                return "Quick check: Which shape has three sides?";
        }
    }

    private static String hardPromptFor(int index) {
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return "Geography check: Which country has Reykjavik as its capital?";
            case 1:
                return "Pattern check: What comes next: 2, 4, 8, 16, __?";
            case 2:
                return "Map check: Which ocean is west of Morocco?";
            case 3:
                return "Puzzle check: What is the next prime number after 29?";
            default:
                return "Logic check: If all Zorbs are Lints and no Lints are Vels, can a Zorb be a Vel?";
        }
    }

    public static String expectedAnswerFor(int index) {
        return expectedAnswerFor(index, OverlayRepeatMode.HEAVY);
    }

    public static String expectedAnswerFor(int index, String mode) {
        if (OverlayRepeatMode.LIGHT.equals(mode)) {
            switch (Math.floorMod(index, CHALLENGE_COUNT)) {
                case 0:
                    return "7";
                case 1:
                    return "Purple";
                case 2:
                    return "Tuesday";
                case 3:
                    return "Hot";
                default:
                    return "Triangle";
            }
        }
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return "Iceland";
            case 1:
                return "32";
            case 2:
                return "Atlantic";
            case 3:
                return "31";
            default:
                return "No";
        }
    }

    public static boolean isAnswerValid(int index, String input) {
        return isAnswerValid(index, input, OverlayRepeatMode.HEAVY);
    }

    public static boolean isAnswerValid(int index, String input, String mode) {
        String normalizedInput = normalize(input);
        if (OverlayRepeatMode.LIGHT.equals(mode)) {
            switch (Math.floorMod(index, CHALLENGE_COUNT)) {
                case 0:
                    return normalizedInput.contains("7") ||
                            normalizedInput.contains("seven");
                case 1:
                    return normalizedInput.contains("purple");
                case 2:
                    return normalizedInput.contains("tuesday");
                case 3:
                    return normalizedInput.contains("hot");
                default:
                    return normalizedInput.contains("triangle");
            }
        }
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return normalizedInput.contains("iceland");
            case 1:
                return normalizedInput.contains("32");
            case 2:
                return normalizedInput.contains("atlantic");
            case 3:
                return normalizedInput.contains("31") ||
                        normalizedInput.contains("thirty one") ||
                        normalizedInput.contains("thirtyone");
            default:
                return normalizedInput.equals("no") ||
                        normalizedInput.contains("cannot") ||
                        normalizedInput.contains("can not");
        }
    }

    private static String normalize(String value) {
        String withoutAccents = Normalizer.normalize(
                value == null ? "" : value,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");
        return withoutAccents
                .toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
