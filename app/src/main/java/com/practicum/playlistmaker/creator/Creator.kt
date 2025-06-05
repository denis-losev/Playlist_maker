package com.practicum.playlistmaker.creator

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import com.practicum.playlistmaker.Constants.NIGHT_MODE_PREFERENCES
import com.practicum.playlistmaker.Constants.TRACKS_LIST_HISTORY
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.impl.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.impl.TracksRepositoryImpl
import com.practicum.playlistmaker.search.data.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.TracksInteractor
import com.practicum.playlistmaker.search.data.TracksRepository
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.player.data.impl.PlayerRepositoryImpl
import com.practicum.playlistmaker.player.domain.PlayerInteractor
import com.practicum.playlistmaker.player.domain.impl.PlayerInteractorImpl
import com.practicum.playlistmaker.settings.data.impl.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.sharing.SharingInteractor
import com.practicum.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.practicum.playlistmaker.sharing.domain.impl.SharingInteractorImpl

object Creator {

    private lateinit var  appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getTracksRepository(): TracksRepository {
        return  TracksRepositoryImpl(RetrofitNetworkClient(appContext))
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryRepository() : SearchHistoryRepository {
        val sharedPrefs = appContext.getSharedPreferences(TRACKS_LIST_HISTORY, Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPrefs)
    }

    fun provideSettingsInteractor(): SettingsInteractor {
        val sharedPreferences = appContext.getSharedPreferences(NIGHT_MODE_PREFERENCES, Context.MODE_PRIVATE)
        val settingsRepository = SettingsRepositoryImpl(sharedPreferences)
        return SettingsInteractorImpl(settingsRepository)
    }

    fun provideSharingInteractor(activity: Activity): SharingInteractor {
        val externalNavigator = ExternalNavigatorImpl(activity)
        return SharingInteractorImpl(externalNavigator, appContext)
    }

    fun providePlayerInteractor(): PlayerInteractor {
        val mediaPlayer = MediaPlayer()
        val repository = PlayerRepositoryImpl(mediaPlayer)
        return PlayerInteractorImpl(repository)
    }
}