package com.frictionwellbeing.app

import android.content.Context

object AppSettings {
    private const val PREFERENCES_NAME = "friction_wellbeing"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"
    private const val KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
    private const val KEY_OVERLAY_BLOCKER_ENABLED = "overlay_blocker_enabled"
    private const val KEY_STRICT_OVERLAY_MODE = "strict_overlay_mode"
    private const val ALLOWED_UNTIL_PREFIX = "allowed_until_"
    private const val DEFAULT_DAILY_LIMIT_MINUTES = 15

    fun selectedPackages(context: Context): Set<String> =
        preferences(context).getStringSet(KEY_SELECTED_PACKAGES, emptySet()).orEmpty().toSet()

    fun saveSelectedPackages(context: Context, packageNames: Set<String>) {
        preferences(context).edit().putStringSet(KEY_SELECTED_PACKAGES, packageNames).apply()
    }

    fun dailyLimitMinutes(context: Context): Int =
        preferences(context).getInt(KEY_DAILY_LIMIT_MINUTES, DEFAULT_DAILY_LIMIT_MINUTES)

    fun saveDailyLimitMinutes(context: Context, minutes: Int) {
        preferences(context).edit().putInt(KEY_DAILY_LIMIT_MINUTES, minutes).apply()
    }

    fun overlayBlockerEnabled(context: Context): Boolean =
        preferences(context).getBoolean(KEY_OVERLAY_BLOCKER_ENABLED, false)

    fun saveOverlayBlockerEnabled(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_OVERLAY_BLOCKER_ENABLED, enabled).apply()
    }

    fun strictOverlayMode(context: Context): Boolean =
        preferences(context).getBoolean(KEY_STRICT_OVERLAY_MODE, false)

    fun saveStrictOverlayMode(context: Context, enabled: Boolean) {
        preferences(context).edit().putBoolean(KEY_STRICT_OVERLAY_MODE, enabled).apply()
    }

    fun allowedUntilMillis(context: Context, packageName: String): Long =
        preferences(context).getLong(ALLOWED_UNTIL_PREFIX + packageName, 0L)

    fun saveAllowedUntilMillis(context: Context, packageName: String, allowedUntilMillis: Long) {
        preferences(context).edit()
            .putLong(ALLOWED_UNTIL_PREFIX + packageName, allowedUntilMillis)
            .apply()
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
}
