package com.swadratna.swadratna_staff

import android.app.Application
import com.swadratna.swadratna_staff.ui.DeviceUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SwadRatnaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DeviceUtils.initialize(this)
    }
}