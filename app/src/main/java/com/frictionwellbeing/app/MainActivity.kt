package com.frictionwellbeing.app

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
}

private data class InstalledApp(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap?,
)

private class FrictionPreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("friction_wellbeing", Context.MODE_PRIVATE)

    fun selectedPackages(): Set<String> =
        preferences.getStringSet(KEY_SELECTED_PACKAGES, emptySet()).orEmpty().toSet()

    fun saveSelectedPackages(packageNames: Set<String>) {
        preferences.edit().putStringSet(KEY_SELECTED_PACKAGES, packageNames).apply()
    }

    fun dailyLimitMinutes(): Int =
        preferences.getInt(KEY_DAILY_LIMIT_MINUTES, DEFAULT_DAILY_LIMIT_MINUTES)

    fun saveDailyLimitMinutes(minutes: Int) {
        preferences.edit().putInt(KEY_DAILY_LIMIT_MINUTES, minutes).apply()
    }

    private companion object {
        const val KEY_SELECTED_PACKAGES = "selected_packages"
        const val KEY_DAILY_LIMIT_MINUTES = "daily_limit_minutes"
        const val DEFAULT_DAILY_LIMIT_MINUTES = 15
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrictionApp(preferences: FrictionPreferences) {
    var screen by remember { mutableStateOf(Screen.Dashboard) }
    var selectedPackages by remember { mutableStateOf(preferences.selectedPackages()) }
    var dailyLimitMinutes by remember { mutableStateOf(preferences.dailyLimitMinutes()) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF1E6B5C),
            secondary = androidx.compose.ui.graphics.Color(0xFF8C5E2A),
            tertiary = androidx.compose.ui.graphics.Color(0xFF3D5A80),
            background = androidx.compose.ui.graphics.Color(0xFFFCFBF7),
            surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text(screen.title) })
                },
                bottomBar = {
                    NavigationBar {
                        Screen.entries.forEach { item ->
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Daily limit",
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = "$dailyLimitMinutes minutes",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Selected apps: ${selectedPackages.size}",
            style = MaterialTheme.typography.titleMedium,
        )

        if (selectedPackages.isEmpty()) {
            Text("No apps selected yet.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(selectedPackages.sorted()) { packageName ->
                    Text(packageName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onChooseApps) {
                Text("Choose apps")
            }
            TextButton(onClick = onOpenSettings) {
                Text("Edit limit")
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
                    Divider()
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
            text = "Friction Wellbeing will need Usage Access later to understand foreground app usage. This MVP only explains and opens the Android settings screen; it does not enforce limits yet.",
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
    val appOpsManager = context.getSystemService(AppOpsManager::class.java)
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

    if (mode != AppOpsManager.MODE_ALLOWED) {
        return false
    }

    val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
    val now = System.currentTimeMillis()
    val recentStats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        now - ONE_DAY_MILLIS,
        now,
    )
    return recentStats.isNotEmpty()
}

private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
