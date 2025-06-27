package com.practicum.playlistmaker.media.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.utils.BindingFragment

class FavoritesFragment : BindingFragment<FragmentFavoritesBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(inflater, container, false)
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}