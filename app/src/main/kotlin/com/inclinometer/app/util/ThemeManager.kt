package com.inclinometer.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean("is_dark_mode", true)
        set(value) = prefs.edit().putBoolean("is_dark_mode", value).apply()

    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
        applyTheme()
    }
}
