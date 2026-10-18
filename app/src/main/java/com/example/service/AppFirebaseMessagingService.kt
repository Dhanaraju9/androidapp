package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.util.NotificationSettingsManager
import com.example.util.FirebaseSyncManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationsEnabled = NotificationSettingsManager.isNotificationsEnabled(this)
        if (!notificationsEnabled) return

        // If the message contains a notification payload, it might be handled by the system
        // if the app is in the background. If in the foreground, we handle it here.
        // If it's a data message, we always handle it here.
        
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Check out the latest content!"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val soundEnabled = NotificationSettingsManager.isNotificationSoundEnabled(this)
        val vibrationEnabled = NotificationSettingsManager.isNotificationVibrationEnabled(this)

        val channelId = "push_notification_channel_s${if (soundEnabled) "1" else "0"}_v${if (vibrationEnabled) "1" else "0"}"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Notifications"
            val descriptionText = "Notifications about updates and news"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                if (soundEnabled) {
                    setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                } else {
                    setSound(null, null)
                }
                enableVibration(vibrationEnabled)
                if (vibrationEnabled) {
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                } else {
                    vibrationPattern = null
                }
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!soundEnabled) {
            builder.setSilent(true)
            builder.setSound(null)
        } else {
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }

        if (!vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        } else {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission rejected or missing at runtime
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        // Associate token with user profile in Firestore
        val currentUser = FirebaseSyncManager.auth.currentUser
        if (currentUser != null && currentUser.email != null) {
            FirebaseSyncManager.updateFcmToken(currentUser.email!!, token)
        }
    }
}
