package com.realityexpander.spotifyclone.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.realityexpander.spotifyclone.exoplayer.FirebaseMusicSource

class AudioPlaybackPreparer(
    private val firebaseMusicSource: FirebaseMusicSource,
    private val playerPrepared: (MediaMetadataCompat?) -> Unit
) : MediaSessionConnector.PlaybackPreparer {

    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false

    // Return the types of actions that this class supports.
    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or  // these are bit flags
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    // After the media is prepared(loaded), get the player ready to play the media (song).
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {

        firebaseMusicSource.whenReady {
            val itemToPlay =
                firebaseMusicSource.audioTracks.find {
                    mediaId == it.description.mediaId
                }

            playerPrepared(itemToPlay)
        }
    }

    // for google voice search
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}















