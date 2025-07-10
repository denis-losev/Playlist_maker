package com.practicum.playlistmaker.di

import android.app.Activity
import com.practicum.playlistmaker.media.ui.view_model.FavoritesViewModel
import com.practicum.playlistmaker.media.ui.view_model.MediaViewModel
import com.practicum.playlistmaker.media.ui.view_model.PlaylistsViewModel
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import com.practicum.playlistmaker.search.ui.view_model.SearchViewModel
import com.practicum.playlistmaker.settings.ui.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        PlayerViewModel(get())
    }

    viewModel { (activity: Activity) ->
        SettingsViewModel(
            sharingInteractor = get{parametersOf(activity)},
            settingsInteractor = get()
        )
    }

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        MediaViewModel()
    }

    viewModel {
        FavoritesViewModel()
    }

    viewModel {
        PlaylistsViewModel()
    }
}