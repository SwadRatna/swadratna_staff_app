package com.swadratna.swadratna_staff

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.swadratna.swadratna_staff.data.remote.repositories.AuthRepository
import com.swadratna.swadratna_staff.data.remote.repositories.TokenRepository
import com.swadratna.swadratna_staff.navigation.NavigationComponent
import com.swadratna.swadratna_staff.navigation.NavigationRoute
import com.swadratna.swadratna_staff.ui.theme.SwadRatna_StaffTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        fun getDeepLinkData(context: Context): Triple<String?, Int, Int>? {
            val sharedPref = context.getSharedPreferences("deep_link_prefs", Context.MODE_PRIVATE)
            val deepLinkType = sharedPref.getString("deep_link_type", null)
            val deepLinkOrderId = sharedPref.getInt("deep_link_order_id", -1)
            val deepLinkTableNumber = sharedPref.getInt("deep_link_table_number", -1)
            
            Log.d("MainActivity", "getDeepLinkData - Type: $deepLinkType, OrderId: $deepLinkOrderId, TableNumber: $deepLinkTableNumber")
            
            return if (deepLinkType != null && deepLinkOrderId != -1 && deepLinkTableNumber != -1) {
                Triple(deepLinkType, deepLinkOrderId, deepLinkTableNumber)
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
                        NavigationComponent(startDestination = destination)
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
            val tableNumber = it.getIntExtra("table_number", -1)
            
            if (notificationType != null) {
                Log.d("MainActivity", "Notification received - Type: $notificationType, OrderId: $orderId, TableNumber: $tableNumber")
                // Handle notification navigation
                handleDeepLinkNavigation(notificationType, orderId, tableNumber)
            }
        }
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
                        handleDeepLinkNavigation("deep_link_order", orderId, tableNumber)
                    }
                }
                
                // Handle https://swadratna.com/order?tableNumber=1&orderId=123
                else if (data.scheme == "https" && data.host == "swadratna.com" && data.path?.startsWith("/order") == true) {
                    val tableNumber = data.getQueryParameter("tableNumber")?.toIntOrNull() ?: -1
                    val orderId = data.getQueryParameter("orderId")?.toIntOrNull() ?: -1
                    
                    if (tableNumber != -1 && orderId != -1) {
                        handleDeepLinkNavigation("deep_link_order", orderId, tableNumber)
                    }
                }
            }
        }
    }

    private fun handleDeepLinkNavigation(type: String, orderId: Int, tableNumber: Int) {
        val sharedPref = getSharedPreferences("deep_link_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("deep_link_type", type)
            putInt("deep_link_order_id", orderId)
            putInt("deep_link_table_number", tableNumber)
            apply()
        }
    }
}

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
