package com.practicum.playlistmaker.media.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.media.ui.FavoritesUiState
import com.practicum.playlistmaker.media.ui.toUiState
import com.practicum.playlistmaker.media.ui.view_model.FavoritesViewModel
import com.practicum.playlistmaker.player.ui.activity.PlayerActivity
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.search.ui.TrackAdapter
import com.practicum.playlistmaker.utils.BindingFragment
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : BindingFragment<FragmentFavoritesBinding>() {

    private val viewModel: FavoritesViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    private val adapter = TrackAdapter { tapOnTrack(it) }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)
        setupUI()
        setupObservers()
        viewModel.fillData()
    }

    private fun setupUI() {
        binding.favoriteTracksList.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            render(state.toUiState(requireContext()))
        }
    }

    private fun render(state: FavoritesUiState) = with(binding) {
        hideAll()

        when (state) {
            is FavoritesUiState.Content -> showContent(state.tracks)
            is FavoritesUiState.Empty -> showEmpty(state)
            FavoritesUiState.Loading -> progressBar.isVisible = true
        }
    }

    private fun showContent(tracks: List<Track>) = with(binding) {
        adapter.tracks = ArrayList(tracks)
        adapter.notifyDataSetChanged()
        favoriteTracksList.isVisible = true
    }

    private fun showEmpty(state: FavoritesUiState.Empty) = with(binding) {
        errorContainer.isVisible = true
        favoritesErrorEmoji.setImageResource(state.emojiRes)
        favoritesErrorMessage.text = state.message
    }

    private fun hideAll() {
        with(binding) {
            progressBar.isVisible = false
            favoriteTracksList.isVisible = false
            errorContainer.isVisible = false
        }
    }

    private fun tapOnTrack(track: Track) {
        clickDebouncer.tryClick {
            viewModel.addTrackToHistory(track)
            val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(TRACK, Gson().toJson(track))
            }
            startActivity(intent)
        }
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}