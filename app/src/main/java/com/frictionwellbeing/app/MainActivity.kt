package com.frictionwellbeing.app

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrictionApp(
                preferences = remember {
                    FrictionPreferences(applicationContext)
                },
            )
        }
    }
}

private enum class Screen(val title: String) {
    Dashboard("Home"),
    Apps("Apps"),
    Focus("Focus"),
    Settings("Settings"),
    Friction("Friction"),
}

private data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap?,
)

private data class SelectedAppUsage(
    val label: String,
    val packageName: String,
    val todayUsageMinutes: Int,
)

private data class FrictionTarget(
    val label: String,
    val packageName: String,
)

private sealed interface DashboardUsageState {
    data object Empty : DashboardUsageState
    data object Loading : DashboardUsageState
    data object PermissionDenied : DashboardUsageState
    data class Loaded(
        val apps: List<SelectedAppUsage>,
        val summary: UsageLimitCalculator.Result,
    ) : DashboardUsageState
}

private class FrictionPreferences(private val context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("friction_wellbeing", Context.MODE_PRIVATE)

    fun selectedPackages(): Set<String> =
        AppSettings.selectedPackages(context)

    fun saveSelectedPackages(packageNames: Set<String>) {
        AppSettings.saveSelectedPackages(context, packageNames)
    }

    fun dailyLimitMinutes(): Int =
        AppSettings.dailyLimitMinutes(context)

    fun saveDailyLimitMinutes(minutes: Int) {
        AppSettings.saveDailyLimitMinutes(context, minutes)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrictionApp(preferences: FrictionPreferences) {
    var screen by remember { mutableStateOf(Screen.Dashboard) }
    var selectedPackages by remember { mutableStateOf(preferences.selectedPackages()) }
    var dailyLimitMinutes by remember { mutableIntStateOf(preferences.dailyLimitMinutes()) }
    var frictionTarget by remember { mutableStateOf<FrictionTarget?>(null) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = WellnessLime,
            secondary = WellnessText,
            tertiary = WellnessMuted,
            background = WellnessInk,
            surface = WellnessCard,
            surfaceVariant = WellnessCardSoft,
            onPrimary = Color(0xFF06231C),
            onSecondary = Color(0xFF152100),
            onBackground = WellnessText,
            onSurface = WellnessText,
            onSurfaceVariant = WellnessMuted,
        ),
        typography = frictionTypography(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = WellnessInk,
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BrandMark(size = 28)
                                Text(screen.title, fontWeight = FontWeight.Bold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = WellnessText,
                        ),
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = WellnessDeep.copy(alpha = 0.96f)) {
                        Screen.entries
                            .filterNot { it == Screen.Friction }
                            .forEach { item ->
                                NavigationBarItem(
                                    selected = screen == item,
                                    onClick = { screen = item },
                                    label = { Text(item.title) },
                                    icon = { Text(item.title.first().toString()) },
                                )
                            }
                    }
                },
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appBackgroundBrush())
                        .padding(innerPadding),
                ) {
                    when (screen) {
                        Screen.Dashboard -> DashboardScreen(
                            selectedPackages = selectedPackages,
                            dailyLimitMinutes = dailyLimitMinutes,
                            onChooseApps = { screen = Screen.Apps },
                            onOpenSettings = { screen = Screen.Settings },
                            onOpenUsageAccess = { screen = Screen.Settings },
                            onConfigureOverlay = { screen = Screen.Settings },
                            onStartFrictionDemo = { target ->
                                frictionTarget = target
                                screen = Screen.Friction
                            },
                        )

                        Screen.Apps -> AppSelectionScreen(
                            selectedPackages = selectedPackages,
                            onSelectionChanged = { nextSelection ->
                                selectedPackages = nextSelection
                                preferences.saveSelectedPackages(nextSelection)
                            },
                        )

                        Screen.Focus -> FocusScreen()

                        Screen.Settings -> SettingsScreen(
                            selectedPackages = selectedPackages,
                            dailyLimitMinutes = dailyLimitMinutes,
                            onDailyLimitChanged = { minutes ->
                                dailyLimitMinutes = minutes
                                preferences.saveDailyLimitMinutes(minutes)
                            },
                            onStartFrictionDemo = { target ->
                                frictionTarget = target
                                screen = Screen.Friction
                            },
                        )

                        Screen.Friction -> FrictionScreen(
                            target = frictionTarget,
                            onCancel = {
                                frictionTarget = null
                                screen = Screen.Dashboard
                            },
                            onContinue = {
                                frictionTarget = null
                                screen = Screen.Dashboard
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPanel(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF181818),
                        Color(0xFF050505),
                        Color(0xFF000000),
                    ),
                ),
            )
            .border(1.dp, WellnessLime.copy(alpha = 0.22f), RoundedCornerShape(32.dp))
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark(size = 34)
            Text(
                text = "FRICTION",
                style = MaterialTheme.typography.labelLarge,
                color = WellnessLime,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = WellnessMuted,
        )
        content()
    }
}

