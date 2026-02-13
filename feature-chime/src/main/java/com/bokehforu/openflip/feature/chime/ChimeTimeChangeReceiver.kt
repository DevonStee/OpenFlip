package com.bokehforu.openflip.feature.chime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bokehforu.openflip.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChimeTimeChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var hourlyChimeManager: HourlyChimeManager

    override fun onReceive(context: Context, intent: Intent) {
        if (!settingsRepository.isHourlyChimeEnabled()) return
        Log.d("ChimeTimeChange", "Time change received: ${intent.action}, rescheduling")
        hourlyChimeManager.scheduleNextChime()
    }
}
