package com.realityexpander.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.realityexpander.spotifyclone.common.Constants.NETWORK_ERROR
import com.realityexpander.spotifyclone.common.Event
import com.realityexpander.spotifyclone.common.Resource

// Sits between fragment/activity and the MusicService
class MusicServiceConnection(
    context: Context
) {

    // is connected to the MusicService
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong: LiveData<MediaMetadataCompat?> = _curPlayingSong

    // For access to playback transport controls and callbacks
    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls  // only accessible after mediaController is instantiated & connected

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    // Connect to the MediaBrowser & get reference to the MediaController
    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            Log.d("MusicServiceConnection", "CONNECTED")

            mediaController = MediaControllerCompat(
                context,
                mediaBrowser.sessionToken
            ).apply {
                registerCallback(MediaControllerCallback())
            }

            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            Log.d("MusicServiceConnection", "SUSPENDED")

            _isConnected.postValue(Event(Resource.error(
                "The connection was suspended",
                false
            )))
        }

        override fun onConnectionFailed() {
            Log.d("MusicServiceConnection", "FAILED")

            _isConnected.postValue(Event(Resource.error(
                "Couldn't connect to media browser",
                false
            )))
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        // When a new track is ready to be played, this callback is called to update the title/artist/album art etc
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        // Detect errors and possibly other events from the mediaSession from the MediaController
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)

            when(event) {
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldn't connect to the server. Please check your internet connection.",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}

















