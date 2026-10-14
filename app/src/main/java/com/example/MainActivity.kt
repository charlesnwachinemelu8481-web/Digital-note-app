package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NotesViewModel
import com.example.ui.Screen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.CharcoalSubtle
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.QuickNoteTheme
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.WarmOffWhite

class MainActivity : ComponentActivity() {
    private val viewModel: NotesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickNoteTheme {
                QuickNoteApp(viewModel = viewModel)
            }
        }
    }
}

sealed class BottomNavItem(
    val screen: Screen,
    val title: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    data object Home : BottomNavItem(Screen.HOME, "Notes", Icons.Filled.Home, Icons.Outlined.Home)
    data object Search : BottomNavItem(Screen.SEARCH, "Search", Icons.Filled.Search, Icons.Outlined.Search)
    data object Folders : BottomNavItem(Screen.FOLDERS, "Folders", Icons.Filled.Folder, Icons.Outlined.Folder)
    data object Trash : BottomNavItem(Screen.TRASH, "Trash", Icons.Filled.Delete, Icons.Outlined.Delete)
    data object Settings : BottomNavItem(Screen.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun QuickNoteApp(viewModel: NotesViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Handle back button on sub-screens
    BackHandler(enabled = currentScreen != Screen.HOME) {
        viewModel.navigateBack()
    }

    if (currentScreen == Screen.EDITOR) {
        NoteEditorScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                QuickNoteBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            },
            containerColor = WarmOffWhite,
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(viewModel = viewModel)
                        Screen.SEARCH -> SearchScreen(viewModel = viewModel)
                        Screen.FOLDERS -> FoldersScreen(viewModel = viewModel)
                        Screen.TRASH -> TrashScreen(viewModel = viewModel)
                        Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        Screen.EDITOR -> NoteEditorScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNoteBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Folders,
        BottomNavItem.Trash,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = WarmOffWhite,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = SlateBorder)
            .testTag("bottom_nav_bar")
    ) {
        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = if (isSelected) CharcoalText else CharcoalSubtle
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CharcoalText,
                    selectedTextColor = CharcoalText,
                    indicatorColor = AmberAccent,
                    unselectedIconColor = CharcoalSubtle,
                    unselectedTextColor = CharcoalSubtle
                ),
                modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
            )
        }
    }
}
