package com.frictionwellbeing.app

import android.content.Context

object AppSettings {
    private const val PREFERENCES_NAME = "friction_wellbeing"
    private const val KEY_SELECTED_PACKAGES = "selected_packages"
    private const val KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
    private const val KEY_OVERLAY_BLOCKER_ENABLED = "overlay_blocker_enabled"
    private const val KEY_STRICT_OVERLAY_MODE = "strict_overlay_mode"
    private const val KEY_OVERLAY_REPEAT_MODE = "overlay_repeat_mode"
    private const val KEY_ULTRA_FOCUS_UNTIL = "ultra_focus_until"
    private const val KEY_LIGHT_MODE_MINUTES = "light_mode_minutes"
    private const val KEY_HEAVY_MODE_MINUTES = "heavy_mode_minutes"
    private const val KEY_ULTRA_FOCUS_MINUTES = "ultra_focus_minutes"
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

    fun overlayRepeatMode(context: Context): String =
        preferences(context).getString(KEY_OVERLAY_REPEAT_MODE, OverlayRepeatMode.HEAVY)
            ?: OverlayRepeatMode.HEAVY

    fun saveOverlayRepeatMode(context: Context, mode: String) {
        preferences(context).edit().putString(KEY_OVERLAY_REPEAT_MODE, mode).apply()
    }

    fun lightModeMinutes(context: Context): Int =
        preferences(context).getInt(
            KEY_LIGHT_MODE_MINUTES,
            OverlayRepeatMode.DEFAULT_LIGHT_MINUTES,
        )

    fun saveLightModeMinutes(context: Context, minutes: Int) {
        preferences(context).edit()
            .putInt(KEY_LIGHT_MODE_MINUTES, minutes.coerceAtLeast(1))
            .apply()
    }

    fun heavyModeMinutes(context: Context): Int =
        preferences(context).getInt(
            KEY_HEAVY_MODE_MINUTES,
            OverlayRepeatMode.DEFAULT_HEAVY_MINUTES,
        )

    fun saveHeavyModeMinutes(context: Context, minutes: Int) {
        preferences(context).edit()
            .putInt(KEY_HEAVY_MODE_MINUTES, minutes.coerceAtLeast(1))
            .apply()
    }

    fun ultraFocusMinutes(context: Context): Int =
        preferences(context).getInt(
            KEY_ULTRA_FOCUS_MINUTES,
            OverlayRepeatMode.DEFAULT_ULTRA_FOCUS_MINUTES,
        )

    fun saveUltraFocusMinutes(context: Context, minutes: Int) {
        preferences(context).edit()
            .putInt(KEY_ULTRA_FOCUS_MINUTES, minutes.coerceAtLeast(1))
            .apply()
    }

    fun allowWindowMillis(context: Context, mode: String): Long =
        when (mode) {
            OverlayRepeatMode.LIGHT -> OverlayRepeatMode.minutesToMillis(lightModeMinutes(context))
            OverlayRepeatMode.HEAVY -> OverlayRepeatMode.minutesToMillis(heavyModeMinutes(context))
            else -> 0L
        }

    fun ultraFocusDurationMillis(context: Context): Long =
        OverlayRepeatMode.minutesToMillis(ultraFocusMinutes(context))

    fun ultraFocusUntilMillis(context: Context): Long =
        preferences(context).getLong(KEY_ULTRA_FOCUS_UNTIL, 0L)

    fun saveUltraFocusUntilMillis(context: Context, untilMillis: Long) {
        preferences(context).edit().putLong(KEY_ULTRA_FOCUS_UNTIL, untilMillis).apply()
    }

    fun ultraFocusActive(context: Context, nowMillis: Long = System.currentTimeMillis()): Boolean =
        overlayRepeatMode(context) == OverlayRepeatMode.ULTRA_FOCUS &&
            ultraFocusUntilMillis(context) > nowMillis

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
