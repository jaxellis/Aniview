package com.jaxellis.aniview

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class AniviewApplication : Application() {
    // Companion object to hold theme states
    companion object {
        const val PREF_NAME = "aniview_prefs"
        const val KEY_HIGH_CONTRAST = "high_contrast_mode"
        
        // Function to get the current high contrast mode state
        fun isHighContrastEnabled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        }
        
        // Function to set high contrast mode
        fun setHighContrastMode(context: Context, enabled: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { putBoolean(KEY_HIGH_CONTRAST, enabled) }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Set the app to always use dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
} 