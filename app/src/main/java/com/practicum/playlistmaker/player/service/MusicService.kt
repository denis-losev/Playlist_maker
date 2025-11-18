package com.practicum.playlistmaker.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.search.domain.model.Track

public class MusicService : Service(){

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrack: Track? = null

    private val _playbackState = MutableLiveData<PlaybackState>()
    val playbackState: MutableLiveData<PlaybackState> = _playbackState

    private var isPrepared = false

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent): IBinder {
        currentTrack = requireNotNull(intent.getParcelableExtra(EXTRA_TRACK)) {
            "Track must be provided via EXTRA_TRACK"
        }
        initializeMediaPlayer()
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        _playbackState.value = PlaybackState.IDLE
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(currentTrack?.previewUrl)
            setOnPreparedListener {
                isPrepared = true
                _playbackState.postValue(PlaybackState.PREPARED)
            }
            setOnCompletionListener {
                _playbackState.postValue(PlaybackState.COMPLETED)
                stopForeground(true)
            }
            setOnErrorListener { mp, what, extra ->
                _playbackState.postValue(PlaybackState.ERROR)
                false
            }
            prepareAsync()
        }
    }

    fun play() {
        if (isPrepared) {
            mediaPlayer?.start()
            _playbackState.value = PlaybackState.PLAYING
        } else {
            initializeMediaPlayer()
        }
    }

    fun pause() {
        if (isPrepared && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            _playbackState.value = PlaybackState.PAUSED
            stopForeground(true)
        }
    }

    fun getCurrentPosition(): Int {
        return if (isPrepared) mediaPlayer?.currentPosition ?: 0 else 0
    }

    fun getCurrentTrack(): Track? = currentTrack

    fun showNotification() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    fun hideNotification() {
        stopForeground(true)
    }

    private fun createNotification(): Notification {
        val track = currentTrack ?: return createEmptyNotification()
        val notificationText = "${track.artistName} - ${track.trackName}"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Playlist Maker")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun createEmptyNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Playlist Maker")
            .setContentText("No track playing")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(true)
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "music_player_channel"
    }
}

enum class PlaybackState {
    IDLE, PREPARED, PLAYING, PAUSED, COMPLETED, ERROR
}