/*
 * Copyright (C) 2026 DevonStee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
