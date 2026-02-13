package com.bokehforu.openflip.domain.usecase

import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateTimeFormatUseCaseTest {

    @Test
    fun `execute updates time format mode`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = UpdateTimeFormatUseCase(repository)

        useCase.execute(1)

        assertEquals(1, repository.getTimeFormatMode())
    }
}
