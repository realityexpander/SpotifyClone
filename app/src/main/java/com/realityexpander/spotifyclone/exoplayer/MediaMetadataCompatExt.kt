package com.realityexpander.spotifyclone.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.realityexpander.spotifyclone.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}