package com.practicum.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.room.Room
import com.google.gson.Gson
import com.practicum.playlistmaker.Constants.NIGHT_MODE_PREFERENCES
import com.practicum.playlistmaker.Constants.SHARED_PREFS
import com.practicum.playlistmaker.Constants.TRACKS_LIST_HISTORY
import com.practicum.playlistmaker.db.data.AppDatabase
import com.practicum.playlistmaker.db.data.migrations.MIGRATION_1_2
import com.practicum.playlistmaker.search.data.network.ITunesApiService
import com.practicum.playlistmaker.search.data.network.NetworkClient
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.search.data.network.RetrofitNetworkClient.Companion.BASE_URL
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single<ITunesApiService> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApiService::class.java)
    }

    single<NetworkClient> {
        RetrofitNetworkClient(get(), androidContext())
    }

    factory {
        MediaPlayer()
    }

    single {
        Gson()
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
    }

    single(named(NIGHT_MODE_PREFERENCES)) {
        androidContext()
            .getSharedPreferences(NIGHT_MODE_PREFERENCES, Context.MODE_PRIVATE)
    }

    single(named(TRACKS_LIST_HISTORY)) {
        androidContext()
            .getSharedPreferences(TRACKS_LIST_HISTORY, Context.MODE_PRIVATE)
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}