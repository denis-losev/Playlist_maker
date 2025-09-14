package com.practicum.playlistmaker.media.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: Int = 0,
    val name: String,
    val description: String,
    val image: String,
    val tracksCount: Int = 0,
    val tracksIds: List<Int> = emptyList()
) : Parcelable