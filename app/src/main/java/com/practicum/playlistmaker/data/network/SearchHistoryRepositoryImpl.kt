package com.practicum.playlistmaker.data.network

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.Constants.TRACKS_LIST_HISTORY
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.models.Track

class SearchHistoryRepositoryImpl(private val sharedPrefs: SharedPreferences) : SearchHistoryRepository{

    private val gson = Gson()
    private var tracksListHistory: ArrayList<Track> = loadHistory()

    private fun loadHistory(): ArrayList<Track> {
        val json = sharedPrefs.getString(TRACKS_LIST_HISTORY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<Track>>() {}.type)
        } else {
            arrayListOf()
        }
    }
    override fun addTrackHistory(track: Track) {
        tracksListHistory.remove(track)
        tracksListHistory.add(0, track)

        if (tracksListHistory.size > MAX_TRACKS_COUNT) {
            tracksListHistory =
                tracksListHistory.take(MAX_TRACKS_COUNT).toList() as ArrayList<Track>
        }

        sharedPrefs.edit()
            .putString(TRACKS_LIST_HISTORY, gson.toJson(tracksListHistory))
            .apply()
    }

    override fun getSearchHistory(): ArrayList<Track> {
        return tracksListHistory
    }

    override fun clearHistory() {
        sharedPrefs.edit()
            .remove(TRACKS_LIST_HISTORY)
            .apply()
    }

    companion object {
        const val MAX_TRACKS_COUNT = 10
    }
}