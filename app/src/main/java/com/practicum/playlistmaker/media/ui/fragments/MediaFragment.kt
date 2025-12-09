package com.practicum.playlistmaker.media.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.practicum.playlistmaker.Constants.PLAYLIST
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.ui.composable.screens.MediaScreen
import com.practicum.playlistmaker.media.ui.view_model.favorites.FavoritesViewModel
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistsViewModel
import com.practicum.playlistmaker.utils.PlaylistMakerTheme
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PlaylistMakerTheme {
                    MediaScreen(
                        onTrackClick = { track ->
                            clickDebouncer.tryClick {
                                favoritesViewModel.addTrackToHistory(track)
                                val bundle = Bundle().apply { putParcelable(TRACK, track) }
                                findNavController().navigate(
                                    R.id.playerFragment,
                                    bundle
                                )
                            }
                        },
                        onCreatePlaylistClick = {
                            clickDebouncer.tryClick {
                                requireActivity().findNavController(R.id.fragment_main_container)
                                    .navigate(R.id.fragment_create_playlist)
                            }
                        },
                        onPlaylistClick = { playlist ->
                            clickDebouncer.tryClick {
                                val bundle = Bundle().apply { putParcelable(PLAYLIST, playlist) }
                                findNavController().navigate(
                                    R.id.playlistFragment,
                                    bundle
                                )
                            }
                        },
                        favoritesViewModel = favoritesViewModel,
                        playlistsViewModel = playlistsViewModel
                    )
                }
            }
        }
    }
}