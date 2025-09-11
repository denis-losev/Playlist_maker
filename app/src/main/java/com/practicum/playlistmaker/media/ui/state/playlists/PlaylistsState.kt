package com.practicum.playlistmaker.media.ui.state.playlists

import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.utils.UiMessage

sealed class PlaylistsState {

    object Loading : PlaylistsState()
    data class Content(val playlists: List<Playlist>) : PlaylistsState()
    data class Empty(val message: UiMessage) : PlaylistsState()
}