package com.practicum.playlistmaker.settings.ui.view_model

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.sharing.SharingInteractor
import com.practicum.playlistmaker.sharing.domain.model.EmailData

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<ThemeSettings>()
    fun getState(): LiveData<ThemeSettings> = _state

    init {
        val currentSettings = settingsInteractor.getThemeSettings()
        _state.value = currentSettings
        applyTheme(currentSettings.isDarkMode)
    }

    fun switchTheme(isDarkThemeEnabled: Boolean) {
        settingsInteractor.updateThemeSettings(ThemeSettings(isDarkThemeEnabled))
        applyTheme(isDarkThemeEnabled)
        _state.value = ThemeSettings(isDarkThemeEnabled)
    }

    fun shareApp(link: String) {
        sharingInteractor.shareApp(link)
    }

    fun openTerms(link: String) {
        sharingInteractor.openTerms(link)
    }

    fun contactSupport(data: EmailData) {
        sharingInteractor.openSupport(data)
    }

    private fun applyTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
}