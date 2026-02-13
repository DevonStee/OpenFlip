package com.bokehforu.openflip.feature.chime.data

import com.bokehforu.openflip.domain.gateway.HourlyChimeScheduler
import com.bokehforu.openflip.feature.chime.HourlyChimeManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HourlyChimeSchedulerImpl @Inject constructor(
    private val hourlyChimeManager: HourlyChimeManager
) : HourlyChimeScheduler {
    override fun canScheduleExactAlarms(): Boolean = hourlyChimeManager.canScheduleExactAlarms()

    override fun scheduleNextChime() {
        hourlyChimeManager.scheduleNextChime()
    }
}
