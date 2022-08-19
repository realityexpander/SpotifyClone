package com.realityexpander.spotifyclone.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.realityexpander.spotifyclone.data.remote.MusicDatabase
import com.realityexpander.spotifyclone.exoplayer.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase  // Firebase FireStore Music Database
) {

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_DOWNLOADING

        // Get songs from Firebase
        val (allSongs, isSuccessful) = musicDatabase.getAllSongs()
        if (allSongs.isEmpty() && !isSuccessful) {
            state = STATE_ERROR
            return@withContext
        }

        // Convert songs metadata to MediaMetadataCompat
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }
        state = STATE_READY_TO_PLAY
    }

    // Define From where to stream the song
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource =
            ConcatenatingMediaSource() // list of songs to play (one after another)

        // Build the list of songs to play
        songs.forEach { song ->
            val mediaSource =
                ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(
                        song.getString(METADATA_KEY_MEDIA_URI).toUri()
                    )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    // Build browsable media items
    fun asMediaItems() =
        songs.map { song ->
            val desc =
                MediaDescriptionCompat.Builder()
                    .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
                    .setTitle(song.description.title)
                    .setSubtitle(song.description.subtitle)
                    .setMediaId(song.description.mediaId)
                    .setIconUri(song.description.iconUri)
                    .build()

            MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
        }.toMutableList()

    // List of lambda functions to run on each state change
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_INITIAL
        set(value) {
            if (value == STATE_READY_TO_PLAY || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_READY_TO_PLAY)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if (state == STATE_INITIAL || state == STATE_DOWNLOADING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_READY_TO_PLAY)
            return true
        }
    }
}

enum class State {
    STATE_INITIAL,       // initial state (CREATED)
    STATE_DOWNLOADING,  // downloading from the firebase database (INITIALIZING)
    STATE_READY_TO_PLAY,   // successfully downloaded from the firebase database, ready to play (INITIALIZED)
    STATE_ERROR
}















