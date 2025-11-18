package com.practicum.playlistmaker.player.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistedTrackState
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistsState
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.service.MusicService
import com.practicum.playlistmaker.player.ui.adapters.PlaylistsInPlayerAdapter
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import com.practicum.playlistmaker.utils.BindingFragment
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : BindingFragment<FragmentPlayerBinding>() {

    private val viewModel: PlayerViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer

    private lateinit var playlistsAdapter: PlaylistsInPlayerAdapter

    private lateinit var track: Track

    private var musicService: MusicService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
            viewModel.setMusicService(musicService)
            viewModel.preparePlayer(track)
        }

        override fun onServiceDisconnected(arg0: ComponentName?) {
            musicService = null
            isBound = false
            viewModel.setMusicService(null)
        }
    }

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

        checkNotificationPermission()

        playlistsAdapter = PlaylistsInPlayerAdapter { playlist ->
            tapOnPlaylist(playlist)
        }

        viewModel.preparePlayer(track)

        bindMusicService()

        createPlaylistButtonClick()
        setupUI()
        setupObservers()
        createBottomSheet()
    }

    private fun bindMusicService() {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            putExtra(MusicService.EXTRA_TRACK, track)
        }
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindMusicService() {
        if (isBound) {
            requireContext().unbindService(connection)
            isBound = false
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResumed()
    }

    override fun onStop() {
        super.onStop()
        if (!requireActivity().isFinishing) {
            if (!requireActivity().isChangingConfigurations) {
                viewModel.onAppMinimized()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onScreenClosed()
        unbindMusicService()
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
                    binding.playBtn.setPlaying(false)
                    binding.currentPlayPosition.text = ZERO_TIMER
                }

                is PlayerState.Prepared -> {
                    binding.playBtn.isEnabled = true
                    binding.playBtn.setPlaying(false)
                    binding.currentPlayPosition.text = ZERO_TIMER
                    updateFavoriteButton(state.track.isFavorite)
                }

                is PlayerState.Playing -> {
                    binding.playBtn.setPlaying(true)
                    binding.currentPlayPosition.text = formatTime(state.currentPosition)
                }

                is PlayerState.Paused -> {
                    binding.playBtn.setPlaying(false)
                    binding.currentPlayPosition.text = formatTime(state.currentPosition)
                }

                is PlayerState.Completed -> {
                    binding.playBtn.setPlaying(false)
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
                    playlistsAdapter.playlists.clear()
                    playlistsAdapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.getPlaylistedTrackState().observe(viewLifecycleOwner) { state ->
            when (state) {
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

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    requireContext(),
                    "Разрешение на уведомления получено",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Уведомления не будут показываться",
                    Toast.LENGTH_SHORT
                ).show()
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

        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }
}