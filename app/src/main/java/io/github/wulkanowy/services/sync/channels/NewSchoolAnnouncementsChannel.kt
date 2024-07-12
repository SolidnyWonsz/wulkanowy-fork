package io.github.wulkanowy.services.sync.channels

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.wulkanowy.R
import javax.inject.Inject

@TargetApi(26)
class NewSchoolAnnouncementsChannel @Inject constructor(
    private val notificationManager: NotificationManagerCompat,
    @ApplicationContext private val context: Context
) : Channel {

    companion object {
        const val CHANNEL_ID = "new_school_announcements_channel"
    }

    override fun create() {
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_new_school_announcement), NotificationManager.IMPORTANCE_HIGH)
                .apply {
                    enableLights(true)
                    enableVibration(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                })
    }
}