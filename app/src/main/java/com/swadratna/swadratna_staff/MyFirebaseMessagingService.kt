package com.swadratna.swadratna_staff

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
        private const val CHANNEL_ID = "swadratna_notifications"
        private const val CHANNEL_NAME = "SwadRatna Notifications"
        private const val CHANNEL_ID_ORDER = "swadratna_orders"
        private const val CHANNEL_NAME_ORDER = "SwadRatna Orders"
        private const val CHANNEL_ID_BILL = "swadratna_bills"
        private const val CHANNEL_NAME_BILL = "SwadRatna Bills"
        private const val CHANNEL_DESCRIPTION = "Notifications for SwadRatna Staff App"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "SwadRatna", it.body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()

    }

    enum class DeepLinkType(val type: String) {
        TYPE_NEWORDER("new_order"),
        TYPE_REQUESTBILL("bill_requested")
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: data["action"] ?: "general"
        val orderId = data["order_id"]
        val billId = data["bill_id"]
        val tableNumber = data["table_number"]
        val deepLink = data["deeplink"]
        
        Log.d(TAG, "Handling data message - Type: $type, OrderId: $orderId, BillId: $billId, TableNumber: $tableNumber")
        
        val title = when (type) {
            "new_kot" -> "New KOT Received"
            "approve_bill" -> "Bill Approval Request"
            else -> data["title"] ?: "SwadRatna"
        }

        val body = when (type) {
            "new_kot" -> "New items ordered for ${tableNumber ?: "a table"}"
            "approve_bill" -> "Bill approval requested for Order #$orderId"
            else -> data["body"] ?: ""
        }

        // Check if app is in foreground
        if (isAppInForeground()) {
            Log.d(TAG, "App is in foreground, sending intent to MainActivity for in-app notification")
            // Send intent to MainActivity to show in-app notification
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_type", type)
                putExtra("order_id", orderId?.toIntOrNull() ?: -1)
                putExtra("bill_id", billId)
                putExtra("table_number", tableNumber?.toIntOrNull() ?: -1)
                putExtra("notification_title", title)
                putExtra("notification_body", body)
                putExtra("in_app_notification", true) // Flag to indicate this is for in-app notification
                putExtra("deeplink", deepLink)
            }
            startActivity(intent)
        } else {
            Log.d(TAG, "App is in background, sending system notification")
            sendNotification(title, body, deepLink, type, orderId, tableNumber, "high")
        }
    }

    private fun sendNotification(
        title: String, 
        body: String,
        deepLink: String? = null,
        type: String = "general",
        orderId: String? = null,
        tableNumber: String? = null,
        priority: String = "normal"
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Determine Channel ID based on type
        // Use separate channels to support distinct sounds in background
        val channelId = when(type) {
            "new_kot", "new_order" -> CHANNEL_ID_ORDER
            "approve_bill", "bill_requested", "request_bill" -> CHANNEL_ID_BILL
            else -> CHANNEL_ID
        }
        
        val channelName = when(channelId) {
            CHANNEL_ID_ORDER -> CHANNEL_NAME_ORDER
            CHANNEL_ID_BILL -> CHANNEL_NAME_BILL
            else -> CHANNEL_NAME
        }
        
        // Determine Sound URI
        // Expecting user to add 'sound_order_alert.mp3' and 'sound_bill_alert.mp3' in res/raw
        val soundUri = try {
            when(type) {
                "new_kot", "new_order" -> {
                    // Check if custom sound exists, else fallback to Ringtone
                    val resId = resources.getIdentifier("sound_order_alert", "raw", packageName)
                    if (resId != 0) {
                        Uri.parse("android.resource://$packageName/$resId")
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    }
                }
                "approve_bill", "bill_requested", "request_bill" -> {
                    val resId = resources.getIdentifier("sound_bill_alert", "raw", packageName)
                    if (resId != 0) {
                        Uri.parse("android.resource://$packageName/$resId")
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                }
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } catch (e: Exception) {
            // Fallback safety
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if channel exists before creating/recreating to avoid unnecessary overhead
            // Note: Once created, sound cannot be changed without deleting the channel or reinstalling app
            // If you change the sound file, you might need to change channel ID or uninstall app
            
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    importance
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("order_id", orderId?.toIntOrNull() ?: -1)
            putExtra("table_number", tableNumber?.toIntOrNull() ?: -1)
            
            // Add other data that might be needed
            if ((type == "approve_bill" || type == "bill_requested") && deepLink != null) {
                 val uri = deepLink.toUri()
                 val billIdParam = uri.getQueryParameter("billId")
                 putExtra("bill_id", billIdParam)
            }

            if (type == "new_order" || type == "payment_completed") {
                val deepLinkUri = if(deepLink != null) {
                    deepLink.toUri()
                } else {
                    "swadratna://order?tableNumber=${tableNumber ?: 0}&orderId=${orderId ?: 0}".toUri()
                }
                data = deepLinkUri
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(if (priority == "high") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setSound(soundUri)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
        
        if (priority == "high") {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }

        val notificationId = System.currentTimeMillis().toInt()
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification sent successfully - ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }

    private fun sendNotification(title: String, body: String) {
        sendNotification(title, body, "general")
    }

    private fun isAppInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
}