package com.practicum.playlistmaker.presentation.activities.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.activities.search.SearchActivity.Companion.TRACK
import com.practicum.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private val gson = Gson()
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private var playerState = STATE_DEFAULT
    private lateinit var toolbar: Toolbar
    private lateinit var track: Track
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var trackTimeMillis: TextView
    private lateinit var artworkUrl100: ImageView
    private lateinit var albumValue: TextView
    private lateinit var yearValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var countryValue: TextView
    private lateinit var playBtn: ImageView
    private lateinit var playDuration: TextView
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                playDuration.text = SimpleDateFormat(
                    "mm:ss",
                    Locale.getDefault()
                ).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, CLICK_DEBOUNCE_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        toolbar = findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        track = gson.fromJson(intent.getStringExtra(TRACK), Track::class.java)

        artworkUrl100 = findViewById(R.id.coverImage)
        trackName = findViewById(R.id.title)
        artistName = findViewById(R.id.artist)
        trackTimeMillis = findViewById(R.id.trackTimeValue)
        albumValue = findViewById(R.id.albumValue)
        yearValue = findViewById(R.id.yearValue)
        genreValue = findViewById(R.id.genreValue)
        countryValue = findViewById(R.id.countryValue)
        playBtn = findViewById(R.id.play_btn)
        playDuration = findViewById(R.id.play_duration)

        getCoverImage()
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTimeMillis.text = track.getTrackDuration()
        albumValue.text = track.collectionName
        yearValue.text = track.getTrackYear()
        genreValue.text = track.primaryGenreName
        countryValue.text = track.country

        preparePlayer()

        playBtn.setOnClickListener {
            playbackControl()
        }
    }

    private fun getCoverImage() {
        Glide.with(artworkUrl100)
            .load(
                track.getCoverArtwork()
            )
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.cover_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(artworkUrl100)
    }

    private fun preparePlayer() {
        try {
            mediaPlayer.setDataSource(track.previewUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                playBtn.isEnabled = true
                playerState = STATE_PREPARED
            }
            mediaPlayer.setOnCompletionListener {
                playBtn.setImageResource(R.drawable.play_btn)
                playDuration.text = ZERO_TIMER
                playerState = STATE_PREPARED
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Хьюстон, у нас проблемы с ${e.message}!", Toast.LENGTH_LONG)
                .show()
            e.printStackTrace()
        }

    }

    private fun startPlayer() {
        mediaPlayer.start()
        playBtn.setImageResource(R.drawable.pause_btn)
        handler.post(updateTimerRunnable)
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playBtn.setImageResource(R.drawable.play_btn)
        playerState = STATE_PAUSED
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimerRunnable)
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimerRunnable)
        mediaPlayer.release()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val ZERO_TIMER = "00:00"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}