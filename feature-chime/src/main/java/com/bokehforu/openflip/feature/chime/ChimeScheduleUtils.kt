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
