package com.practicum.playlistmaker

import android.content.Context
import com.practicum.playlistmaker.data.Constants.TRACKS_LIST_HISTORY
import com.practicum.playlistmaker.data.network.TracksRepositoryImpl
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.network.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl

object Creator {

    private lateinit var  appContext: Context

    fun init(context: Context) {
        appContext = context
    }

    private fun getTracksRepository(): TracksRepository {
        return  TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    fun provideSearchHistoryRepository() : SearchHistoryRepository {
        val sharedPrefs = appContext.getSharedPreferences(TRACKS_LIST_HISTORY, Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPrefs)
    }
}