package com.practicum.playlistmaker.activities.search

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.activities.search.SearchActivity.Companion.TRACK
import com.practicum.playlistmaker.track.Track

class PlayerActivity : AppCompatActivity() {

    private val gson = Gson()
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

        getCoverImage()
        trackName.text = track.trackName
        artistName.text = track.artistName
        trackTimeMillis.text = track.getTrackDuration()
        albumValue.text = track.collectionName
        yearValue.text = track.getTrackYear()
        genreValue.text = track.primaryGenreName
        countryValue.text = track.country
    }

    private fun getCoverImage() {
        Glide.with(artworkUrl100)
            .load(track.artworkUrl100.replaceAfterLast("/", "512x512bb.jpg"))
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.cover_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(artworkUrl100)
    }
}