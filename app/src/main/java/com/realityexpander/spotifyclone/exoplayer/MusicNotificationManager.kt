package com.realityexpander.spotifyclone.exoplayer

import android.app.Notification.BADGE_ICON_LARGE
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.realityexpander.spotifyclone.R
import com.realityexpander.spotifyclone.common.Constants.NOTIFICATION_CHANNEL_ID
import com.realityexpander.spotifyclone.common.Constants.NOTIFICATION_ID

class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newAudioTrackCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager
    private var cachedBitmap: Bitmap? = null


    init {
        // Set default cached bitmap
        cachedBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.ic_music
        )

        // to control the playback of music
        val mediaController = MediaControllerCompat(context, sessionToken)

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel_name,
            R.string.notification_channel_description,
            NOTIFICATION_ID,
            DescriptionAdapter(mediaController),  // Custom description adapter for song info
            notificationListener  // to listen for actions including swipe to dismiss
        ).apply {
            setSmallIcon(R.drawable.ic_music)
            setMediaSessionToken(sessionToken)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setBadgeIconType(BADGE_ICON_LARGE)
            }
            setUseStopAction(true)        // show stop only if setUseNavigationActions is false
            setUseChronometer(true)       // unknown
            setUseNavigationActions(true) // skip to previous and next
        }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        // title of the currently playing audio Track
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newAudioTrackCallback() // to update the time in the notification

            return mediaController.metadata.description.title.toString()
        }

        // Return the pending intent that leads to our app's main activity
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        // subtitle of the currently playing audio track
        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        // album art of the currently playing audio track
        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {

            // use glide to load the album art uri and convert to bitmap
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri) // from MediaDescriptionCompat.Builder().setIconUri(song.description.iconUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        cachedBitmap = resource
                        callback.onBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })

            return cachedBitmap // return cached bitmap if available
        }
    }
}





















