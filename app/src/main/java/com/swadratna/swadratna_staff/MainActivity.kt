package com.swadratna.swadratna_staff

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.collectAsState
import com.swadratna.swadratna_staff.data.remote.repositories.AuthRepository
import com.swadratna.swadratna_staff.data.remote.repositories.TokenRepository
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor
import com.swadratna.swadratna_staff.navigation.NavigationComponent
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.components.InAppNotificationManager
import com.swadratna.swadratna_staff.ui.components.InAppNotificationProvider
import com.swadratna.swadratna_staff.ui.theme.SwadRatna_StaffTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        fun getDeepLinkData(context: Context): DeepLinkData? {
            val sharedPref = context.getSharedPreferences("deep_link_prefs", Context.MODE_PRIVATE)
            val type = sharedPref.getString("deep_link_type", null)
            val orderId = sharedPref.getInt("deep_link_order_id", -1)
            val billId = sharedPref.getString("deep_link_bill_id", null)
            val tableNumber = sharedPref.getInt("deep_link_table_number", -1)
            
            Log.d("MainActivity", "getDeepLinkData - Type: $type, OrderId: $orderId, BillId: $billId, TableNumber: $tableNumber")
            
            return if (type != null) {
                DeepLinkData(type, orderId, billId, tableNumber)
            } else {
                null
            }
        }
        
        fun clearDeepLinkData(context: Context) {
            val sharedPref = context.getSharedPreferences("deep_link_prefs", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            Log.d("MainActivity", "Deep link data cleared from SharedPreferences")
        }
    }

    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var tokenRepository: TokenRepository
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    private lateinit var notificationManager: InAppNotificationManager
    private var isAppInForeground = false
    private var deepLinkTrigger = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle notification intent
        handleNotificationIntent(intent)
        
        // Handle deep link intent
        handleDeepLinkIntent(intent)
        
        // Check for any stored FCM token from messaging service
        checkStoredToken()
        
        // Register for FCM token
        tokenRepository.FirebaseTokenRegisteration()
        
        // Initialize notification manager
        notificationManager = InAppNotificationManager()
        
        // Set up lifecycle observer to track app foreground state
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onEnterForeground() {
                isAppInForeground = true
                Log.d("MainActivity", "App entered foreground")
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onEnterBackground() {
                isAppInForeground = false
                Log.d("MainActivity", "App entered background")
            }
        })
        
        setContent {
            SwadRatna_StaffTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            startDestination = if (authRepository.isLoggedIn()) {
                                NavigationRoute.Tables.route
                            } else {
                                NavigationRoute.Login.route
                            }
                        }
                    }

                    startDestination?.let { destination ->
                        InAppNotificationProvider(notificationManager = notificationManager) {
                            NavigationComponent(
                                startDestination = destination,
                                deepLinkTrigger = deepLinkTrigger.value
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun checkStoredToken() {
        // Check if there's a stored FCM token from the messaging service
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val storedToken = sharedPref.getString("fcm_token", null)
        
        if (storedToken != null) {
            Log.d("MainActivity", "Found stored FCM token, sending to server")
            lifecycleScope.launch {
                try {
                    tokenRepository.sendTokenToServer(storedToken)
                    // Clear the stored token after successful send
                    sharedPref.edit().remove("fcm_token").apply()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to send stored token", e)
                }
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val notificationType = it.getStringExtra("notification_type")
            val orderId = it.getIntExtra("order_id", -1)
            val billId = it.getStringExtra("bill_id")
            val tableNumber = it.getIntExtra("table_number", -1)
            val inAppNotification = it.getBooleanExtra("in_app_notification", false)
            val notificationTitle = it.getStringExtra("notification_title")
            val notificationBody = it.getStringExtra("notification_body")
            val deepLink = it.getStringExtra("deeplink")
            
            if (notificationType != null) {
                Log.d("MainActivity", "Notification received - Type: $notificationType, OrderId: $orderId, BillId: $billId, TableNumber: $tableNumber, InApp: $inAppNotification, DeepLink: $deepLink")
                
                // If this is specifically an in-app notification from Firebase service
                if (inAppNotification) {
                    showInAppNotificationWithTitle(notificationType, orderId, billId, tableNumber, notificationTitle, notificationBody, deepLink)
                } else {
                    // It's a system notification click (or deep link)
                    // Always navigate directly, even if app is in foreground
                    handleDeepLinkNavigation(notificationType, orderId, billId, tableNumber)
                }
            }
        }
    }
    
    private fun isAppInForeground(): Boolean {
        return this.isAppInForeground
    }

    private fun showInAppNotification(type: String, orderId: Int, billId: String?, tableNumber: Int, deepLink: String? = null) {
        val notification = com.swadratna.swadratna_staff.ui.components.InAppNotification(
            id = System.currentTimeMillis().toString(),
            title = when (type) {
                "new_order" -> "New Order"
                "payment_completed" -> "Payment Completed"
                "order_ready" -> "Order Ready"
                else -> "Notification"
            },
            message = when (type) {
                "new_order" -> "New order received for table ${if (tableNumber != -1) tableNumber else "N/A"}"
                "payment_completed" -> "Payment completed for table ${if (tableNumber != -1) tableNumber else "N/A"}"
                "order_ready" -> "Order ${if (orderId != -1) "#$orderId" else ""} is ready"
                else -> "You have a new notification"
            },
            type = type,
            orderId = if (orderId != -1) orderId else null,
            billId = billId,
            tableNumber = if (tableNumber != -1) tableNumber else null,
            deepLink = deepLink
        )
        
        notificationManager.showNotification(notification)
        Log.d("MainActivity", "In-app notification shown: ${notification.title}")
    }
    
    private fun showInAppNotificationWithTitle(type: String, orderId: Int, billId: String?, tableNumber: Int, title: String?, body: String?, deepLink: String? = null) {
        val notification = com.swadratna.swadratna_staff.ui.components.InAppNotification(
            id = System.currentTimeMillis().toString(),
            title = title ?: when (type) {
                "new_order" -> "New Order"
                "payment_completed" -> "Payment Completed"
                "order_ready" -> "Order Ready"
                else -> "Notification"
            },
            message = body ?: when (type) {
                "new_order" -> "New order received for table ${if (tableNumber != -1) tableNumber else "N/A"}"
                "payment_completed" -> "Payment completed for table ${if (tableNumber != -1) tableNumber else "N/A"}"
                "order_ready" -> "Order ${if (orderId != -1) "#$orderId" else ""} is ready"
                else -> "You have a new notification"
            },
            type = type,
            orderId = if (orderId != -1) orderId else null,
            billId = billId,
            tableNumber = if (tableNumber != -1) tableNumber else null,
            deepLink = deepLink
        )
        
        notificationManager.showNotification(notification)
        Log.d("MainActivity", "In-app notification shown with custom title: ${notification.title}")
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        intent?.let { intent ->
            val data = intent.data
            if (data != null) {
                Log.d("MainActivity", "Deep link received: $data")
                
                // Handle swadratna://order?tableNumber=1&orderId=123
                if (data.scheme == "swadratna" && data.host == "order") {
                    val tableNumber = data.getQueryParameter("tableNumber")?.toIntOrNull() ?: -1
                    val orderId = data.getQueryParameter("orderId")?.toIntOrNull() ?: -1
                    
                    if (tableNumber != -1 && orderId != -1) {
                        handleDeepLinkNavigation("deep_link_order", orderId, null, tableNumber)
                    }
                }
                
                // Handle https://swadratna.com/order?tableNumber=1&orderId=123
                else if (data.scheme == "https" && data.host == "swadratna.com" && data.path?.startsWith("/order") == true) {
                    val tableNumber = data.getQueryParameter("tableNumber")?.toIntOrNull() ?: -1
                    val orderId = data.getQueryParameter("orderId")?.toIntOrNull() ?: -1
                    
                    if (tableNumber != -1 && orderId != -1) {
                        handleDeepLinkNavigation("deep_link_order", orderId, null, tableNumber)
                    }
                }
            }
        }
    }

    private fun handleDeepLinkNavigation(type: String, orderId: Int, billId: String?, tableNumber: Int) {
        val sharedPref = getSharedPreferences("deep_link_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("deep_link_type", type)
            putInt("deep_link_order_id", orderId)
            putString("deep_link_bill_id", billId)
            putInt("deep_link_table_number", tableNumber)
            apply()
        }
        deepLinkTrigger.value += 1
    }
    

}

data class DeepLinkData(
    val type: String,
    val orderId: Int,
    val billId: String?,
    val tableNumber: Int
)

data class RegisterDeviceTokenRequest(
    val device_id: String,
    val fcm_token: String,
    val platform: String,
    val app_type: String,
    val device_info: String
)

data class RegisterDeviceTokenResponse(
    val success: Boolean,
    val message: String
)
