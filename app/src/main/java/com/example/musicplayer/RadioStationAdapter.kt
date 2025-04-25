package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RadioStationAdapter(
    private var stations: List<RadioStation>,
    private val onStationClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioStationAdapter.StationViewHolder>() {

    class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationImage: ImageView = itemView.findViewById(R.id.stationImage)
        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val stationCategory: TextView = itemView.findViewById(R.id.stationCategory)
        val featuredIcon: ImageView = itemView.findViewById(R.id.featuredIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.radio_station_item, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stations[position]
        
        holder.stationName.text = station.name
        holder.stationCategory.text = "Category: ${station.category}"
        
        // Set featured icon visibility
        holder.featuredIcon.visibility = if (station.featured) View.VISIBLE else View.GONE
        
        // Load station image
        if (station.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(station.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.stationImage)
        } else {
            // Use a placeholder if no image URL is provided
            holder.stationImage.setImageResource(R.drawable.ic_launcher_foreground)
        }
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onStationClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    fun updateStations(newStations: List<RadioStation>) {
        stations = newStations
        notifyDataSetChanged()
    }
}