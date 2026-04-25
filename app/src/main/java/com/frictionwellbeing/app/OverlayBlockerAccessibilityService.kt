package com.frictionwellbeing.app

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
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
        removeOverlay()
        super.onDestroy()
    }

    private fun maybeShowFrictionOverlay(packageName: String) {
        if (overlayView != null || currentOverlayPackage == packageName) {
            return
        }

        val selectedPackages = AppSettings.selectedPackages(this)
        val isSelectedTarget = packageName in selectedPackages
        val now = System.currentTimeMillis()
        val dailyLimitMinutes = AppSettings.dailyLimitMinutes(this)
        val todayUsageMinutes = queryTodayUsageMinutes(packageName)
        val shouldShowOverlay = OverlayFrictionEligibility.shouldShowOverlay(
            AppSettings.overlayBlockerEnabled(this),
            true,
            hasOverlayPermission(),
            isSelectedTarget,
            AppSettings.allowedUntilMillis(this, packageName),
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
        val challengeText = challengeFor(packageName)

        val countdownText = TextView(this).apply {
            text = "Gate opens in $remainingSeconds seconds"
            textSize = 18f
            setTextColor(ACCENT)
            gravity = Gravity.CENTER
        }
        val intentionInput = EditText(this).apply {
            hint = "Answer the prompt, then write why you want to continue."
            minLines = 3
            setTextColor(TEXT_PRIMARY)
            setHintTextColor(TEXT_MUTED)
            background = roundedDrawable(SURFACE_RAISED, 18f, STROKE, 2)
            setPadding(24, 18, 24, 18)
        }
        val continueButton = Button(this).apply {
            text = "Continue for 2 minutes"
            isEnabled = false
            setTextColor(Color.rgb(6, 35, 28))
        }

        fun refreshContinueState() {
            continueButton.isEnabled = FrictionStateCalculator.canContinue(
                remainingSeconds,
                intentionInput.text?.toString().orEmpty(),
            )
            countdownText.text = if (remainingSeconds > 0) {
                "Gate opens in $remainingSeconds seconds"
            } else {
                "Gate open. Answer the prompt to continue."
            }
        }

        val leaveButton = Button(this).apply {
            text = "Leave now"
            setTextColor(TEXT_PRIMARY)
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
            AppSettings.saveAllowedUntilMillis(
                this,
                packageName,
                OverlayFrictionEligibility.allowedUntil(System.currentTimeMillis()),
            )
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
            setPadding(34, 34, 34, 34)
            background = roundedDrawable(SURFACE, 34f, STROKE, 2)
            addView(TextView(context).apply {
                text = "Mind check"
                textSize = 14f
                letterSpacing = 0.08f
                setTextColor(ACCENT)
                gravity = Gravity.CENTER
            })
            addView(TextView(context).apply {
                text = "Pause before $appLabel"
                textSize = 26f
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
                textSize = 18f
                setTextColor(TEXT_PRIMARY)
                gravity = Gravity.CENTER
                setPadding(0, 26, 0, 22)
            })
            addView(countdownText)
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
            setBackgroundColor(Color.argb(244, 5, 8, 13))
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

    private fun challengeFor(packageName: String): String {
        val challenges = listOf(
            "Geography check: Which country has Reykjavik as its capital?",
            "Puzzle check: What comes next: 2, 4, 8, 16, __?",
            "Map check: Which ocean is west of Morocco?",
            "Logic check: If today is Saturday, what day is it in three days?",
            "Geography check: Which continent contains the Andes?",
            "Focus check: Name one thing you came here to do, not scroll past.",
        )
        val seed = (System.currentTimeMillis() /
            OverlayFrictionEligibility.DEFAULT_ALLOW_WINDOW_MILLIS +
            packageName.hashCode()).toInt()
        return challenges[Math.floorMod(seed, challenges.size)]
    }

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

    private val windowManager: WindowManager
        get() = getSystemService(WindowManager::class.java)

    companion object {
        private const val MILLIS_PER_MINUTE = 60L * 1000L
        private val ACCENT = Color.rgb(72, 224, 184)
        private val SURFACE = Color.rgb(18, 25, 34)
        private val SURFACE_RAISED = Color.rgb(26, 36, 48)
        private val STROKE = Color.rgb(52, 67, 84)
        private val TEXT_PRIMARY = Color.rgb(232, 238, 245)
        private val TEXT_MUTED = Color.rgb(154, 166, 180)
        const val INSTAGRAM_PACKAGE = "com.instagram.android"

        @Volatile
        var lastForegroundPackage: String? = null
            private set
    }
}
