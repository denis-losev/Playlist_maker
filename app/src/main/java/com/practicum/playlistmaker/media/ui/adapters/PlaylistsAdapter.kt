package com.practicum.playlistmaker.media.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.media.domain.model.Playlist

class PlaylistsAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>() {

    var playlists = ArrayList<Playlist>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_media_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val playlistCoverImage: ImageView = view.findViewById(R.id.playlistCoverImage)
        private val playlistName: TextView = view.findViewById(R.id.playlistName)
        private val playlistTracksCount: TextView = view.findViewById(R.id.tracksCount)

        fun bind(model: Playlist) {
            playlistName.text = model.name
            playlistTracksCount.text = itemView.resources.getQuantityString(
                R.plurals.tracks_count,
                model.tracksCount,
                model.tracksCount
            )

            Glide.with(itemView.context)
                .load(model.image)
                .placeholder(R.drawable.cover_placeholder)
                .into(playlistCoverImage)
        }
    }
}