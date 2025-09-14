package com.practicum.playlistmaker.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.adapters.PlaylistsAdapter
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsUiState
import com.practicum.playlistmaker.media.ui.state.playlists.toUiState
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistsViewModel
import com.practicum.playlistmaker.utils.BindingFragment
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : BindingFragment<FragmentPlaylistsBinding>() {

    private val viewModel: PlaylistsViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    private val adapter = PlaylistsAdapter { tapOnPlaylist(it) }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistsBinding {
        return FragmentPlaylistsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)
        setupUI()
        setupObservers()
        createPlaylistButtonClick()
        viewModel.fillData()
    }

    private fun setupUI() {
        binding.playlistsList.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            render(state.toUiState(requireContext()))
        }
    }

    private fun render(state: PlaylistsUiState) = with(binding) {
        hideAll()

        when (state) {
            is PlaylistsUiState.Content -> showContent(state.playlists)
            is PlaylistsUiState.Empty -> showEmpty(state)
            PlaylistsUiState.Loading -> progressBar.isVisible = true
        }
    }

    private fun showContent(playlists: List<Playlist>) = with(binding) {
        adapter.playlists = ArrayList(playlists)
        adapter.notifyDataSetChanged()
        playlistsList.isVisible = true
    }

    private fun showEmpty(state: PlaylistsUiState.Empty) = with(binding) {
        playlistsErrorContainer.isVisible = true
        playlistsErrorEmoji.setImageResource(state.emojiRes)
        playlistsErrorMessage.text = state.message
    }

    private fun hideAll() {
        with(binding) {
            progressBar.isVisible = false
            playlistsList.isVisible = false
            playlistsErrorContainer.isVisible = false
        }
    }

    private fun tapOnPlaylist(playlist: Playlist) {

    }

    private fun createPlaylistButtonClick() {
        clickDebouncer.tryClick {
            binding.newPlaylistButton.setOnClickListener {
                requireActivity().findNavController(R.id.fragment_main_container)
                    .navigate(R.id.fragment_create_playlist)
            }
        }
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}