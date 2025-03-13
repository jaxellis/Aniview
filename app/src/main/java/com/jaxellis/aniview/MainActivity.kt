package com.jaxellis.aniview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.jaxellis.aniview.ui.screens.AnimeScreen
import com.jaxellis.aniview.ui.screens.HomeScreen
import com.jaxellis.aniview.ui.screens.MangaScreen
import com.jaxellis.aniview.ui.screens.ProfileScreen
import com.jaxellis.aniview.ui.screens.SettingsScreen
import com.jaxellis.aniview.ui.theme.AniviewTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            
            AniviewTheme(darkTheme = isDarkTheme) {
                AniviewApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@PreviewScreenSizes
@Composable
fun AniviewApp(
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Track if we're showing the settings screen
    var showSettings by remember { mutableStateOf(false) }
    
    // Create a pager state for swiping between screens
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { AppDestinations.entries.size }
    )
    
    // Derive the current destination from pager state
    val currentDestination by remember {
        derivedStateOf { 
            AppDestinations.entries[pagerState.currentPage] 
        }
    }

    if (showSettings) {
        SettingsScreen(
            onBackClick = { showSettings = false },
            onToggleTheme = onToggleTheme,
            isDarkTheme = isDarkTheme
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon(),
                                contentDescription = it.label
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { 
                            // When clicking a tab, update the pager state
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(it.ordinal)
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true,
                ) { page ->
                    // Display the appropriate screen based on the page
                    when (AppDestinations.entries[page]) {
                        AppDestinations.HOME -> HomeScreen()
                        AppDestinations.Anime -> AnimeScreen()
                        AppDestinations.Manga -> MangaScreen()
                        AppDestinations.PROFILE -> ProfileScreen(
                            onSettingsClick = { showSettings = true }
                        )
                    }
                }
            }
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
    Anime("Anime", { getIconFromDrawable(R.drawable.ic_anime) }),
    Manga("Manga", { getIconFromDrawable(R.drawable.ic_manga) }),
    PROFILE("Profile", { Icons.Default.AccountBox }),
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AniviewTheme {
        HomeScreen()
    }
}