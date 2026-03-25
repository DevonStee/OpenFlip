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

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ChimeScheduleUtilsTest {

    @Test
    fun `returns one strike for quarter minutes`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        listOf(15, 30, 45).forEach { minute ->
            calendar.set(Calendar.MINUTE, minute)
            assertEquals(1, ChimeScheduleUtils.resolveChimeCountForCalendar(calendar))
        }
    }

    @Test
    fun `returns matching hour strikes for top of hour in 24h clock`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val samples = mapOf(
            0 to 12,
            1 to 1,
            8 to 8,
            12 to 12,
            17 to 5,
            23 to 11
        )

        samples.forEach { (hourOfDay, expected) ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            assertEquals(expected, ChimeScheduleUtils.resolveChimeCountForCalendar(calendar))
        }
    }
}
