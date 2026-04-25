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
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return "Geography check: Which country has Reykjavik as its capital?";
            case 1:
                return "Pattern check: What comes next: 2, 4, 8, 16, __?";
            case 2:
                return "Map check: Which ocean is west of Morocco?";
            case 3:
                return "Calendar check: If today is Saturday, what day is it in three days?";
            default:
                return "Geography check: Which continent contains the Andes?";
        }
    }

    public static String expectedAnswerFor(int index) {
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return "Iceland";
            case 1:
                return "32";
            case 2:
                return "Atlantic";
            case 3:
                return "Tuesday";
            default:
                return "South America";
        }
    }

    public static boolean isAnswerValid(int index, String input) {
        String normalizedInput = normalize(input);
        switch (Math.floorMod(index, CHALLENGE_COUNT)) {
            case 0:
                return normalizedInput.contains("iceland");
            case 1:
                return normalizedInput.contains("32");
            case 2:
                return normalizedInput.contains("atlantic");
            case 3:
                return normalizedInput.contains("tuesday");
            default:
                return normalizedInput.contains("south america") ||
                        normalizedInput.contains("southamerica");
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
