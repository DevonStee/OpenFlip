package com.bokehforu.openflip

import android.app.Application
import com.bokehforu.openflip.feature.chime.HourlyChimeManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OpenFlipApplication : Application() {
    
    @Inject
    lateinit var hourlyChimeManager: HourlyChimeManager
}
