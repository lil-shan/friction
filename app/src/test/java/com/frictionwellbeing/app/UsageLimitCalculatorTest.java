package com.frictionwellbeing.app;

import java.util.Map;
import java.util.Set;

public final class UsageLimitCalculatorTest {
    public static void main(String[] args) {
        reportsBelowLimitWhenUsageIsUnderLimit();
        reportsOverLimitWhenUsageMeetsLimit();
        reportsPermissionRequiredWithoutUsageAccess();
    }

    private static void reportsBelowLimitWhenUsageIsUnderLimit() {
        UsageLimitCalculator.Result result = UsageLimitCalculator.summarize(
                Set.of("com.example.social"),
                Map.of("com.example.social", 14),
                15,
                true
        );

        assertEquals(14, result.getTotalUsageMinutes(), "total usage");
        assertEquals(UsageLimitCalculator.Status.BELOW_LIMIT, result.getStatus(), "status");
    }

    private static void reportsOverLimitWhenUsageMeetsLimit() {
        UsageLimitCalculator.Result result = UsageLimitCalculator.summarize(
                Set.of("com.example.social"),
                Map.of("com.example.social", 15),
                15,
                true
        );

        assertEquals(15, result.getTotalUsageMinutes(), "total usage");
        assertEquals(UsageLimitCalculator.Status.OVER_LIMIT, result.getStatus(), "status");
    }

    private static void reportsPermissionRequiredWithoutUsageAccess() {
        UsageLimitCalculator.Result result = UsageLimitCalculator.summarize(
                Set.of("com.example.social"),
                Map.of("com.example.social", 20),
                15,
                false
        );

        assertEquals(0, result.getTotalUsageMinutes(), "total usage");
        assertEquals(UsageLimitCalculator.Status.PERMISSION_REQUIRED, result.getStatus(), "status");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
