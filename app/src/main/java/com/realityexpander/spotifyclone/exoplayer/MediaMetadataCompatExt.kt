package com.realityexpander.spotifyclone.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.realityexpander.spotifyclone.data.entities.AudioTrack

fun MediaMetadataCompat.toAudioTrack(): AudioTrack? {
    return description?.let {
        AudioTrack(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}