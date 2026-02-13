package com.bokehforu.openflip.core.manager

data class Time(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val is24Hour: Boolean
) {
    val hourFormatted: String
        get() = if (is24Hour) {
            hour.toString().padStart(2, '0')
        } else {
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            hour12.toString().padStart(2, '0')
        }
    
    val minuteFormatted: String
        get() = minute.toString().padStart(2, '0')
    
    val secondFormatted: String
        get() = second.toString().padStart(2, '0')
    
    val amPm: String
        get() = if (hour < 12) "AM" else "PM"
}
