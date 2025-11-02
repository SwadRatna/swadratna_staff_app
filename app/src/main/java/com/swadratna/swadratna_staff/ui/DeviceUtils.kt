package com.swadratna.swadratna_staff.ui

import android.content.Context
import java.util.UUID

object DeviceUtils {
    // Key for storing the unique ID
    private const val PREFS_FILE = "device_prefs"
    private const val DEVICE_ID_KEY = "unique_device_id"

    // Context is required to access SharedPreferences
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    // This is the function you need to call
    fun getUniqueDeviceId(): String {
        val prefs = applicationContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        
        // 1. Try to load existing ID
        var deviceId = prefs.getString(DEVICE_ID_KEY, null)

        // 2. If no ID exists, generate a new one
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            
            // 3. Save the new ID persistently
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply()
        }

        return deviceId
    }
}