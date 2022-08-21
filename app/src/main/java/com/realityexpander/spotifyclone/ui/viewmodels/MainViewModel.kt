package com.realityexpander.spotifyclone.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.realityexpander.spotifyclone.data.entities.AudioTrack
import com.realityexpander.spotifyclone.exoplayer.MusicServiceConnection
import com.realityexpander.spotifyclone.exoplayer.isPlayEnabled
import com.realityexpander.spotifyclone.exoplayer.isPlaying
import com.realityexpander.spotifyclone.exoplayer.isPrepared
import com.realityexpander.spotifyclone.common.Constants.MEDIA_ROOT_ID
import com.realityexpander.spotifyclone.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _audioTracks = MutableLiveData<Resource<List<AudioTrack>>>()
    val audioTracks: LiveData<Resource<List<AudioTrack>>> = _audioTracks

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingAudioTrack = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        // Indicate that media is loading
        _audioTracks.postValue(Resource.loading(null))

        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {

                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)

                    val items = children.map {
                        AudioTrack(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }

                    // Indicate that media is loaded
                    _audioTracks.postValue(Resource.success(items))
                }
            })
    }

    fun skipToNextAudioTrack() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousAudioTrack() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleAudioTrack(audioTrack: AudioTrack, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false

        if (isPrepared && audioTrack.mediaId ==
            curPlayingAudioTrack.value?.getString(METADATA_KEY_MEDIA_ID)
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(audioTrack.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()

        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {}
        )
    }
}

















