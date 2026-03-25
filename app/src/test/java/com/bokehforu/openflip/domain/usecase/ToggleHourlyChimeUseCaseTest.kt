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

package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.domain.gateway.HourlyChimeScheduler
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleHourlyChimeUseCaseTest {

    @Test
    fun `enable updates setting and schedules next chime`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val scheduler = FakeHourlyChimeScheduler(canSchedule = true)
        val useCase = ToggleHourlyChimeUseCase(repository, scheduler)

        val result = useCase.execute(true)

        assertTrue(result is Result.Success)
        assertTrue(repository.isHourlyChimeEnabled())
        assertTrue(scheduler.scheduleCalled)
    }

    @Test
    fun `disable updates setting without scheduling`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val scheduler = FakeHourlyChimeScheduler(canSchedule = true)
        val useCase = ToggleHourlyChimeUseCase(repository, scheduler)

        val result = useCase.execute(false)

        assertTrue(result is Result.Success)
        assertFalse(repository.isHourlyChimeEnabled())
        assertFalse(scheduler.scheduleCalled)
    }

    @Test
    fun `enable returns failure when scheduling throws`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val scheduler = FakeHourlyChimeScheduler(canSchedule = true, throwOnSchedule = true)
        val useCase = ToggleHourlyChimeUseCase(repository, scheduler)

        val result = useCase.execute(true)

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ToggleHourlyChimeError.Unexpected)
    }

    private class FakeHourlyChimeScheduler(
        private val canSchedule: Boolean,
        private val throwOnSchedule: Boolean = false
    ) : HourlyChimeScheduler {
        var scheduleCalled: Boolean = false

        override fun canScheduleExactAlarms(): Boolean = canSchedule

        override fun scheduleNextChime() {
            if (throwOnSchedule) {
                throw IllegalStateException("schedule failed")
            }
            scheduleCalled = true
        }
    }
}
