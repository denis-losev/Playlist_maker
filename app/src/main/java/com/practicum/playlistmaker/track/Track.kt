package com.practicum.playlistmaker.track

import android.icu.text.SimpleDateFormat
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Int,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String
) : Parcelable {

    fun getTrackDuration(): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(this.trackTimeMillis)
    }

    fun getTrackYear(): String {
        return LocalDate.parse(releaseDate, DateTimeFormatter.ISO_DATE_TIME).year.toString()
    }
}