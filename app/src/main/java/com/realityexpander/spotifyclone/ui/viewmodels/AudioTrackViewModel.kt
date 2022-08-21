package com.realityexpander.spotifyclone.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.spotifyclone.exoplayer.AudioService
import com.realityexpander.spotifyclone.exoplayer.AudioServiceConnection
import com.realityexpander.spotifyclone.exoplayer.currentPlaybackPosition
import com.realityexpander.spotifyclone.common.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioTrackViewModel @Inject constructor(
    audioServiceConnection: AudioServiceConnection
) : ViewModel() {

    private val playbackState = audioServiceConnection.playbackState

    private val _curSongDuration = MutableLiveData<Long>()
    val curAudioTrackDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(AudioService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}



















