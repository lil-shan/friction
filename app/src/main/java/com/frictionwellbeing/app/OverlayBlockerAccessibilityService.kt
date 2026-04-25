package com.frictionwellbeing.app

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Calendar
import kotlin.math.ceil

class OverlayBlockerAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private var countdownRunnable: Runnable? = null
    private var repeatCheckRunnable: Runnable? = null
    private var currentOverlayPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType ?: return
        if (
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        lastForegroundPackage = packageName
        if (packageName == this.packageName) {
            return
        }

        maybeShowFrictionOverlay(packageName)
    }

    override fun onInterrupt() {
        removeOverlay()
    }

    override fun onDestroy() {
        repeatCheckRunnable?.let(handler::removeCallbacks)
        repeatCheckRunnable = null
        removeOverlay()
        super.onDestroy()
    }

    private fun maybeShowFrictionOverlay(packageName: String) {
        if (overlayView != null || currentOverlayPackage == packageName) {
            return
        }

        val selectedPackages = AppSettings.selectedPackages(this)
        val isSelectedTarget = packageName in selectedPackages
        if (!isSelectedTarget) {
            return
        }

        val now = System.currentTimeMillis()
        if (AppSettings.ultraFocusActive(this, now)) {
            showUltraFocusOverlay(
                packageName = packageName,
                appLabel = loadApplicationLabel(packageName),
                focusUntilMillis = AppSettings.ultraFocusUntilMillis(this),
            )
            return
        }

        val dailyLimitMinutes = AppSettings.dailyLimitMinutes(this)
        val todayUsageMinutes = queryTodayUsageMinutes(packageName)
        val allowedUntilMillis = AppSettings.allowedUntilMillis(this, packageName)
        scheduleRepeatCheckIfNeeded(packageName, allowedUntilMillis, now)
        val shouldShowOverlay = OverlayFrictionEligibility.shouldShowOverlay(
            AppSettings.overlayBlockerEnabled(this),
            true,
            hasOverlayPermission(),
            true,
            allowedUntilMillis,
            now,
            todayUsageMinutes,
            dailyLimitMinutes,
            AppSettings.strictOverlayMode(this),
        )

        if (shouldShowOverlay) {
            showFrictionOverlay(
                packageName = packageName,
                appLabel = loadApplicationLabel(packageName),
            )
        }
    }

    private fun showFrictionOverlay(packageName: String, appLabel: String) {
        if (!hasOverlayPermission()) {
            return
        }

        currentOverlayPackage = packageName
        var remainingSeconds = FrictionStateCalculator.COUNTDOWN_SECONDS
        val repeatMode = AppSettings.overlayRepeatMode(this)
        val challengeIndex = FrictionChallenge.indexFor(packageName, System.currentTimeMillis())
        val challengeText = FrictionChallenge.promptFor(challengeIndex, repeatMode)
        val allowWindowMinutes = when (repeatMode) {
            OverlayRepeatMode.LIGHT -> AppSettings.lightModeMinutes(this)
            OverlayRepeatMode.HEAVY -> AppSettings.heavyModeMinutes(this)
            else -> 0
        }

        val countdownText = TextView(this).apply {
            text = "Gate opens in $remainingSeconds seconds"
            textSize = 16f
            setTextColor(ACCENT)
            gravity = Gravity.CENTER
            background = roundedDrawable(CHIP_SURFACE, 28f, STROKE, 1)
            setPadding(22, 14, 22, 14)
        }
        val answerText = TextView(this).apply {
            text = "Answer required"
            textSize = 13f
            setTextColor(TEXT_MUTED)
            gravity = Gravity.CENTER
            background = roundedDrawable(CHIP_SURFACE, 28f, STROKE, 1)
            setPadding(22, 14, 22, 14)
        }
        val intentionInput = EditText(this).apply {
            hint = "Answer + why you want to continue"
            minLines = 3
            setTextColor(TEXT_PRIMARY)
            setHintTextColor(TEXT_MUTED)
            background = roundedDrawable(SURFACE_RAISED, 26f, STROKE, 2)
            setPadding(28, 22, 28, 22)
        }
        val continueButton = Button(this).apply {
            text = "Continue for $allowWindowMinutes min"
            isEnabled = false
            setTextColor(Color.BLACK)
            backgroundTintList = ColorStateList.valueOf(ACCENT)
        }

        fun refreshContinueState() {
            val input = intentionInput.text?.toString().orEmpty()
            val answerValid = FrictionChallenge.isAnswerValid(challengeIndex, input, repeatMode)
            continueButton.isEnabled = answerValid && FrictionStateCalculator.canContinue(
                remainingSeconds,
                input,
            )
            countdownText.text = if (remainingSeconds > 0) {
                "Gate opens in $remainingSeconds seconds"
            } else if (!answerValid) {
                "Gate open. Correct answer still needed."
            } else {
                "Gate open. You can continue."
            }
            answerText.text = if (answerValid) {
                "Answer accepted"
            } else {
                "Correct answer required"
            }
            answerText.setTextColor(if (answerValid) ACCENT else TEXT_MUTED)
        }

        val leaveButton = Button(this).apply {
            text = "Leave now"
            setTextColor(TEXT_PRIMARY)
            backgroundTintList = ColorStateList.valueOf(CHIP_SURFACE)
            setOnClickListener {
                removeOverlay()
                if (!performGlobalAction(GLOBAL_ACTION_HOME)) {
                    startActivity(
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }

        continueButton.setOnClickListener {
            val nowMillis = System.currentTimeMillis()
            val allowedUntilMillis = OverlayFrictionEligibility.allowedUntil(
                nowMillis,
                AppSettings.allowWindowMillis(this, AppSettings.overlayRepeatMode(this)),
            )
            AppSettings.saveAllowedUntilMillis(
                this,
                packageName,
                allowedUntilMillis,
            )
            scheduleRepeatCheckIfNeeded(packageName, allowedUntilMillis, nowMillis)
            removeOverlay()
        }

        intentionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refreshContinueState()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(38, 38, 38, 38)
            background = gradientDrawable(
                intArrayOf(SURFACE_TOP, SURFACE, SURFACE_BOTTOM),
                42f,
                STROKE,
                2,
            )
            addView(TextView(context).apply {
                text = "FRICTION CHECK"
                textSize = 12f
                letterSpacing = 0.12f
                setTextColor(ACCENT)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "Pause before\n$appLabel"
                textSize = 28f
                setTextColor(TEXT_PRIMARY)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = packageName
                textSize = 13f
                setTextColor(TEXT_MUTED)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = challengeText
                textSize = 20f
                setTextColor(TEXT_PRIMARY)
                gravity = Gravity.CENTER
                setPadding(0, 26, 0, 22)
            })
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                addView(countdownText, LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                ).apply {
                    setMargins(0, 0, 8, 0)
                })
                addView(answerText, LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f,
                ).apply {
                    setMargins(8, 0, 0, 0)
                })
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
            addView(intentionInput, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(0, 24, 0, 18)
            })
            addView(continueButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
            addView(leaveButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(0, 10, 0, 0)
            })
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = gradientDrawable(
                intArrayOf(Color.rgb(0, 0, 0), Color.rgb(12, 12, 12)),
                0f,
                Color.TRANSPARENT,
                0,
            )
            addView(panel, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        runCatching {
            windowManager.addView(content, layoutParams)
            overlayView = content
            refreshContinueState()
            startCountdown { seconds ->
                remainingSeconds = seconds
                refreshContinueState()
            }
        }.onFailure {
            currentOverlayPackage = null
        }
    }

    private fun showUltraFocusOverlay(
        packageName: String,
        appLabel: String,
        focusUntilMillis: Long,
    ) {
        if (!hasOverlayPermission()) {
            return
        }

        currentOverlayPackage = packageName

        val countdownText = TextView(this).apply {
            textSize = 16f
            setTextColor(ACCENT)
            gravity = Gravity.CENTER
            background = roundedDrawable(CHIP_SURFACE, 28f, STROKE, 1)
            setPadding(22, 14, 22, 14)
        }
        val leaveButton = Button(this).apply {
            text = "Leave app"
            setTextColor(Color.BLACK)
            backgroundTintList = ColorStateList.valueOf(ACCENT)
            setOnClickListener {
                removeOverlay()
                if (!performGlobalAction(GLOBAL_ACTION_HOME)) {
                    startActivity(
                        Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }

        fun refreshFocusCountdown() {
            val remainingMillis = (focusUntilMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            countdownText.text = if (remainingMillis > 0L) {
                "Locked for ${remainingMillis.toDisplayMinutes()} min"
            } else {
                "Focus window complete"
            }
            if (remainingMillis <= 0L) {
                removeOverlay()
            }
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(38, 38, 38, 38)
            background = gradientDrawable(
                intArrayOf(SURFACE_TOP, SURFACE, SURFACE_BOTTOM),
                42f,
                STROKE,
                2,
            )
            addView(TextView(context).apply {
                text = "ULTRA FOCUS"
                textSize = 12f
                letterSpacing = 0.12f
                setTextColor(ACCENT)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "Do not open\n$appLabel"
                textSize = 28f
                setTextColor(TEXT_PRIMARY)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "This target app is unavailable until your focus timer ends."
                textSize = 18f
                setTextColor(TEXT_PRIMARY)
                gravity = Gravity.CENTER
                setPadding(0, 22, 0, 18)
            })
            addView(countdownText, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
            addView(leaveButton, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(0, 22, 0, 0)
            })
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            background = gradientDrawable(
                intArrayOf(Color.rgb(0, 0, 0), Color.rgb(12, 12, 12)),
                0f,
                Color.TRANSPARENT,
                0,
            )
            addView(panel, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ))
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.CENTER
        }

        runCatching {
            windowManager.addView(content, layoutParams)
            overlayView = content
            refreshFocusCountdown()
            countdownRunnable = object : Runnable {
                override fun run() {
                    refreshFocusCountdown()
                    if (overlayView != null) {
                        handler.postDelayed(this, 1000L)
                    }
                }
            }
            handler.postDelayed(countdownRunnable!!, 1000L)
        }.onFailure {
            currentOverlayPackage = null
        }
    }

    private fun startCountdown(onTick: (Int) -> Unit) {
        var remainingSeconds = FrictionStateCalculator.COUNTDOWN_SECONDS
        countdownRunnable = object : Runnable {
            override fun run() {
                remainingSeconds -= 1
                onTick(remainingSeconds)
                if (remainingSeconds > 0) {
                    handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.postDelayed(countdownRunnable!!, 1000L)
    }

    private fun removeOverlay() {
        countdownRunnable?.let(handler::removeCallbacks)
        countdownRunnable = null
        overlayView?.let { view ->
            runCatching {
                windowManager.removeView(view)
            }
        }
        overlayView = null
        currentOverlayPackage = null
    }

    private fun scheduleRepeatCheckIfNeeded(
        packageName: String,
        allowedUntilMillis: Long,
        nowMillis: Long,
    ) {
        repeatCheckRunnable?.let(handler::removeCallbacks)
        repeatCheckRunnable = null
        if (allowedUntilMillis <= nowMillis) {
            return
        }
        repeatCheckRunnable = Runnable {
            repeatCheckRunnable = null
            if (lastForegroundPackage == packageName) {
                maybeShowFrictionOverlay(packageName)
            }
        }
        handler.postDelayed(
            repeatCheckRunnable!!,
            (allowedUntilMillis - nowMillis).coerceAtLeast(1000L),
        )
    }

    private fun queryTodayUsageMinutes(packageName: String): Int {
        val usageStatsManager =
            getSystemService(UsageStatsManager::class.java) ?: return 0
        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now,
        ) ?: return 0

        val usageMillis = usageStats
            .filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
        if (usageMillis <= 0L) {
            return 0
        }
        return ceil(usageMillis / MILLIS_PER_MINUTE.toDouble()).toInt()
    }

    private fun Long.toDisplayMinutes(): Int {
        if (this <= 0L) {
            return 0
        }
        return ceil(this / MILLIS_PER_MINUTE.toDouble()).toInt()
    }

    private fun loadApplicationLabel(packageName: String): String {
        val applicationInfo = runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
        }.getOrNull()

        return applicationInfo?.loadLabel(packageManager)?.toString() ?: packageName
    }

    private fun hasOverlayPermission(): Boolean =
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(this)

    private fun roundedDrawable(
        color: Int,
        radius: Float,
        strokeColor: Int,
        strokeWidth: Int,
    ): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }

    private fun gradientDrawable(
        colors: IntArray,
        radius: Float,
        strokeColor: Int,
        strokeWidth: Int,
    ): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors).apply {
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }

    private val windowManager: WindowManager
        get() = getSystemService(WindowManager::class.java)

    companion object {
        private const val MILLIS_PER_MINUTE = 60L * 1000L
        private val ACCENT = Color.rgb(255, 216, 77)
        private val SURFACE_TOP = Color.rgb(28, 28, 28)
        private val SURFACE = Color.rgb(14, 14, 14)
        private val SURFACE_BOTTOM = Color.rgb(0, 0, 0)
        private val SURFACE_RAISED = Color.rgb(24, 24, 24)
        private val CHIP_SURFACE = Color.rgb(31, 31, 31)
        private val STROKE = Color.rgb(58, 58, 58)
        private val TEXT_PRIMARY = Color.rgb(248, 248, 242)
        private val TEXT_MUTED = Color.rgb(183, 183, 176)
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        @Volatile
        var lastForegroundPackage: String? = null
            private set
    }
}
