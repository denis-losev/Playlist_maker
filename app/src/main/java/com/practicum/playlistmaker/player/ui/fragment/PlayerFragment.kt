package com.practicum.playlistmaker.player.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.Constants.ZERO_TIMER
import com.practicum.playlistmaker.databinding.FragmentPlayerBinding
import com.practicum.playlistmaker.db.domain.playlists.PlaylistsInteractor
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistedTrackState
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsState
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.ui.adapters.PlaylistsInPlayerAdapter
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import com.practicum.playlistmaker.utils.BindingFragment
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : BindingFragment<FragmentPlayerBinding>() {

    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    private lateinit var playlistsAdapter: PlaylistsInPlayerAdapter

    private lateinit var track: Track
    private val playlistsInteractor: PlaylistsInteractor by inject()
    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlayerBinding {
        return FragmentPlayerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)

        track = arguments?.getParcelable(TRACK)
            ?: throw IllegalArgumentException("track is required")


        playlistsAdapter = PlaylistsInPlayerAdapter { playlist ->
            tapOnPlaylist(playlist)
        }

        viewModel.preparePlayer(track)

        createPlaylistButtonClick()
        setupUI()
        setupObservers()
        createBottomSheet()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.playlistsRecyclerView.adapter = playlistsAdapter

        with(binding) {
            trackName.text = track.trackName
            artistName.text = track.artistName
            trackDurationValue.text = track.getTrackDuration()
            albumValue.text = track.collectionName
            yearValue.text = track.getTrackYear()
            genreValue.text = track.primaryGenreName
            countryValue.text = track.country

            Glide.with(coverImage)
                .load(track.getCoverArtwork())
                .centerInside()
                .transform(RoundedCorners(8))
                .placeholder(R.drawable.cover_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(coverImage)

            playBtn.setOnClickListener {
                viewModel.togglePlayback()
            }

            favouritesBtn.setOnClickListener {
                viewModel.toggleFavoriteFlag()
                updateFavoriteButton(track.isFavorite)
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favouritesBtn.setImageResource(R.drawable.added_fav_btn)
        } else {
            binding.favouritesBtn.setImageResource(R.drawable.add_to_fav_btn)
        }
    }

    private fun setupObservers() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlayerState.Default -> {
                    binding.playBtn.isEnabled = false
                    binding.playBtn.setImageResource(R.drawable.play_btn)
                    binding.currentPlayPosition.text = ZERO_TIMER
                }

                is PlayerState.Prepared -> {
                    binding.playBtn.isEnabled = true
                    binding.playBtn.setImageResource(R.drawable.play_btn)
                    binding.currentPlayPosition.text = ZERO_TIMER
                    updateFavoriteButton(state.track.isFavorite)
                }

                is PlayerState.Playing -> {
                    binding.playBtn.setImageResource(R.drawable.pause_btn)
                    binding.currentPlayPosition.text = formatTime(state.currentPosition)
                }

                is PlayerState.Paused -> {
                    binding.playBtn.setImageResource(R.drawable.play_btn)
                    binding.currentPlayPosition.text = formatTime(state.currentPosition)
                }

                is PlayerState.Completed -> {
                    binding.playBtn.setImageResource(R.drawable.play_btn)
                    binding.currentPlayPosition.text = ZERO_TIMER
                }
            }
        }

        viewModel.getPlaylistsState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistsState.Loading -> {
                    // Показать индикатор загрузки
                }
                is PlaylistsState.Content -> {
                    showPlaylists(state.playlists)
                }
                is PlaylistsState.Empty -> {
                    // Показать пустое состояние
                    playlistsAdapter.playlists.clear()
                    playlistsAdapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.getPlaylistedTrackState().observe(viewLifecycleOwner) { state ->
            when(state) {
                is PlaylistedTrackState.AlreadyExists -> showMessage("Трек уже есть в ${state.playlistName}")
                is PlaylistedTrackState.Error -> showMessage("Ошибка ${state.message}")
                is PlaylistedTrackState.Success -> {
                    showMessage("Трек добавлен в ${state.playlistName}")
                    closeBottomSheet()
                }
            }
        }
    }

    private fun createBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet
        val overlay = binding.overlay
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.addToPlaylistBtn.setOnClickListener {
            bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_COLLAPSED
        }

        overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> overlay.visibility = View.GONE
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        overlay.visibility = View.VISIBLE
                        viewModel.getPlaylists()
                    }
                    else -> {
                        overlay.visibility = View.VISIBLE
                        viewModel.getPlaylists()
                    }
                }
            }

            override fun onSlide(p0: View, p1: Float) {}

        })

    }

    private fun tapOnPlaylist(playlist: Playlist) {
        viewModel.tapOnPlaylist(playlist)
    }

    private fun showPlaylists(playlists: List<Playlist>) {
        playlistsAdapter.playlists.clear()
        playlistsAdapter.playlists.addAll(playlists)
        playlistsAdapter.notifyDataSetChanged()
    }

    fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun closeBottomSheet() {
        val bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        binding.overlay.visibility = View.GONE
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
        fun newInstance(track: Track): PlayerFragment {
            val args = bundleOf(TRACK to track)
            val fragment = PlayerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}