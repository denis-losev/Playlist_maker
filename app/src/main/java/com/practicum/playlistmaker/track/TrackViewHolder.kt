package com.practicum.playlistmaker.track

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.practicum.playlistmaker.R

class TrackViewHolder(private val trackView: View) : RecyclerView.ViewHolder(trackView) {

    private val title: TextView
    private val artist: TextView
    private val duration: TextView
    private val cover: ImageView

    init {
        title = trackView.findViewById(R.id.title)
        artist = trackView.findViewById(R.id.artist)
        duration = trackView.findViewById(R.id.duration)
        cover = trackView.findViewById(R.id.cover)
    }

    fun bind(model: Track) {
        title.text = model.trackName
        artist.text = model.artistName
        duration.text = model.getTrackDuration()
        Glide.with(trackView.context)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.cover_placeholder)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(cover)
    }
}