package com.swadratna.swadratna_staff.ui.components

import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Notification manager to handle in-app notifications
class InAppNotificationManager {
    private val _currentNotification = MutableStateFlow<InAppNotification?>(null)
    val currentNotification: StateFlow<InAppNotification?> = _currentNotification.asStateFlow()
    
    private val notificationQueue = mutableListOf<InAppNotification>()
    
    fun showNotification(notification: InAppNotification) {
        // If there's already a notification showing, add to queue
        if (_currentNotification.value != null) {
            notificationQueue.add(notification)
            Log.d("InAppNotificationManager", "Notification queued. Queue size: ${notificationQueue.size}")
        } else {
            // No notification currently showing, show this one
            _currentNotification.value = notification
            Log.d("InAppNotificationManager", "Notification shown immediately: ${notification.title}")
        }
    }
    
    fun dismissNotification() {
        // Remove current notification
        _currentNotification.value = null
        
        // Check if there are queued notifications
        if (notificationQueue.isNotEmpty()) {
            // Show the next notification from queue after a short delay
            val nextNotification = notificationQueue.removeAt(0)
            Log.d("InAppNotificationManager", "Showing next queued notification: ${nextNotification.title}")
            // Use a small delay to allow UI to transition smoothly
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(300)
                _currentNotification.value = nextNotification
            }
        }
    }
    
    fun clearNotification() {
        notificationQueue.clear()
        _currentNotification.value = null
        Log.d("InAppNotificationManager", "All notifications cleared")
    }
    
    fun forceShowNotification(notification: InAppNotification) {
        // Force show this notification immediately, replacing current one
        _currentNotification.value = notification
        Log.d("InAppNotificationManager", "Notification forced: ${notification.title}")
    }
}

// Create a composition local for the notification manager
val LocalNotificationManager = staticCompositionLocalOf<InAppNotificationManager> {
    error("No InAppNotificationManager provided")
}

// Provider composable
@Composable
fun InAppNotificationProvider(
    notificationManager: InAppNotificationManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalNotificationManager provides notificationManager
    ) {
        content()
    }
}