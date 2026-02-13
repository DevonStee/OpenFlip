package com.bokehforu.openflip.feature.chime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bokehforu.openflip.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChimeBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var hourlyChimeManager: HourlyChimeManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        if (settingsRepository.isHourlyChimeEnabled()) {
            hourlyChimeManager.scheduleNextChime()
        }
    }
}
