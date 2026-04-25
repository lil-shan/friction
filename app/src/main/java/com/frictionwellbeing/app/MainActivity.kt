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
import androidx.compose.material3.Card
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    Dashboard("Dashboard"),
    Apps("Apps"),
    UsageAccess("Usage Access"),
    Settings("Settings"),
    Overlay("Overlay"),
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
            primary = Color(0xFF48E0B8),
            secondary = Color(0xFFFFB86B),
            tertiary = Color(0xFF8EA7FF),
            background = Color(0xFF090D12),
            surface = Color(0xFF121922),
            surfaceVariant = Color(0xFF1A2430),
            onPrimary = Color(0xFF06231C),
            onSecondary = Color(0xFF2B1700),
            onBackground = Color(0xFFE8EEF5),
            onSurface = Color(0xFFE8EEF5),
            onSurfaceVariant = Color(0xFFC6D0DA),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(screen.title) })
                },
                bottomBar = {
                    NavigationBar {
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
                        .padding(innerPadding),
                ) {
                    when (screen) {
                        Screen.Dashboard -> DashboardScreen(
                            selectedPackages = selectedPackages,
                            dailyLimitMinutes = dailyLimitMinutes,
                            onChooseApps = { screen = Screen.Apps },
                            onOpenSettings = { screen = Screen.Settings },
                            onOpenUsageAccess = { screen = Screen.UsageAccess },
                            onConfigureOverlay = { screen = Screen.Overlay },
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

                        Screen.UsageAccess -> UsageAccessScreen()

                        Screen.Settings -> SettingsScreen(
                            dailyLimitMinutes = dailyLimitMinutes,
                            onDailyLimitChanged = { minutes ->
                                dailyLimitMinutes = minutes
                                preferences.saveDailyLimitMinutes(minutes)
                            },
                        )

                        Screen.Overlay -> OverlayBlockerSettingsScreen()

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
    val usageAccessGranted = hasUsageAccess(context)
    val overlayBlockerEnabled = AppSettings.overlayBlockerEnabled(context)
    val overlayPermissionGranted = hasOverlayPermission(context)
    val accessibilityEnabled = isOverlayAccessibilityServiceEnabled(context)

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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        DashboardCard(title = "Selected apps") {
            Text("${selectedPackages.size} target apps selected")
            Button(onClick = onChooseApps) {
                Text("Select apps")
            }
        }

        DashboardCard(title = "Daily limit") {
            Text("$dailyLimitMinutes minutes")
            TextButton(onClick = onOpenSettings) {
                Text("Edit limit")
            }
        }

        DashboardCard(title = "Permissions status") {
            Text("Usage Access: ${if (usageAccessGranted) "Enabled" else "Needed"}")
            Text("Accessibility Service: ${if (accessibilityEnabled) "Enabled" else "Needed for overlay blocker"}")
            Text("Display over other apps: ${if (overlayPermissionGranted) "Enabled" else "Needed for overlay blocker"}")
            Button(onClick = onOpenUsageAccess) {
                Text("Open Usage Access settings")
            }
            TextButton(onClick = onConfigureOverlay) {
                Text("Configure overlay blocker")
            }
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
                    Text(
                        text = "${state.summary.totalUsageMinutes} of ${state.summary.dailyLimitMinutes} minutes used today",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = state.summary.status.dashboardLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
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

                DashboardCard(title = "Friction status") {
                    Text(if (frictionTargets.isEmpty()) {
                        "No selected app is at or over the daily limit."
                    } else {
                        "${frictionTargets.size} selected app(s) can show friction."
                    })
                    Button(
                        enabled = frictionTargets.isNotEmpty(),
                        onClick = { frictionTargets.firstOrNull()?.let(onStartFrictionDemo) },
                    ) {
                        Text("Test friction screen")
                    }
                    if (frictionTargets.isNotEmpty()) {
                        Text("Available demo targets:")
                    }
                    frictionTargets.forEach { target ->
                        Text("${target.label} (${target.packageName})")
                    }
                }

                DashboardCard(title = "Overlay blocker") {
                    Text(if (overlayBlockerEnabled) {
                        "Overlay Blocker Mode is on."
                    } else {
                        "Overlay Blocker Mode is off."
                    })
                    Text("Instagram package support starts with com.instagram.android.")
                    Text("After Continue, friction can return after about 2 minutes.")
                    Button(onClick = onConfigureOverlay) {
                        Text("Configure overlay blocker")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = { refreshCount += 1 }) {
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
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
        Text("Overlay Blocker Mode", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Optional experimental mode. It detects selected target apps such as Instagram by package name and can show a friction overlay before you continue. It does not detect Reels or Shorts yet.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "This requires Usage Access, Accessibility Service, and Display over other apps permission. You must enable each permission yourself.",
            style = MaterialTheme.typography.bodyMedium,
        )

        DashboardCard(title = "Mode") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Overlay Blocker Mode")
                    Text("Default is off. Enable only if you understand the permissions.")
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
                    Text("Strict overlay mode")
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

    val canContinue = FrictionStateCalculator.canContinue(
        remainingSeconds,
        intentionText,
    )
    val appLabel = target?.label ?: "this app"
    val packageName = target?.packageName.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Pause before opening $appLabel",
            style = MaterialTheme.typography.headlineSmall,
        )
        if (packageName.isNotBlank()) {
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = if (remainingSeconds > 0) {
                "Continue available in $remainingSeconds seconds"
            } else {
                "Countdown complete"
            },
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = intentionText,
            onValueChange = { intentionText = it },
            label = { Text("Answer this, then say why: Which country has Reykjavik as its capital?") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                enabled = canContinue,
                onClick = onContinue,
            ) {
                Text("Continue")
            }
            TextButton(onClick = onCancel) {
                Text("Cancel")
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
        Text(
            text = "Pick apps where friction should eventually apply.",
            style = MaterialTheme.typography.bodyMedium,
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
            .padding(vertical = 12.dp),
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
private fun UsageAccessScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasUsageAccess(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (hasPermission) {
                "Usage Access is enabled."
            } else {
                "Usage Access is not enabled."
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Friction Wellbeing uses Usage Access to show today's foreground usage for selected apps. This MVP shows usage and limit status; it does not enforce limits yet.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            },
        ) {
            Text("Open Usage Access settings")
        }
        TextButton(onClick = { hasPermission = hasUsageAccess(context) }) {
            Text("Refresh permission status")
        }
    }
}

@Composable
private fun SettingsScreen(
    dailyLimitMinutes: Int,
    onDailyLimitChanged: (Int) -> Unit,
) {
    var textValue by remember(dailyLimitMinutes) {
        mutableStateOf(dailyLimitMinutes.toString())
    }
    val parsedValue = textValue.toIntOrNull()
    val isValid = parsedValue != null && parsedValue > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Set the default daily limit for selected apps.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it.filter(Char::isDigit).take(4) },
            label = { Text("Daily limit in minutes") },
            isError = textValue.isNotEmpty() && !isValid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            enabled = isValid,
            onClick = {
                parsedValue?.let(onDailyLimitChanged)
            },
        ) {
            Text("Save limit")
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
