package com.bokehforu.openflip.domain.gateway

interface HourlyChimeScheduler {
    fun canScheduleExactAlarms(): Boolean
    fun scheduleNextChime()
}
