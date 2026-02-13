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
