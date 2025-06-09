package com.practicum.playlistmaker.settings.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.sharing.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<ThemeSettings>()
    fun getState(): LiveData<ThemeSettings> = _state

    init {
        _state.value = ThemeSettings(settingsInteractor.getThemeSettings().isDarkMode)
    }

    fun switchTheme(isDarkThemeEnabled: Boolean) {
        settingsInteractor.updateThemeSettings(ThemeSettings(isDarkThemeEnabled))
        _state.value = ThemeSettings(isDarkThemeEnabled)
    }

    fun shareApp() {
        sharingInteractor.shareApp()
    }

    fun openTerms() {
        sharingInteractor.openTerms()
    }

    fun contactSupport() {
        sharingInteractor.openSupport()
    }
}