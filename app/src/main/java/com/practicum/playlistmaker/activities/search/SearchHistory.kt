package com.practicum.playlistmaker.activities.search

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.track.Track

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()

    private var tracksListHistory: ArrayList<Track> = loadHistory()

    private fun loadHistory(): ArrayList<Track> {
        val json = sharedPreferences.getString(TRACKS_LIST_HISTORY, null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<MutableList<Track>>() {}.type)
        } else {
            arrayListOf()
        }
    }

    fun addTrackToHistory(track: Track) {
        tracksListHistory.remove(track)
        tracksListHistory.add(0, track)

        if (tracksListHistory.size > MAX_TRACKS_COUNT) {
            tracksListHistory =
                tracksListHistory.take(MAX_TRACKS_COUNT).toList() as ArrayList<Track>
        }

        sharedPreferences.edit()
            .putString(TRACKS_LIST_HISTORY, gson.toJson(tracksListHistory))
            .apply()
    }

    fun getTracksHistory(): ArrayList<Track> {
        return tracksListHistory
    }

    fun clearHistory() {
        sharedPreferences.edit()
            .remove(TRACKS_LIST_HISTORY)
            .apply()
    }

    private companion object {
        const val TRACKS_LIST_HISTORY = "TRACKS_LIST_HISTORY"
        const val MAX_TRACKS_COUNT = 10
    }
}