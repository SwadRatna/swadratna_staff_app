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
import androidx.core.net.toUri
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.Lifecycle

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
        private const val CHANNEL_ID = "swadratna_notifications"
        private const val CHANNEL_NAME = "SwadRatna Notifications"
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
                putExtra("order_id", orderId)
                putExtra("bill_id", billId)
                putExtra("table_number", tableNumber)
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
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            orderId?.let { putExtra("orderId", it.toIntOrNull()) }
            tableNumber?.let { putExtra("tableNumber", it.toIntOrNull()) }


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
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(if (priority == "high") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
        
        if (priority == "high") {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
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