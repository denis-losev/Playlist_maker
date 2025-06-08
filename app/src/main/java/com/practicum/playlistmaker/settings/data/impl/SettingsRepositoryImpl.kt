package com.practicum.playlistmaker.settings.data.impl

import android.content.SharedPreferences
import com.practicum.playlistmaker.Constants.NIGHT_MODE_PREFERENCES
import com.practicum.playlistmaker.settings.domain.SettingsRepository
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getThemeSettings(): ThemeSettings {
        val isDarkMode = sharedPreferences.getBoolean(NIGHT_MODE_PREFERENCES, false)
        return ThemeSettings(isDarkMode)
    }

    override fun updateThemeSettings(settings: ThemeSettings) {
        sharedPreferences.edit()
            .putBoolean(NIGHT_MODE_PREFERENCES, settings.isDarkMode)
            .apply()
    }
}