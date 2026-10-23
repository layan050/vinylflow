package com.example.vinylflow

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.vinylflow.domain.model.PRESET_MIDNIGHT_BLACK
import com.example.vinylflow.domain.model.VinylTheme
import com.example.vinylflow.ui.customize.CustomizeScreen
import com.example.vinylflow.ui.player.PlayerScreen
import com.example.vinylflow.ui.player.TabletPlayerScreen
import com.example.vinylflow.ui.settings.SettingsScreen
import com.example.vinylflow.ui.sources.SourcesScreen
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
    var theme by remember { mutableStateOf(PRESET_MIDNIGHT_BLACK) }

    // Auto-detect tablet (Xiaomi Pad 7 → Expanded width in landscape)
    val windowSizeClass = calculateWindowSizeClass(activity)
    val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

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
                    onThemeChange = { newTheme ->
                        theme = newTheme
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
