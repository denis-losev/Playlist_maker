package com.practicum.playlistmaker.di

import android.app.Activity
import com.practicum.playlistmaker.db.domain.favorites.FavoriteInteractor
import com.practicum.playlistmaker.db.domain.favorites.impl.FavoriteInteractorImpl
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.db.domain.playlists.impl.PlaylistInteractorImpl
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.impl.PlayerInteractorImpl
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.sharing.SharingInteractor
import com.practicum.playlistmaker.sharing.domain.ExternalNavigator
import com.practicum.playlistmaker.sharing.domain.impl.SharingInteractorImpl
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val interactorModule = module {

    factory<PlayerInteractor> {
        PlayerInteractorImpl(get())
    }

    single<TracksInteractor> {
        TracksInteractorImpl(get())
    }

    factory<SharingInteractor> { (activity: Activity) ->
        val navigator: ExternalNavigator = get { parametersOf(activity) }
        SharingInteractorImpl(navigator)
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    single<FavoriteInteractor> {
        FavoriteInteractorImpl(get())
    }

    single<PlaylistsInteractor> {
        PlaylistInteractorImpl(get(), get())
    }
}