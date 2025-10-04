package com.practicum.playlistmaker.media.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.media.domain.model.Playlist
import com.practicum.playlistmaker.media.ui.view_model.playlists.PlaylistViewModel
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.search.ui.TrackAdapter
import com.practicum.playlistmaker.utils.BindingFragment
import com.practicum.playlistmaker.utils.debouncer.ClickDebouncer
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.practicum.playlistmaker.Constants.PLAYLIST
import com.practicum.playlistmaker.media.ui.state.playlists.PlaylistState
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class PlaylistFragment() : BindingFragment<FragmentPlaylistBinding>() {

    private val viewModel: PlaylistViewModel by viewModel()
    private lateinit var clickDebouncer: ClickDebouncer
    private lateinit var actionsBottomSheetBehavior: BottomSheetBehavior<MaterialCardView>

    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    private val tracksAdapter = TrackAdapter(
        onTrackClick = { tapOnTrack(it) },
        onTrackLongClick = { showDeleteTrackDialog(it) }
    )

    private lateinit var playlist: Playlist

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPlaylistBinding {
        return FragmentPlaylistBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickDebouncer = ClickDebouncer(coroutineScope = viewLifecycleOwner.lifecycleScope)

        playlist = arguments?.getParcelable(PLAYLIST)
            ?: throw IllegalArgumentException("playlist is required")

        viewModel.setPlaylist(playlist)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Playlist>(PLAYLIST)
            ?.observe(viewLifecycleOwner) { updatedPlaylist ->
                playlist = updatedPlaylist
                refreshUI()
            }

        setupUI()
        observeDuration()
        observeState()
        setupTracksBottomSheetHeight()
        setupActionsBottomSheetHeight()
        setupActionsBottomSheet()
        setupClickListeners()
    }


    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.tracksRecyclerView.adapter = tracksAdapter

        with(binding) {

            playlistName.text = playlist.name
            playlistDescription.text = playlist.description

            tracksCount.text = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.tracksCount,
                playlist.tracksCount
            )

            Glide.with(requireContext())
                .load(playlist.image)
                .transform(
                    CenterCrop(),
                    RoundedCorners(8)
                )
                .placeholder(R.drawable.cover_placeholder)
                .into(playlistCoverImage)

        }
    }

    private fun observeState() {
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlaylistState.Content -> {
                    showContentState(state.tracks)
                }

                is PlaylistState.Empty -> showEmptyState(state.message.toString())
                PlaylistState.Loading -> showLoadingState()
            }
        }
    }

    private fun showLoadingState() {
        with(binding) {
            tracksRecyclerView.visibility = View.GONE
        }
    }

    private fun showContentState(tracks: List<Track>) {
        with(binding) {
            tracksRecyclerView.visibility = View.VISIBLE
            tracksAdapter.submitList(tracks)
        }
    }

    private fun showEmptyState(message: String) {
        with(binding) {}
    }

    private fun observeDuration() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistDuration.collect { durationMs ->
                val durationMinutes = formatTrackTime(durationMs)
                binding.durationSummary.text = resources.getQuantityString(
                    R.plurals.minutes_count,
                    durationMinutes.toInt(),
                    durationMinutes
                )
            }
        }
    }

    private fun formatTrackTime(milliseconds: Long): Long = milliseconds / 60000

    private fun tapOnTrack(track: Track) {
        clickDebouncer.tryClick {
            viewModel.addTrackToHistory(track)

            val bundle = Bundle().apply { putParcelable(TRACK, track) }
            findNavController().navigate(
                R.id.playerFragment,
                bundle
            )
        }
    }

    private fun setupTracksBottomSheetHeight() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistBottomSheet)

        binding.playlistBottomSheet.post {
            val location = IntArray(2)
            binding.sharePlaylistButton.getLocationOnScreen(location)
            val buttonBottom = location[1] + binding.sharePlaylistButton.height
            val screenHeight = resources.displayMetrics.heightPixels
            val peekHeight = screenHeight - buttonBottom + dpToPx(24)
            bottomSheetBehavior.peekHeight = peekHeight
        }
    }

    private fun setupActionsBottomSheetHeight() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.actionsBottomSheet)

        binding.actionsBottomSheet.post {
            val location = IntArray(2)
            binding.sharePlaylistButton.getLocationOnScreen(location)
            val buttonBottom = location[1] + binding.sharePlaylistButton.height
            val screenHeight = resources.displayMetrics.heightPixels

            val peekHeight = screenHeight - buttonBottom + dpToPx(24)

            bottomSheetBehavior.maxHeight = peekHeight
            bottomSheetBehavior.peekHeight = peekHeight

            bottomSheetBehavior.isFitToContents = false
            bottomSheetBehavior.skipCollapsed = false
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showDeleteTrackDialog(track: Track) {

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удаление трека")
            .setMessage("Хотите удалить трек \"${track.trackName}\" из плейлиста?")
            .setPositiveButton("ДА") { dialog, which ->
                deleteTrackFromPlaylist(track)
                dialog.dismiss()
            }
            .setNegativeButton("НЕТ") { dialog, which ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        dialog.show()
    }

    private fun deleteTrackFromPlaylist(track: Track) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = viewModel.removeTrackFromPlaylist(track.trackId, playlist)

                if (success) {
                    val currentTracks = tracksAdapter.tracks.toMutableList()
                    currentTracks.removeAll { it.trackId == track.trackId }
                    tracksAdapter.submitList(currentTracks)

                    updateCountersAfterDeletion()
                } else {
                    showErrorDialog("Не удалось удалить трек")
                }
            } catch (e: Exception) {
                showErrorDialog("Ошибка при удалении трека: ${e.message}")
            }
        }
    }

    private fun updateCountersAfterDeletion() {
        val newCount = playlist.tracksCount - 1
        playlist = playlist.copy(tracksCount = newCount)

        binding.tracksCount.text = resources.getQuantityString(
            R.plurals.tracks_count,
            newCount,
            newCount
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadPlaylistDuration(playlist)
        }
    }

    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupClickListeners() {
        binding.sharePlaylistButton.setOnClickListener {
            checkAndSharePlaylist()
        }

        binding.dotsButton.setOnClickListener {
            showActionsBottomSheet()
        }

        binding.shareActionButton.setOnClickListener {
            hideActionsBottomSheet()
            checkAndSharePlaylist()
        }

        binding.editPlaylistButton.setOnClickListener {
            hideActionsBottomSheet()
            navigateToEditPlaylist()
        }

        binding.deletePlaylistButton.setOnClickListener {
            hideActionsBottomSheet()
            showDeletePlaylistDialog()
        }
    }

    private fun navigateToEditPlaylist() {
        hideActionsBottomSheet()

        binding.overlay.postDelayed({
            if (!isAdded || view == null) return@postDelayed

            try {
                val bundle = Bundle().apply {
                    putParcelable(PLAYLIST, playlist)
                }

                findNavController().navigate(
                    R.id.editPlaylistFragment,
                    bundle
                )
            } catch (e: Exception) {
                println("[FRAGMENT] Navigation error: ${e.message}")
            }
        }, 300)
    }

    private fun checkAndSharePlaylist() {

        if (playlist.tracksCount == 0) {
            showEmptyPlaylistMessage()
        } else {
            sharePlaylist()
        }
    }

    private fun showEmptyPlaylistMessage() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Поделиться плейлистом")
            .setMessage("В этом плейлисте нет списка треков, которым можно поделиться")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sharePlaylist() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val tracks = viewModel.getTracksInPlaylist(playlist)
                val shareText = createShareText(tracks)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Поделиться плейлистом"))
            } catch (e: Exception) {
                showErrorDialog("Ошибка при создании списка для sharing")
            }
        }
    }

    private fun createShareText(tracks: List<Track>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("${playlist.name}\n")
        if (playlist.description.isNotEmpty()) {
            stringBuilder.append("${playlist.description}\n")
        }

        val tracksCountText = resources.getQuantityString(
            R.plurals.tracks_count,
            tracks.size,
            tracks.size
        )
        stringBuilder.append("$tracksCountText\n")
        stringBuilder.append("\n")

        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val trackDuration = formatTrackDuration(track.trackTimeMillis)
            stringBuilder.append("$trackNumber. ${track.artistName} - ${track.trackName} ($trackDuration)\n")
        }

        return stringBuilder.toString()
    }

    private fun formatTrackDuration(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun setupActionsBottomSheet() {
        actionsBottomSheetBehavior = BottomSheetBehavior.from(binding.actionsBottomSheet)

        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (!isAdded || view == null || _binding == null) return

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                    }

                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (!isAdded || view == null || _binding == null) return
                binding.overlay.alpha = slideOffset
            }
        }

        bottomSheetCallback?.let {
            actionsBottomSheetBehavior.addBottomSheetCallback(it)
        }

        binding.overlay.setOnClickListener {
            hideActionsBottomSheet()
        }

        setupMenuPlaylistInfo()
    }

    private fun setupMenuPlaylistInfo() {
        val playlistInfoView = binding.playlistView
        val playlistCoverImage: ImageView = playlistInfoView.playlistCoverImage
        val playlistName: MaterialTextView = playlistInfoView.playlistName
        val tracksCount: MaterialTextView = playlistInfoView.tracksCount
        Glide.with(requireContext())
            .load(playlist.image)
            .transform(
                CenterCrop(),
                RoundedCorners(8)
            )
            .placeholder(R.drawable.cover_placeholder)
            .into(playlistCoverImage)

        playlistName.text = playlist.name

        tracksCount.text = resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.tracksCount,
            playlist.tracksCount
        )
    }

    private fun showActionsBottomSheet() {
        if (!isAdded || view == null) return

        binding.actionsBottomSheet.visibility = View.VISIBLE
        actionsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideActionsBottomSheet() {
        if (!isAdded || view == null) return

        actionsBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showDeletePlaylistDialog() {

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить плейлист")
            .setMessage("Хотите удалить плейлист?")
            .setPositiveButton("Да") { dialog, which ->
                deletePlaylist()
                dialog.dismiss()
            }
            .setNegativeButton("Нет") { dialog, which ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun deletePlaylist() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deletePlaylist(playlist)
                findNavController().popBackStack()
            } catch (e: Exception) {
                showErrorDialog("Ошибка при удалении плейлиста: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetCallback?.let {
            actionsBottomSheetBehavior.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
    }

    private fun refreshUI() {
        setupUI()
        viewModel.loadPlaylistDuration(playlist)
        viewModel.fillData()
    }
}