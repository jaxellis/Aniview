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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jaxellis.aniview.AniviewApplication

/**
 * Represents the available theme options in the app.
 * 
 * This enum is designed to be extensible for future theme additions.
 */
enum class ThemeOption(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    HIGH_CONTRAST("High Contrast");
    
    companion object {
        /**
         * Get the current theme option based on app preferences
         */
        fun getCurrentTheme(context: android.content.Context): ThemeOption {
            val isHighContrastEnabled = AniviewApplication.isHighContrastEnabled(context)
            val isDarkThemeForced = AniviewApplication.isDarkThemeForced(context)
            val isLightThemeForced = AniviewApplication.isLightThemeForced(context)
            
            return when {
                isHighContrastEnabled -> HIGH_CONTRAST
                isDarkThemeForced -> DARK
                isLightThemeForced -> LIGHT
                else -> SYSTEM
            }
        }
    }
}

// Regular Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = Blue,
    secondary = BlueDim,
    tertiary = Peach,
    background = Background,
    surface = Foreground,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Text,
    onSurface = Text
)

// High Contrast Dark Color Scheme
private val HighContrastDarkColorScheme = darkColorScheme(
    primary = Color(0xFF12ACFD), // Brighter blue for better contrast
    secondary = Color(0xFF5590D0), // More visible blue dim
    tertiary = Color(0xFF00FFFF), // Bright cyan for tertiary elements
    background = Color(0xFF000000), // Pure black for background
    surface = Color(0xFF0C0C0C), // Near black for surfaces
    onPrimary = Color(0xFFFFFFFF), // Pure white for text on primary
    onSecondary = Color(0xFFFFFFFF), // Pure white for text on secondary
    onTertiary = Color(0xFFFFFFFF), // Pure white for text on tertiary
    onBackground = Color(0xFFFFFFFF), // Pure white for text on background
    onSurface = Color(0xFFFFFFFF) // Pure white for text on surface
)

// Legacy Light Color Scheme - no longer used as default
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    onSurfaceVariant = Gray90
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
    themeOption: ThemeOption? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Determine if high contrast is enabled
    val isHighContrastEnabled = themeOption == ThemeOption.HIGH_CONTRAST || 
        (themeOption == null && AniviewApplication.isHighContrastEnabled(context))
    
    // Determine if dark theme should be used
    val useDarkTheme = when(themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.HIGH_CONTRAST -> true // High contrast is based on dark theme
        ThemeOption.SYSTEM, null -> darkTheme
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isHighContrastEnabled -> HighContrastDarkColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Use different typography for high contrast mode
    val typography = if (isHighContrastEnabled) HighContrastTypography else Typography

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
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
                
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