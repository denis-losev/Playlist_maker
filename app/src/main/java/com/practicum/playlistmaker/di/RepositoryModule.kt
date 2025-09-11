package com.practicum.playlistmaker.di

import android.app.Activity
import com.practicum.playlistmaker.db.data.converters.PlaylistDbConvertor
import com.practicum.playlistmaker.db.data.converters.PlaylistedTrackDbConvertor
import com.practicum.playlistmaker.db.data.converters.TrackDbConvertor
import com.practicum.playlistmaker.db.data.impl.FavoriteRepositoryImpl
import com.practicum.playlistmaker.db.data.impl.PlaylistRepositoryImpl
import com.practicum.playlistmaker.db.data.impl.PlaylistedTracksRepositoryImpl
import com.practicum.playlistmaker.db.domain.favorites.FavoriteRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistRepository
import com.practicum.playlistmaker.db.domain.playlists.PlaylistedTracksRepository
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
        TracksRepositoryImpl(get(), get())
    }

    factory<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory<ExternalNavigator> { (activity: Activity) ->
        ExternalNavigatorImpl(activity)
    }

    factory { TrackDbConvertor() }

    factory { PlaylistedTrackDbConvertor() }

    factory { PlaylistDbConvertor() }

    single<FavoriteRepository> {
        FavoriteRepositoryImpl(get(), get())
    }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get())
    }

    single<PlaylistedTracksRepository> {
        PlaylistedTracksRepositoryImpl(get(), get())
    }
}