package com.bokehforu.openflip.feature.chime

import java.util.Calendar

object ChimeScheduleUtils {

    fun resolveChimeCountForTime(timeMillis: Long): Int {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
        return resolveChimeCountForCalendar(calendar)
    }

    fun resolveChimeCountForCalendar(calendar: Calendar): Int {
        return if (calendar.get(Calendar.MINUTE) == 0) {
            val hour12 = calendar.get(Calendar.HOUR)
            if (hour12 == 0) 12 else hour12
        } else {
            1
        }
    }
}
