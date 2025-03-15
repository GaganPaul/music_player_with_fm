package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the playback control actions here
        when (intent?.action) {
            MusicService.ACTION_PLAY_PREVIOUS -> {
                // Handle previous action
                val previousIntent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.ACTION_PLAY_PREVIOUS
                }
                context?.startService(previousIntent)
            }
            MusicService.ACTION_TOGGLE_PLAYBACK -> {
                // Handle play/pause action
                val toggleIntent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.ACTION_TOGGLE_PLAYBACK
                }
                context?.startService(toggleIntent)
            }
            MusicService.ACTION_PLAY_NEXT -> {
                // Handle next action
                val nextIntent = Intent(context, MusicService::class.java).apply {
                    action = MusicService.ACTION_PLAY_NEXT
                }
                context?.startService(nextIntent)
            }
            else -> {
                // Do nothing
            }
        }
    }
}
