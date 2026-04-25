package com.frictionwellbeing.app;

import java.util.Map;
import java.util.Set;

public final class UsageLimitCalculator {
    private UsageLimitCalculator() {
    }

    public enum Status {
        NO_APPS_SELECTED,
        PERMISSION_REQUIRED,
        BELOW_LIMIT,
        OVER_LIMIT
    }

    public static Result summarize(
            Set<String> selectedPackages,
            Map<String, Integer> usageMinutesByPackage,
            int dailyLimitMinutes,
            boolean hasUsageAccess
    ) {
        if (selectedPackages.isEmpty()) {
            return new Result(0, dailyLimitMinutes, Status.NO_APPS_SELECTED);
        }

        if (!hasUsageAccess) {
            return new Result(0, dailyLimitMinutes, Status.PERMISSION_REQUIRED);
        }

        int totalMinutes = 0;
        for (String packageName : selectedPackages) {
            Integer minutes = usageMinutesByPackage.get(packageName);
            if (minutes != null && minutes > 0) {
                totalMinutes += minutes;
            }
        }

        Status status = totalMinutes >= dailyLimitMinutes
                ? Status.OVER_LIMIT
                : Status.BELOW_LIMIT;
        return new Result(totalMinutes, dailyLimitMinutes, status);
    }

    public static final class Result {
        private final int totalUsageMinutes;
        private final int dailyLimitMinutes;
        private final Status status;

        public Result(int totalUsageMinutes, int dailyLimitMinutes, Status status) {
            this.totalUsageMinutes = totalUsageMinutes;
            this.dailyLimitMinutes = dailyLimitMinutes;
            this.status = status;
        }

        public int getTotalUsageMinutes() {
            return totalUsageMinutes;
        }

        public int getDailyLimitMinutes() {
            return dailyLimitMinutes;
        }

        public Status getStatus() {
            return status;
        }
    }
}
