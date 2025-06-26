package com.practicum.playlistmaker.media.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.utils.BindingFragment

class PlaylistsFragment : BindingFragment<FragmentPlaylistsBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistsBinding {
        return FragmentPlaylistsBinding.inflate(inflater, container, false)
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}