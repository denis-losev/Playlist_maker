package com.practicum.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPrefs = getSharedPreferences(PRACTICUM_EXAMPLE_PREFERENCES, MODE_PRIVATE)
        val darkModeState = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        darkTheme = sharedPrefs.getBoolean(NIGHT_MODE_PREFERENCES, darkModeState)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(NIGHT_MODE_PREFERENCES, darkThemeEnabled)
            .apply()
    }

    companion object {
        const val PRACTICUM_EXAMPLE_PREFERENCES = "PRACTICUM_EXAMPLE_PREFERENCES"
        const val NIGHT_MODE_PREFERENCES = "NIGHT_MODE_PREFERENCES"
    }
}