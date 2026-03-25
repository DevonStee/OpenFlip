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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleThemeUseCaseTest {

    @Test
    fun `toggle flips current theme`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = ToggleThemeUseCase(repository)
        val initial = repository.isDarkTheme()

        val next = useCase.toggle()

        assertEquals(!initial, next)
        assertEquals(next, repository.isDarkTheme())
    }

    @Test
    fun `set writes target theme`() {
        val repository = SettingsRepositoryImpl(FakeSettingsStore())
        val useCase = ToggleThemeUseCase(repository)

        useCase.set(true)

        assertTrue(repository.isDarkTheme())
    }
}
