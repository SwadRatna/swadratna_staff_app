package com.swadratna.swadratna_staff.data.remote.repositories

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.swadratna.swadratna_staff.data.remote.services.ApiService
import retrofit2.HttpException
import android.util.Log // Don't forget this import
import com.google.firebase.messaging.FirebaseMessaging
import com.swadratna.swadratna_staff.RegisterDeviceTokenRequest
import com.swadratna.swadratna_staff.ui.DeviceUtils.getUniqueDeviceId
import javax.inject.Inject
import javax.inject.Named
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

class TokenRepository @Inject constructor(
    @Named("authenticated") private val apiService: ApiService,
    private val networkMonitor: NetworkMonitor
) {

    fun FirebaseTokenRegisteration() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val fcmToken = task.result
            
            sendTokenToServer(fcmToken)
        }
    }

    fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!networkMonitor.isOnline.value) {
                    Log.w("FCM_TAG", "Skipping token registration: offline")
                    return@launch
                }
                val request = RegisterDeviceTokenRequest(
                    device_id = getUniqueDeviceId(),
                    fcm_token = token,
                    platform = "android",
                    app_type = "staff",
                    device_info = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}"
                )

                val response = apiService.registerDeviceToken(request)

                if (response.isSuccessful) {
                    Log.i("FCM_TAG", "Token registered successfully on server.")
                } else {
                    Log.e("FCM_TAG", "Failed to register token. Code: ${response.code()}")
                }

            } catch (e: HttpException) {
                Log.e("FCM_TAG", "HTTP Error during token registration: ${e.message()}")
            } catch (e: Exception) {
                Log.e("FCM_TAG", "Network/Other Error during token registration: ${e.message}")
            }
        }
    }
}
