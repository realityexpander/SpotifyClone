package com.realityexpander.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.realityexpander.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.realityexpander.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.realityexpander.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.realityexpander.spotifyclone.common.Constants.MEDIA_ROOT_ID
import com.realityexpander.spotifyclone.common.Constants.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    // Music Service background processing job
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // Tracks the current session for the playing of the media (audio)
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false // is the service playing a audio track or not

    private var curPlayingAudioTrack: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    companion object {
        var curSongDuration = 0L  // milliseconds
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Fetch the audio track data from the Firebase database
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        // Pending intent to open up this app from notification
        val activityIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(this, 0, it, 0)
            }


        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        // Set the MediaBrowserServiceCompat.Token for the MediaSessionCompat
        sessionToken = mediaSession.sessionToken

        // Create the notification for player
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            // Called when the current audio track changes
            curSongDuration = exoPlayer.duration  // milliseconds
        }


        // set the track to play first
        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) { audioTrack ->
            // Called when the current audio track changes
            curPlayingAudioTrack = audioTrack

            preparePlayer(
                firebaseMusicSource.audioTracks,
                audioTrack,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        // Listen to player state changes and errors
        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)

        // Show notification for control of the player
        musicNotificationManager.showNotification(exoPlayer)
    }

    // Used to get media description for the an audio track (when track changes), also intercept to player events
    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {

        // windowIndex is the index of the track that is now playing
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.audioTracks[windowIndex].description
        }
    }

    // Create the list of songs to be played for the exoPlayer
    private fun preparePlayer(
        audioTracks: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curAudioTrackIndex =
            if (curPlayingAudioTrack == null)
                0
            else
                audioTracks.indexOf(itemToPlay)

        // Convert the list of songs to a list of MediaMetadataCompat objects
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))

        // Set the seek to the start of the audio track
        exoPlayer.seekTo(curAudioTrackIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    // When the user stops the app (from the task manager) the player is stopped
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    // Gets the root of the browser tree (the media library)
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>  // playable audio track or list of audio tracks
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = firebaseMusicSource.whenReady { isTracksDownloaded ->
                    if (isTracksDownloaded) {
                        result.sendResult(firebaseMusicSource.asMediaItems())  // send the list of audio tracks

                        if (!isPlayerInitialized && firebaseMusicSource.audioTracks.isNotEmpty()) {

                            // Set the track to play first
                            preparePlayer(
                                firebaseMusicSource.audioTracks,
                                firebaseMusicSource.audioTracks[0],
                                false
                            )

                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)

                        result.sendResult(null)
                    }
                }

                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}























