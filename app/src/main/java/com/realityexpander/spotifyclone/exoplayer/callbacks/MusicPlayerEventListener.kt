package com.realityexpander.spotifyclone.exoplayer.callbacks

import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.realityexpander.spotifyclone.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.EventListener {  // deprecated use Player.Listener instead

    // Use onPlaybackStateChanged(int) and onPlayWhenReadyChanged(boolean, int) instead.

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)

        // If player is ready but dont want to start playing yet, then stop the foreground service.
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)  // keep the notification
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)

        Toast.makeText(musicService, "An error occurred: ${error.message}", Toast.LENGTH_LONG).show()
        Log.d("MusicPlayerEventListen", "An unknown error occurred: ${error.message}")
    }
}