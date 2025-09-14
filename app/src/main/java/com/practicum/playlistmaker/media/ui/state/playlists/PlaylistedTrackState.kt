package com.practicum.playlistmaker.media.ui.state.playlists

sealed class PlaylistedTrackState {
    data class AlreadyExists(val playlistName: String) : PlaylistedTrackState()
    data class Success(val playlistName: String) : PlaylistedTrackState()
    data class Error(val message: String) : PlaylistedTrackState()
}