package com.example.musicplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SongsAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val onLikeClick: (Song, Boolean) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {

    // Method to provide access to the songs list
    fun getSongs(): List<Song> {
        return songs
    }

    fun shuffleSongs() {
        songs = songs.shuffled()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
        holder.itemView.setOnClickListener { onItemClick(song) }
        
        // Set like button click listener
        holder.likeButton.setOnClickListener {
            val newLikedState = !song.isLiked
            song.isLiked = newLikedState
            onLikeClick(song, newLikedState)
            notifyItemChanged(position)
        }
        
        // Set more options button click listener
        holder.moreButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.song_options_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        onDeleteClick(song)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Highlight the currently playing song
        if (song.isPlaying) {
            // Apply highlight styling to the currently playing song
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.shuffleOnColor))
        } else {
            // Reset background color for other songs
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun submitList(newList: List<Song>) {
        songs = newList
        notifyDataSetChanged()
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
        private val albumImageView: ImageView = itemView.findViewById(R.id.albumImageView)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        fun bind(song: Song) {
            titleTextView.text = song.title
            artistTextView.text = song.artist
            titleTextView.setTextColor(Color.BLACK)
            artistTextView.setTextColor(Color.BLACK)
            
            // Set like button state
            likeButton.setImageResource(
                if (song.isLiked) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            
            Glide.with(itemView.context)
                .load(song.albumArtUri)
                .placeholder(R.drawable.audioicon) // Placeholder image
                .error(R.drawable.audioicon) // Error image
                .into(albumImageView)
        }
    }
}
