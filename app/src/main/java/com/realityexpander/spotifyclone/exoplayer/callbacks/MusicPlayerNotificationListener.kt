package com.realityexpander.spotifyclone.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.realityexpander.spotifyclone.exoplayer.MusicService
import com.realityexpander.spotifyclone.other.Constants.NOTIFICATION_ID

class MusicPlayerNotificationListener(
    private val musicService: MusicService
) : PlayerNotificationManager.NotificationListener {

    // When user swipes the notification away, the service is stopped.
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)

        musicService.apply {
            stopForeground(true)  // clear the notification
            isForegroundService = false
            stopSelf()
        }
    }

    // When a song is started, the notification is added.
    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)

        musicService.apply {
            if(ongoing && !isForegroundService) {
                // start the foreground service
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this@apply::class.java) // this == MusicService
                )

                // Display the notification
                startForeground(NOTIFICATION_ID, notification)

                // Service is now in the foreground
                isForegroundService = true
            }
        }
    }
}











