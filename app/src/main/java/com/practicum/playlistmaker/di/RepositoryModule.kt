package com.practicum.playlistmaker.di

import android.app.Activity
import com.practicum.playlistmaker.player.domain.PlayerRepository
import com.practicum.playlistmaker.player.data.impl.PlayerRepositoryImpl
import com.practicum.playlistmaker.search.domain.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.TracksRepository
import com.practicum.playlistmaker.search.data.impl.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.impl.TracksRepositoryImpl
import com.practicum.playlistmaker.settings.domain.SettingsRepository
import com.practicum.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.practicum.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.practicum.playlistmaker.sharing.domain.ExternalNavigator
import org.koin.dsl.module

val repositoryModule = module {

    factory<PlayerRepository> {
        PlayerRepositoryImpl(get())
    }

    factory<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get())
    }

    factory<TracksRepository> {
        TracksRepositoryImpl(get())
    }

    factory<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<ExternalNavigator> { (activity: Activity) ->
        ExternalNavigatorImpl(activity)
    }
}