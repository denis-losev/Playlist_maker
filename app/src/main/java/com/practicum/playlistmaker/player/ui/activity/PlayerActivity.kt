package com.practicum.playlistmaker.player.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityPlayerBinding
import com.practicum.playlistmaker.search.domain.model.Track
import com.practicum.playlistmaker.Constants.TRACK
import com.practicum.playlistmaker.Constants.ZERO_TIMER
import com.practicum.playlistmaker.player.PlayerState
import com.practicum.playlistmaker.player.ui.view_model.PlayerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        track = Gson().fromJson(intent.getStringExtra(TRACK), Track::class.java)

        viewModel.preparePlayer(track)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

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
        }
    }

    private fun setupObservers() {
        viewModel.getState().observe(this) { state ->
            when (state) {
                is PlayerState.Default -> {
                    binding.playBtn.isEnabled = false
                    binding.playBtn.setImageResource(R.drawable.play_btn)
                    binding.trackDurationValue.text = ZERO_TIMER
                }

                is PlayerState.Prepared -> {
                    binding.playBtn.isEnabled = true
                    binding.playBtn.setImageResource(R.drawable.play_btn)
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
                }
            }
        }
    }

    fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}