@Composable
private fun BrandMark(size: Int = 36) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 2).dp))
            .background(WellnessLime),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "F",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.32f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun MetricBubble(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        color.copy(alpha = 0.36f),
                        color.copy(alpha = 0.12f),
                        WellnessCardSoft,
                    ),
                ),
            )
            .border(1.dp, color.copy(alpha = 0.24f), RoundedCornerShape(26.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = WellnessMuted)
    }
}

@Composable
private fun PermissionLine(label: String, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        StatusChip(
            text = if (enabled) "Enabled" else "Needed",
            color = if (enabled) WellnessLime else WellnessAmber,
        )
    }
}

@Composable
private fun PrimaryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WellnessMint,
            contentColor = Color.Black,
            disabledContainerColor = WellnessCardSoft,
            disabledContentColor = WellnessMuted,
        ),
    ) {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun QuietButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = WellnessLime),
    ) {
        content()
    }
}

@Composable
private fun RepeatModeButton(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) WellnessLime else WellnessCardSoft,
            contentColor = if (selected) Color.Black else WellnessText,
            disabledContainerColor = WellnessCardSoft.copy(alpha = 0.55f),
            disabledContentColor = WellnessMuted,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OverlayModeSelector(
    repeatMode: String,
    ultraFocusUntil: Long,
    lightMinutes: Int,
    heavyMinutes: Int,
    ultraMinutes: Int,
    onLightMinutesChanged: (Int) -> Unit,
    onHeavyMinutesChanged: (Int) -> Unit,
    onUltraMinutesChanged: (Int) -> Unit,
    onModeChanged: (String, Long) -> Unit,
) {
    val nowMillis = System.currentTimeMillis()
    val ultraFocusActive = repeatMode == OverlayRepeatMode.ULTRA_FOCUS &&
        ultraFocusUntil > nowMillis

    RepeatModeButton(
        title = "Light",
        subtitle = "Easy questions. Repeats after $lightMinutes min.",
        selected = repeatMode == OverlayRepeatMode.LIGHT,
        enabled = !ultraFocusActive,
        onClick = { onModeChanged(OverlayRepeatMode.LIGHT, 0L) },
    )
    RepeatModeButton(
        title = "Heavy",
        subtitle = "Harder puzzles. Repeats after $heavyMinutes min.",
        selected = repeatMode == OverlayRepeatMode.HEAVY,
        enabled = !ultraFocusActive,
        onClick = { onModeChanged(OverlayRepeatMode.HEAVY, 0L) },
    )
    RepeatModeButton(
        title = "Ultra Focus",
        subtitle = if (ultraFocusActive) {
            "Locked for ${((ultraFocusUntil - nowMillis) / 60000L).coerceAtLeast(1L)} more min."
        } else {
            "Starts a $ultraMinutes-minute target-app lock. Cannot be turned off inside the app until it ends."
        },
        selected = repeatMode == OverlayRepeatMode.ULTRA_FOCUS,
        enabled = !ultraFocusActive,
        onClick = {
            onModeChanged(
                OverlayRepeatMode.ULTRA_FOCUS,
                System.currentTimeMillis() + OverlayRepeatMode.minutesToMillis(ultraMinutes),
            )
        },
    )
    ModeMinutesField(
        label = "Light repeat minutes",
        value = lightMinutes,
        enabled = !ultraFocusActive,
        onValueChanged = onLightMinutesChanged,
    )
    ModeMinutesField(
        label = "Heavy repeat minutes",
        value = heavyMinutes,
        enabled = !ultraFocusActive,
        onValueChanged = onHeavyMinutesChanged,
    )
    ModeMinutesField(
        label = "Ultra Focus minutes",
        value = ultraMinutes,
        enabled = !ultraFocusActive,
        onValueChanged = onUltraMinutesChanged,
    )
    if (ultraFocusActive) {
        Text(
            text = "Ultra Focus is active. Mode switching unlocks when the timer ends.",
            color = WellnessAmber,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ModeMinutesField(
    label: String,
    value: Int,
    enabled: Boolean,
    onValueChanged: (Int) -> Unit,
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = textValue,
        enabled = enabled,
        onValueChange = { next ->
            val filtered = next.filter(Char::isDigit).take(3)
            textValue = filtered
            filtered.toIntOrNull()?.takeIf { it > 0 }?.let(onValueChanged)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun appBackgroundBrush(): Brush =
    Brush.verticalGradient(
        listOf(
            Color.Black,
            WellnessInk,
            Color(0xFF101010),
        ),
    )

private fun frictionTypography(): Typography {
    val base = FontFamily.SansSerif
    return Typography(
        displaySmall = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            lineHeight = 31.sp,
            letterSpacing = 0.sp,
        ),
        titleLarge = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        labelLarge = TextStyle(
            fontFamily = base,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.8.sp,
        ),
    )
}

@Composable
private fun DashboardScreen(
    selectedPackages: Set<String>,
    dailyLimitMinutes: Int,
    onChooseApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onConfigureOverlay: () -> Unit,
    onStartFrictionDemo: (FrictionTarget) -> Unit,
) {
    val context = LocalContext.current
    var refreshCount by remember { mutableIntStateOf(0) }
    var usageState by remember { mutableStateOf<DashboardUsageState>(DashboardUsageState.Loading) }
    var repeatMode by remember { mutableStateOf(AppSettings.overlayRepeatMode(context)) }
    var ultraFocusUntil by remember { mutableStateOf(AppSettings.ultraFocusUntilMillis(context)) }
    var lightMinutes by remember { mutableIntStateOf(AppSettings.lightModeMinutes(context)) }
    var heavyMinutes by remember { mutableIntStateOf(AppSettings.heavyModeMinutes(context)) }
    var ultraMinutes by remember { mutableIntStateOf(AppSettings.ultraFocusMinutes(context)) }

    LaunchedEffect(selectedPackages, dailyLimitMinutes, refreshCount) {
        usageState = when {
            selectedPackages.isEmpty() -> DashboardUsageState.Empty
            !hasUsageAccess(context) -> DashboardUsageState.PermissionDenied
            else -> withContext(Dispatchers.IO) {
                val selectedUsage = loadTodayUsageForSelectedApps(context, selectedPackages)
                val usageMinutesByPackage = selectedUsage.associate {
                    it.packageName to it.todayUsageMinutes
                }
                val summary = UsageLimitCalculator.summarize(
                    selectedPackages,
                    usageMinutesByPackage,
                    dailyLimitMinutes,
                    true,
                )
                DashboardUsageState.Loaded(selectedUsage, summary)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HeroPanel(
            title = "Make opening apps intentional.",
            subtitle = "Track selected apps, set a daily limit, and add a real pause before autopilot takes over.",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip("${selectedPackages.size} selected", WellnessLime)
                StatusChip("$dailyLimitMinutes min limit", WellnessLime)
            }
        }

        DashboardCard(title = "Selected apps") {
            Text("${selectedPackages.size} target apps selected")
            PrimaryButton(onClick = onChooseApps) {
                Text("Select apps")
            }
        }

        DashboardCard(title = "Daily limit") {
            Text("$dailyLimitMinutes minutes")
            QuietButton(onClick = onOpenSettings) {
                Text("Edit limit")
            }
        }

        DashboardCard(title = "Focus mode") {
            OverlayModeSelector(
                repeatMode = repeatMode,
                ultraFocusUntil = ultraFocusUntil,
                lightMinutes = lightMinutes,
                heavyMinutes = heavyMinutes,
                ultraMinutes = ultraMinutes,
                onLightMinutesChanged = { minutes ->
                    lightMinutes = minutes
                    AppSettings.saveLightModeMinutes(context, minutes)
                },
                onHeavyMinutesChanged = { minutes ->
                    heavyMinutes = minutes
                    AppSettings.saveHeavyModeMinutes(context, minutes)
                },
                onUltraMinutesChanged = { minutes ->
                    ultraMinutes = minutes
                    AppSettings.saveUltraFocusMinutes(context, minutes)
                },
                onModeChanged = { mode, untilMillis ->
                    repeatMode = mode
                    ultraFocusUntil = untilMillis
                    AppSettings.saveOverlayRepeatMode(context, mode)
                    AppSettings.saveUltraFocusUntilMillis(context, untilMillis)
                    if (mode == OverlayRepeatMode.ULTRA_FOCUS) {
                        AppSettings.saveOverlayBlockerEnabled(context, true)
                        AppSettings.saveStrictOverlayMode(context, true)
                    }
                },
            )
        }

        when (val state = usageState) {
            DashboardUsageState.Empty -> DashboardCard(title = "Today's usage") {
                Text("No apps selected yet.")
            }

            DashboardUsageState.Loading -> DashboardCard(title = "Today's usage") {
                Text("Loading today's usage...")
            }

            DashboardUsageState.PermissionDenied -> {
                DashboardCard(title = "Today's usage") {
                    Text(
                        text = "Usage Access is required to show today's selected-app usage.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is DashboardUsageState.Loaded -> {
                val frictionTargets = state.apps
                    .filter { FrictionStateCalculator.isAtOrOverLimit(it.todayUsageMinutes, dailyLimitMinutes) }
                    .map { app ->
                        FrictionTarget(
                            label = app.label,
                            packageName = app.packageName,
                        )
                    }

                DashboardCard(title = "Today's usage") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricBubble(
                            value = "${state.summary.totalUsageMinutes}",
                            label = "Minutes used",
                            color = WellnessLime,
                            modifier = Modifier.weight(1f),
                        )
                        MetricBubble(
                            value = "${state.summary.dailyLimitMinutes}",
                            label = "Daily limit",
                            color = WellnessLime,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    StatusChip(state.summary.status.dashboardLabel(), WellnessLime)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(140.dp),
                    ) {
                        items(state.apps, key = { it.packageName }) { app ->
                            Column {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${app.todayUsageMinutes} min today - ${app.packageName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuietButton(onClick = { refreshCount += 1 }) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WellnessStroke, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = WellnessCard.copy(alpha = 0.92f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun OverlayBlockerSettingsScreen() {
    val context = LocalContext.current
    var overlayEnabled by remember { mutableStateOf(AppSettings.overlayBlockerEnabled(context)) }
    var strictMode by remember { mutableStateOf(AppSettings.strictOverlayMode(context)) }
    var refreshCount by remember { mutableIntStateOf(0) }
    val usageAccessGranted = hasUsageAccess(context)
    val overlayPermissionGranted = hasOverlayPermission(context)
    val accessibilityEnabled = isOverlayAccessibilityServiceEnabled(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroPanel(
            title = "Overlay blocker",
            subtitle = "Opt-in friction for selected apps. Instagram starts at package-level detection.",
        ) {
            StatusChip("Experimental", WellnessAmber)
        }

        DashboardCard(title = "Mode") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Overlay Blocker Mode", fontWeight = FontWeight.SemiBold)
                    Text("Default is off. Enable only after reviewing the permissions.")
                }
                Switch(
                    checked = overlayEnabled,
                    onCheckedChange = { enabled ->
                        overlayEnabled = enabled
                        AppSettings.saveOverlayBlockerEnabled(context, enabled)
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strict overlay mode", fontWeight = FontWeight.SemiBold)
                    Text("Show friction for selected apps even before the daily limit.")
                }
                Switch(
                    checked = strictMode,
                    onCheckedChange = { enabled ->
                        strictMode = enabled
                        AppSettings.saveStrictOverlayMode(context, enabled)
                    },
                )
            }
        }

        DashboardCard(title = "Setup status") {
            Text("Usage Access: ${if (usageAccessGranted) "Enabled" else "Required"}")
            Text("Accessibility Service: ${if (accessibilityEnabled) "Enabled" else "Required"}")
            Text("Display over other apps: ${if (overlayPermissionGranted) "Enabled" else "Required"}")
            Text("Selected target apps: ${AppSettings.selectedPackages(context).size}")
        }

        DashboardCard(title = "Permissions") {
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) {
                Text("Open Usage Access settings")
            }
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                Text("Open Accessibility settings")
            }
            Button(onClick = { context.startActivity(overlayPermissionIntent(context)) }) {
                Text("Open Display over other apps settings")
            }
            TextButton(onClick = { refreshCount += 1 }) {
                Text("Refresh setup status")
            }
            if (refreshCount > 0) {
                Text("Status refreshed.")
            }
        }

        Text(
            text = "After you complete friction, the same target app gets a short 2-minute allow window before the overlay can return. Detection may vary by Android version and OEM. This mode does not read private screen text and does not upload data.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun FrictionScreen(
    target: FrictionTarget?,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
) {
    var remainingSeconds by remember(target) {
        mutableIntStateOf(FrictionStateCalculator.COUNTDOWN_SECONDS)
    }
    var intentionText by remember(target) { mutableStateOf("") }

    LaunchedEffect(target) {
        remainingSeconds = FrictionStateCalculator.COUNTDOWN_SECONDS
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
        }
    }

    val appLabel = target?.label ?: "this app"
    val packageName = target?.packageName.orEmpty()
    val context = LocalContext.current
    val repeatMode = AppSettings.overlayRepeatMode(context)
    val challengeIndex = remember(target) {
        FrictionChallenge.indexFor(packageName, System.currentTimeMillis())
    }
    val challengeText = FrictionChallenge.promptFor(challengeIndex, repeatMode)
    val answerValid = FrictionChallenge.isAnswerValid(challengeIndex, intentionText, repeatMode)
    val canContinue = answerValid && FrictionStateCalculator.canContinue(
        remainingSeconds,
        intentionText,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroPanel(
            title = "Pause before $appLabel",
            subtitle = packageName.ifBlank { "Answer the challenge before continuing." },
        ) {
            StatusChip(
                text = if (remainingSeconds > 0) "$remainingSeconds seconds" else "Gate open",
                color = if (remainingSeconds > 0) WellnessAmber else WellnessLime,
            )
        }
        DashboardCard(title = "Challenge") {
            Text(challengeText, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (answerValid) {
                    "Answer accepted"
                } else {
                    "Correct answer needed. The app will not reveal it."
                },
                color = if (answerValid) WellnessLime else WellnessMuted,
            )
            OutlinedTextField(
                value = intentionText,
                onValueChange = { intentionText = it },
                label = { Text("Answer + why you want to continue") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton(
                    enabled = canContinue,
                    onClick = onContinue,
                ) {
                    Text("Continue")
                }
                QuietButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun AppSelectionScreen(
    selectedPackages: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            loadLaunchableApps(context)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        HeroPanel(
            title = "Choose your targets",
            subtitle = "Selected apps can show friction once they hit the daily limit or strict overlay mode is on.",
        )
        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> Text("Loading apps...")
            apps.isEmpty() -> Text("No launchable apps found.")
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(apps, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        selected = app.packageName in selectedPackages,
                        onSelectedChanged = { selected ->
                            val nextSelection = if (selected) {
                                selectedPackages + app.packageName
                            } else {
                                selectedPackages - app.packageName
                            }
                            onSelectionChanged(nextSelection)
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    selected: Boolean,
    onSelectedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WellnessCard.copy(alpha = 0.78f))
            .border(1.dp, WellnessStroke.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (app.icon != null) {
            Image(
                bitmap = app.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(app.label.firstOrNull()?.uppercase() ?: "?")
            }
        }

        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = WellnessMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Checkbox(
            checked = selected,
            onCheckedChange = onSelectedChanged,
        )
    }
}

@Composable
private fun FocusScreen() {
    val context = LocalContext.current
    val repeatMode = AppSettings.overlayRepeatMode(context)
    val ultraUntil = AppSettings.ultraFocusUntilMillis(context)
    val now = System.currentTimeMillis()
    val ultraActive = repeatMode == OverlayRepeatMode.ULTRA_FOCUS && ultraUntil > now
    val selectedCount = AppSettings.selectedPackages(context).size
    val modeLabel = when (repeatMode) {
        OverlayRepeatMode.LIGHT -> "Light"
        OverlayRepeatMode.ULTRA_FOCUS -> "Ultra Focus"
        else -> "Heavy"
    }
    val modeCopy = when {
        ultraActive -> "Target apps are locked for ${((ultraUntil - now) / 60000L).coerceAtLeast(1L)} more min."
        repeatMode == OverlayRepeatMode.LIGHT -> "Friction repeats after 10 minutes."
        repeatMode == OverlayRepeatMode.ULTRA_FOCUS -> "Ultra Focus is ready. Start it from Settings."
        else -> "Friction repeats after 2 minutes."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroPanel(
            title = "Focus is a mode, not a mood.",
            subtitle = "Your current blocker profile controls how aggressively Friction interrupts target apps.",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(modeLabel, WellnessLime)
                StatusChip("$selectedCount targets", WellnessText)
            }
        }

        DashboardCard(title = "Current mode") {
            Text(
                text = modeLabel,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Text(modeCopy, color = WellnessMuted)
        }

        DashboardCard(title = "How it behaves") {
            Text("Light gives breathing room with a longer repeat window.")
            Text("Heavy keeps pressure on with a 2-minute repeat.")
            Text("Ultra Focus blocks selected target apps until its timer ends.")
            Text("Change modes from Settings.", color = WellnessMuted)
        }
    }
}

@Composable
private fun UsageAccessScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsageAccess(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroPanel(
            title = if (hasPermission) "Usage Access enabled" else "Usage Access needed",
            subtitle = "Friction reads selected-app totals locally so the dashboard can compare usage with your limit.",
        ) {
            StatusChip(if (hasPermission) "Enabled" else "Required", if (hasPermission) WellnessLime else WellnessAmber)
        }
        DashboardCard(title = "Permission") {
            PrimaryButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                },
            ) {
                Text("Open Usage Access settings")
            }
            QuietButton(onClick = { hasPermission = hasUsageAccess(context) }) {
                Text("Refresh permission status")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    selectedPackages: Set<String>,
    dailyLimitMinutes: Int,
    onDailyLimitChanged: (Int) -> Unit,
    onStartFrictionDemo: (FrictionTarget) -> Unit,
) {
    val context = LocalContext.current
    var textValue by remember(dailyLimitMinutes) {
        mutableStateOf(dailyLimitMinutes.toString())
    }
    var overlayEnabled by remember { mutableStateOf(AppSettings.overlayBlockerEnabled(context)) }
    var strictMode by remember { mutableStateOf(AppSettings.strictOverlayMode(context)) }
    var repeatMode by remember { mutableStateOf(AppSettings.overlayRepeatMode(context)) }
    var ultraFocusUntil by remember { mutableStateOf(AppSettings.ultraFocusUntilMillis(context)) }
    var refreshCount by remember { mutableIntStateOf(0) }
    val parsedValue = textValue.toIntOrNull()
    val isValid = parsedValue != null && parsedValue > 0
    val usageAccessGranted = hasUsageAccess(context)
    val overlayPermissionGranted = hasOverlayPermission(context)
    val accessibilityEnabled = isOverlayAccessibilityServiceEnabled(context)
    val nowMillis = System.currentTimeMillis()
    val ultraFocusActive = repeatMode == OverlayRepeatMode.ULTRA_FOCUS &&
        ultraFocusUntil > nowMillis

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroPanel(
            title = "Settings",
            subtitle = "Daily limits, overlay blocker controls, and permission setup in one place.",
        ) {
            StatusChip("$dailyLimitMinutes minutes now", WellnessLime)
        }
        DashboardCard(title = "Limit") {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it.filter(Char::isDigit).take(4) },
                label = { Text("Daily limit in minutes") },
                isError = textValue.isNotEmpty() && !isValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton(
                enabled = isValid,
                onClick = {
                    parsedValue?.let(onDailyLimitChanged)
                },
            ) {
                Text("Save limit")
            }
        }

        DashboardCard(title = "Friction status") {
            Text("${selectedPackages.size} selected target app(s)")
            Text("$dailyLimitMinutes minute daily limit")
            Text("Challenge validation is active. The answer is not revealed in the overlay.")
            val firstTargetPackage = selectedPackages.firstOrNull()
            PrimaryButton(
                enabled = firstTargetPackage != null,
                onClick = {
                    firstTargetPackage?.let { packageName ->
                        onStartFrictionDemo(
                            FrictionTarget(
                                label = loadApplicationLabel(context.packageManager, packageName),
                                packageName = packageName,
                            ),
                        )
                    }
                },
            ) {
                Text("Test friction")
            }
        }

        DashboardCard(title = "Overlay blocker") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Overlay Blocker Mode", fontWeight = FontWeight.SemiBold)
                    Text("Optional. Shows friction over selected apps.")
                }
                Switch(
                    checked = overlayEnabled,
                    enabled = !ultraFocusActive,
                    onCheckedChange = { enabled ->
                        overlayEnabled = enabled
                        AppSettings.saveOverlayBlockerEnabled(context, enabled)
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strict mode", fontWeight = FontWeight.SemiBold)
                    Text("Show friction before the daily limit during testing.")
                }
                Switch(
                    checked = strictMode,
                    enabled = !ultraFocusActive,
                    onCheckedChange = { enabled ->
                        strictMode = enabled
                        AppSettings.saveStrictOverlayMode(context, enabled)
                    },
                )
            }
            Text(
                text = if (ultraFocusActive) {
                    "Ultra Focus is active. Overlay settings unlock when the timer ends."
                } else {
                    "Mode selection is on Home. Permissions remain here."
                },
                color = if (ultraFocusActive) WellnessAmber else WellnessMuted,
            )
        }

        DashboardCard(title = "Overlay permissions") {
            PermissionLine("Usage Access", usageAccessGranted)
            PermissionLine("Accessibility Service", accessibilityEnabled)
            PermissionLine("Display over other apps", overlayPermissionGranted)
            Text("Overlay mode is opt-in and detects selected apps by package name. It does not read private screen text.")
            PrimaryButton(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) {
                Text("Open Usage Access")
            }
            PrimaryButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                Text("Open Accessibility")
            }
            PrimaryButton(onClick = { context.startActivity(overlayPermissionIntent(context)) }) {
                Text("Open Overlay Permission")
            }
            QuietButton(onClick = { refreshCount += 1 }) {
                Text("Refresh setup status")
            }
            if (refreshCount > 0) {
                Text("Status refreshed.")
            }
        }
    }
}

private fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return queryLaunchableActivities(packageManager, intent)
        .mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            InstalledApp(
                label = resolveInfo.loadLabel(packageManager).toString(),
                packageName = packageName,
                icon = runCatching {
                    resolveInfo.loadIcon(packageManager).toImageBitmap()
                }.getOrNull(),
            )
        }
        .distinctBy { it.packageName }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
}

private fun loadTodayUsageForSelectedApps(
    context: Context,
    selectedPackages: Set<String>,
): List<SelectedAppUsage> {
    val packageManager = context.packageManager
    val usageMinutesByPackage = queryTodayUsageMinutes(context, selectedPackages)
    return selectedPackages
        .sorted()
        .map { packageName ->
            SelectedAppUsage(
                label = loadApplicationLabel(packageManager, packageName),
                packageName = packageName,
                todayUsageMinutes = usageMinutesByPackage[packageName] ?: 0,
            )
        }
}

private fun queryTodayUsageMinutes(
    context: Context,
    selectedPackages: Set<String>,
): Map<String, Int> {
    if (selectedPackages.isEmpty()) {
        return emptyMap()
    }

    val usageStatsManager =
        context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()
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
    ) ?: emptyList()

    return usageStats
        .filter { it.packageName in selectedPackages }
        .usageMillisByPackage()
        .mapValues { (_, usageMillis) -> usageMillis.toDisplayMinutes() }
}

private fun List<UsageStats>.usageMillisByPackage(): Map<String, Long> {
    val usageByPackage = mutableMapOf<String, Long>()
    forEach { usageStats ->
        usageByPackage[usageStats.packageName] =
            usageByPackage.getOrDefault(usageStats.packageName, 0L) +
                usageStats.totalTimeInForeground
    }
    return usageByPackage
}

private fun loadApplicationLabel(
    packageManager: PackageManager,
    packageName: String,
): String {
    val applicationInfo = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

private fun queryLaunchableActivities(
    packageManager: PackageManager,
    intent: Intent,
): List<ResolveInfo> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
    }

private fun Drawable.toImageBitmap(): ImageBitmap {
    val width = intrinsicWidth.takeIf { it > 0 } ?: 48
    val height = intrinsicHeight.takeIf { it > 0 } ?: 48
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}

private fun hasUsageAccess(context: Context): Boolean {
    val appOpsManager = context.getSystemService(AppOpsManager::class.java) ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
    } else {
        @Suppress("DEPRECATION")
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
    }

    return mode == AppOpsManager.MODE_ALLOWED
}

private fun hasOverlayPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

private fun overlayPermissionIntent(context: Context): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        )
    } else {
        Intent(Settings.ACTION_SETTINGS)
    }

private fun isOverlayAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedService = ComponentName(
        context,
        OverlayBlockerAccessibilityService::class.java,
    ).flattenToString()
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

    return enabledServices
        .split(':')
        .any { it.equals(expectedService, ignoreCase = true) }
}

private fun Long.toDisplayMinutes(): Int {
    if (this <= 0L) {
        return 0
    }
    return ceil(this / MILLIS_PER_MINUTE.toDouble()).toInt()
}

private fun UsageLimitCalculator.Status.dashboardLabel(): String =
    when (this) {
        UsageLimitCalculator.Status.NO_APPS_SELECTED -> "No apps selected"
        UsageLimitCalculator.Status.PERMISSION_REQUIRED -> "Usage Access required"
        UsageLimitCalculator.Status.BELOW_LIMIT -> "Below daily limit"
        UsageLimitCalculator.Status.OVER_LIMIT -> "At or over daily limit"
    }

private const val MILLIS_PER_MINUTE = 60L * 1000L
private val WellnessDeep = Color(0xFF000000)
private val WellnessInk = Color(0xFF080808)
private val WellnessCard = Color(0xFF141414)
private val WellnessCardSoft = Color(0xFF202020)
private val WellnessStroke = Color(0xFF343434)
private val WellnessMint = Color(0xFFFFD84D)
private val WellnessLime = Color(0xFFFFD84D)
private val WellnessAqua = Color(0xFFFFFFFF)
private val WellnessAmber = Color(0xFFFFD84D)
private val WellnessText = Color(0xFFF7F7F2)
private val WellnessMuted = Color(0xFFB7B7B0)
