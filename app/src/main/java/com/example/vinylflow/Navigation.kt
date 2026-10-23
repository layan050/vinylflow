package com.example.vinylflow

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.vinylflow.domain.model.PRESET_VINTAGE_CREAM
import com.example.vinylflow.domain.model.VinylTheme
import com.example.vinylflow.ui.customize.CustomizeScreen
import com.example.vinylflow.ui.player.PlayerScreen
import com.example.vinylflow.ui.player.TabletPlayerScreen
import com.example.vinylflow.ui.settings.SettingsScreen
import com.example.vinylflow.ui.sources.SourcesScreen
import com.example.vinylflow.widget.VinylBaseWidgetProvider
import com.example.vinylflow.theme.VinylFlowTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

// ── Nav Destinations — must implement NavKey ──────────────────────────────────

@Serializable
sealed interface Dest : NavKey {
    @Serializable data object Player   : Dest
    @Serializable data object Sources  : Dest
    @Serializable data object Customize: Dest
    @Serializable data object Settings : Dest
}

// ── Main Navigation ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainNavigation(activity: android.app.Activity) {
    val backStack = rememberNavBackStack(Dest.Player)
    val prefs = remember { activity.getSharedPreferences("vinylflow_appearance", android.content.Context.MODE_PRIVATE) }
    var theme by remember {
        mutableStateOf(PRESET_VINTAGE_CREAM.copy(
            backgroundColor = prefs.getLong("background", PRESET_VINTAGE_CREAM.backgroundColor),
            darkMode = prefs.getBoolean("dark", false)
        ))
    }
    val widgetPrefs = remember { activity.getSharedPreferences("vinylflow_widget", android.content.Context.MODE_PRIVATE) }
    var widgetAccent by remember { mutableLongStateOf(widgetPrefs.getLong("accent", 0xFFB6AFFF)) }
    var widgetSurface by remember { mutableLongStateOf(widgetPrefs.getLong("surface", 0xFF24242A)) }
    var widgetShowControls by remember { mutableStateOf(widgetPrefs.getBoolean("controls", true)) }
    var widgetShowArtist by remember { mutableStateOf(widgetPrefs.getBoolean("artist", true)) }

    // Auto-detect tablet (Xiaomi Pad 7 → Expanded width in landscape)
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    VinylFlowTheme(darkMode = theme.darkMode) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    NavDisplay(
        backStack = backStack,
        onBack    = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<Dest.Player> {
                if (isTablet) {
                    TabletPlayerScreen(
                        theme           = theme,
                        onOpenSources   = { backStack.add(Dest.Sources) },
                        onOpenCustomize = { backStack.add(Dest.Customize) },
                        onOpenSettings  = { backStack.add(Dest.Settings) }
                    )
                } else {
                    PlayerScreen(
                        theme           = theme,
                        onOpenSources   = { backStack.add(Dest.Sources) },
                        onOpenCustomize = { backStack.add(Dest.Customize) },
                        onOpenSettings  = { backStack.add(Dest.Settings) }
                    )
                }
            }

            entry<Dest.Sources> {
                SourcesScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<Dest.Customize> {
                CustomizeScreen(
                    currentTheme  = theme,
                    widgetAccent = widgetAccent,
                    widgetSurface = widgetSurface,
                    widgetShowControls = widgetShowControls,
                    widgetShowArtist = widgetShowArtist,
                    onWidgetAppearanceChange = { accent, surface, controls, artist ->
                        widgetAccent = accent
                        widgetSurface = surface
                        widgetShowControls = controls
                        widgetShowArtist = artist
                        widgetPrefs.edit().putLong("accent", accent).putLong("surface", surface)
                            .putBoolean("controls", controls).putBoolean("artist", artist).apply()
                        VinylBaseWidgetProvider.updateAll(activity)
                    },
                    onThemeChange = { newTheme ->
                        theme = newTheme
                        prefs.edit().putLong("background", newTheme.backgroundColor)
                            .putBoolean("dark", newTheme.darkMode).apply()
                        backStack.removeLastOrNull()
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<Dest.Settings> {
                SettingsScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
    }
    }
}
