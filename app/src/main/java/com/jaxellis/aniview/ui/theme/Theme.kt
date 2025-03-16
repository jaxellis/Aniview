package com.jaxellis.aniview.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Represents the available theme options in the app.
 * 
 * This enum is designed to be extensible for future theme additions.
 */
enum class ThemeOption(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    DYNAMIC("Material You"),
    HIGH_CONTRAST("High Contrast");
}

//!TODO Match below themes to anilist themes, Material You is the best so far.

// Regular Dark Color Scheme
//!TODO try to match anilist 
private val DarkColorScheme = darkColorScheme(
    primary = DarkBlue,
    secondary = DarkBlueDim,
    tertiary = DarkPeach,
    background = DarkBackground,
    surface = DarkForeground,
    surfaceVariant = DarkForegroundGrey,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = DarkTextLight
)

// High Contrast Color Scheme
//!TODO try to match anilist 
private val HighContrastColorScheme = lightColorScheme(
    primary = ContrastBlue,
    secondary = ContrastBlueDim,
    tertiary = ContrastBlue,
    background = ContrastBackground,
    surface = ContrastForeground,
    surfaceVariant = ContrastForegroundGrey,
    surfaceTint = ContrastForegroundBlueDark,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = ContrastText,
    onSurface = ContrastText,
    onSurfaceVariant = ContrastTextLight,
    outline = ContrastTextLighter,
    outlineVariant = ContrastShadow
)

// Light Color Scheme
//!TODO try to match anilist 
private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    secondary = LightBlueDim,
    tertiary = LightForeground,
    background = LightBackground,
    surface = LightForeground,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = LightText,
    onSurface = LightText
)

// Typography with larger text for high contrast
private val HighContrastTypography = Typography.copy(
    bodyLarge = Typography.bodyLarge.copy(
        fontSize = Typography.bodyLarge.fontSize * 1.2f,
        fontWeight = FontWeight.SemiBold
    ),
    bodyMedium = Typography.bodyMedium.copy(
        fontSize = Typography.bodyMedium.fontSize * 1.2f,
        fontWeight = FontWeight.SemiBold
    ),
    bodySmall = Typography.bodySmall.copy(
        fontSize = Typography.bodySmall.fontSize * 1.2f,
        fontWeight = FontWeight.SemiBold
    )
)

@Composable
fun AniviewTheme(
    themeOption: ThemeOption,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Determine final dark mode state based on theme option
    val useDarkTheme = when(themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.HIGH_CONTRAST -> false 
        ThemeOption.DYNAMIC -> darkTheme 
        ThemeOption.SYSTEM -> darkTheme  
    }
    
    // Select the color scheme based on theme option and dark mode
    val colorScheme = when (themeOption) {
        // Material You dynamic colors
        ThemeOption.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (useDarkTheme) DarkColorScheme else LightColorScheme
            }
        }
        ThemeOption.HIGH_CONTRAST -> HighContrastColorScheme
        ThemeOption.LIGHT -> LightColorScheme
        ThemeOption.DARK -> DarkColorScheme
        // System follows dark/light setting
        ThemeOption.SYSTEM -> {
            if (useDarkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    val typography = if (themeOption == ThemeOption.HIGH_CONTRAST) 
        HighContrastTypography 
    else 
        Typography

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Enable edge-to-edge content
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Get window insets controller to manage system bars appearance
            val controller = WindowInsetsControllerCompat(window, view)
            
            // Configure system bars appearance and behavior
            controller.apply {
                // Set the appearance of the status bar icons based on theme
                val useLight = when(themeOption) {
                    ThemeOption.LIGHT -> true
                    ThemeOption.HIGH_CONTRAST -> true
                    ThemeOption.DARK -> false
                    ThemeOption.DYNAMIC -> !useDarkTheme
                    ThemeOption.SYSTEM -> !useDarkTheme
                }
                isAppearanceLightStatusBars = useLight
                isAppearanceLightNavigationBars = useLight
                
                // Make system bars visible
                show(WindowInsetsCompat.Type.systemBars())
                
                // Set behavior for system bars
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}