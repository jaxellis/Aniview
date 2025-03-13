package com.jaxellis.aniview

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.jaxellis.aniview.ui.theme.ThemeOption

class AniviewApplication : Application() {
    // Companion object to hold theme states
    companion object {
        const val PREF_NAME = "aniview_prefs"
        const val KEY_HIGH_CONTRAST = "high_contrast_mode"
        const val KEY_THEME = "app_theme"
        
        // Function to get the current high contrast mode state
        fun isHighContrastEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        }
        
        // Function to set high contrast mode
        fun setHighContrastMode(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { 
                putBoolean(KEY_HIGH_CONTRAST, enabled)
                // If enabling high contrast, ensure we're also in dark mode
                if (enabled) {
                    putString(KEY_THEME, ThemeOption.HIGH_CONTRAST.name)
                } else if (getThemeOption(context) == ThemeOption.HIGH_CONTRAST) {
                    // If disabling high contrast and current theme is high contrast,
                    // switch to dark mode
                    putString(KEY_THEME, ThemeOption.DARK.name)
                }
            }
        }
        
        // Function to check if dark theme is forced
        fun isDarkThemeForced(context: Context): Boolean {
            return getThemeOption(context) == ThemeOption.DARK
        }
        
        // Function to check if light theme is forced
        fun isLightThemeForced(context: Context): Boolean {
            return getThemeOption(context) == ThemeOption.LIGHT
        }
        
        // Function to get the current theme option
        fun getThemeOption(context: Context): ThemeOption {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val themeName = prefs.getString(KEY_THEME, ThemeOption.SYSTEM.name)
            return try {
                ThemeOption.valueOf(themeName ?: ThemeOption.SYSTEM.name)
            } catch (e: IllegalArgumentException) {
                // If the stored theme name is invalid, default to system
                ThemeOption.SYSTEM
            }
        }
        
        // Function to set the theme option
        fun setThemeOption(context: Context, themeOption: ThemeOption) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putString(KEY_THEME, themeOption.name)
                // Update high contrast setting based on theme
                putBoolean(KEY_HIGH_CONTRAST, themeOption == ThemeOption.HIGH_CONTRAST)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Set the app to always use dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
} 