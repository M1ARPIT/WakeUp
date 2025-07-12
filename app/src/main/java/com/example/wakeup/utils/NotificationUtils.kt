package com.example.wakeup.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.wakeup.MainActivity
import com.example.wakeup.R
import kotlin.random.Random

object NotificationUtils {

    private const val CHANNEL_ID = "wake_up_channel"
    private const val CHANNEL_NAME = "Motivation Notifications"

    private val titleOptions = listOf(
        "Boost 💡", "Inspire 🌟", "Rise 🔥", "Spark ⚡", "Thrive 🌱",
        "Shine ✨", "Focus 🎯", "Momentum 🚀", "Awaken 🌅", "Power Up 💥"
    )

    fun showNotification(context: Context, content: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows motivational quotes"
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val randomTitle = titleOptions[Random.nextInt(titleOptions.size)]

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(randomTitle)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
