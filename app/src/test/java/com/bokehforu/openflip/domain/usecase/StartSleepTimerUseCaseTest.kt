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
