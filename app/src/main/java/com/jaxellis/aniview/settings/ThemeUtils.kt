package com.jaxellis.aniview.settings

import android.content.Context
import com.jaxellis.aniview.AniviewApplication

/**
 * Utility class for theme operations.
 */
object ThemeUtils {
    /**
     * Toggle high contrast mode on or off.
     *
     * @param context The context.
     * @return The new state of high contrast mode.
     */
    fun toggleHighContrastMode(context: Context): Boolean {
        val currentState = AniviewApplication.isHighContrastEnabled(context)
        val newState = !currentState
        AniviewApplication.setHighContrastMode(context, newState)
        return newState
    }
    
    /**
     * Enable high contrast mode.
     *
     * @param context The context.
     */
    fun enableHighContrastMode(context: Context) {
        AniviewApplication.setHighContrastMode(context, true)
    }
    
    /**
     * Disable high contrast mode.
     *
     * @param context The context.
     */
    fun disableHighContrastMode(context: Context) {
        AniviewApplication.setHighContrastMode(context, false)
    }
} 