package com.practicum.playlistmaker.iTunesApi

import com.practicum.playlistmaker.track.Track

class TrackResponse(
    val resultCount: Int,
    val results: List<Track>
)