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

import com.bokehforu.openflip.domain.result.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StartSleepTimerUseCaseTest {

    @Test
    fun `execute returns timer spec based on minutes`() {
        val useCase = StartSleepTimerUseCase()

        val result = useCase.execute(minutes = 5, nowMillis = 1_000L)
        assertTrue(result is Result.Success)
        val spec = (result as Result.Success).value

        assertEquals(300L, spec.durationSeconds)
        assertEquals(5, spec.originalDurationMinutes)
        assertEquals(301_000L, spec.endTimeMillis)
    }

    @Test
    fun `execute returns failure for non-positive duration`() {
        val useCase = StartSleepTimerUseCase()

        val result = useCase.execute(minutes = 0, nowMillis = 1_000L)

        assertTrue(result is Result.Failure)
        val failure = result as Result.Failure
        assertTrue(failure.error is StartSleepTimerError.InvalidDuration)
    }

    @Test
    fun `execute returns failure when duration exceeds max`() {
        val useCase = StartSleepTimerUseCase()

        val result = useCase.execute(minutes = 301, nowMillis = 1_000L)

        assertTrue(result is Result.Failure)
        val failure = result as Result.Failure
        assertTrue(failure.error is StartSleepTimerError.DurationTooLarge)
    }
}
