package com.jaxellis.aniview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.jaxellis.aniview.ui.screens.AnimeScreen
import com.jaxellis.aniview.ui.screens.HomeScreen
import com.jaxellis.aniview.ui.screens.MangaScreen
import com.jaxellis.aniview.ui.screens.ProfileScreen
import com.jaxellis.aniview.ui.screens.SettingsScreen
import com.jaxellis.aniview.ui.theme.AniviewTheme
import com.jaxellis.aniview.ui.theme.ThemeOption
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var currentTheme by rememberSaveable { 
                mutableStateOf(AniviewApplication.getThemeOption(context)) 
            }
            AniviewTheme(
                themeOption = currentTheme,
                darkTheme = isSystemInDarkTheme()
            ) {
                AniviewApp(
                    currentTheme = currentTheme,
                    onThemeChanged = { newTheme ->
                        currentTheme = newTheme
                        AniviewApplication.setThemeOption(context, newTheme)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@PreviewScreenSizes
@Composable
fun AniviewApp(
    currentTheme: ThemeOption = ThemeOption.DARK,
    onThemeChanged: (ThemeOption) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Track if we're showing the settings screen
    var showSettings by remember { mutableStateOf(false) }
    
    // For tab selection
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    // Create pager state for swipe gestures
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { AppDestinations.entries.size }
    )
    
    // Track if we're navigating via click vs swipe
    var isTabClick by remember { mutableStateOf(false) }
    
    // Keep pager and selectedTab in sync when swiping
    LaunchedEffect(pagerState.currentPage) {
        if (!isTabClick) {
            selectedTabIndex = pagerState.currentPage
        }
    }
    
    if (showSettings) {
        SettingsScreen(
            onBackClick = { showSettings = false },
            currentTheme = currentTheme,
            onThemeChanged = onThemeChanged
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            NavigationSuiteScaffold(
                modifier = Modifier.padding(paddingValues),
                navigationSuiteItems = {
                    AppDestinations.entries.forEachIndexed { index, destination ->
                        item(
                            icon = {
                                Icon(
                                    destination.icon(),
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) },
                            selected = selectedTabIndex == index,
                            onClick = { 
                                if (selectedTabIndex != index) {
                                    isTabClick = true
                                    selectedTabIndex = index
                                    
                                    coroutineScope.launch {
                                        // We'll use animateScrollToPage, but we won't see intermediate screens
                                        // because we're controlling what content gets displayed
                                        pagerState.animateScrollToPage(
                                            page = index,
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        isTabClick = false
                                    }
                                }
                            }
                        )
                    }
                },
                content = {
                    // Horizontal pager for swiping between tabs
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true // Enable swipe gesture
                    ) { page ->
                        // Only render the page if it's the currently selected tab or it's being swiped to
                        if (page == selectedTabIndex || !isTabClick) {
                            when (AppDestinations.entries[page]) {
                                AppDestinations.HOME -> HomeScreen()
                                AppDestinations.ANIME -> AnimeScreen()
                                AppDestinations.MANGA -> MangaScreen() 
                                AppDestinations.PROFILE -> ProfileScreen(
                                    onSettingsClick = { showSettings = true }
                                )
                            }
                        } else {
                            // Empty placeholder to maintain pager dimensions
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun getIconFromDrawable(drawableId: Int): ImageVector {
    return ImageVector.vectorResource(id = drawableId)
}
enum class AppDestinations(
    val label: String,
    val icon: @Composable () -> ImageVector,
) {
    HOME("Home", { Icons.Default.Home }),
    ANIME("Anime", { getIconFromDrawable(R.drawable.ic_anime) }),
    MANGA("Manga", { getIconFromDrawable(R.drawable.ic_manga) }),
    PROFILE("Profile", { Icons.Default.AccountBox }),
}