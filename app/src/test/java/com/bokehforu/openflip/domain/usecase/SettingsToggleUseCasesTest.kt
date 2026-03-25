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
import com.bokehforu.openflip.test.fakes.FakeSettingsStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for settings toggle operations via SettingsRepository directly.
 * Proxy UseCases were removed in favor of direct repository calls.
 */
class SettingsToggleUseCasesTest {

    @Test
    fun `update show seconds use case updates repository`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = UpdateShowSecondsUseCase(repository)
        useCase.execute(true)
        assertTrue(repository.showSeconds())
    }

    @Test
    fun `repository setShowFlaps updates correctly`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        repository.setShowFlaps(false)
        assertFalse(repository.showFlaps())
    }

    @Test
    fun `repository set haptic and sound updates correctly`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        repository.setHapticEnabled(false)
        repository.setSoundEnabled(false)
        assertFalse(repository.isHapticEnabled())
        assertFalse(repository.isSoundEnabled())
    }
}
