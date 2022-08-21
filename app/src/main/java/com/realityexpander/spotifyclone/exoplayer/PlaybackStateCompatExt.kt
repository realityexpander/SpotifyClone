package com.realityexpander.spotifyclone.exoplayer

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isPrepared
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                state == PlaybackStateCompat.STATE_PAUSED
            )

// get the current playback position in milliseconds
inline val PlaybackStateCompat.currentPlaybackPosition: Long
    get() = if(state == PlaybackStateCompat.STATE_PLAYING) {

                // get the time at which the state was last updated.
                // SystemClock.elapsedRealtime() is the time since phone was last booted.
                // (Replicates restricted library function - getCurrentPosition())
                val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime

                (position + (timeDelta * playbackSpeed)).toLong()
            } else
                position