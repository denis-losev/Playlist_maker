package com.practicum.playlistmaker.media.ui.state.playlists

import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.utils.UiMessage

sealed class PlaylistState {

    object Loading : PlaylistState()
    data class Content(val tracks: List<Track>) : PlaylistState()
    data class Empty(val message: UiMessage) : PlaylistState()
}