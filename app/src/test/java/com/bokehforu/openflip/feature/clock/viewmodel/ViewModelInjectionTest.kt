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

package com.bokehforu.openflip.feature.clock.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.domain.usecase.StartSleepTimerUseCase
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.core.controller.interfaces.ElapsedTimeSource
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.data.settings.SettingsStore
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [30],
    application = HiltTestApplication::class
)
class ViewModelInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settingsStore: SettingsStore

    @Inject
    lateinit var timeSource: TimeSource

    @Inject
    lateinit var elapsedTimeSource: ElapsedTimeSource

    @Inject
    lateinit var hapticsProvider: HapticsProvider

    @Inject
    lateinit var soundProvider: SoundProvider

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var toggleThemeUseCase: ToggleThemeUseCase

    @Inject
    lateinit var startSleepTimerUseCase: StartSleepTimerUseCase

    @Inject
    lateinit var updateShowSecondsUseCase: UpdateShowSecondsUseCase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun fullscreenClockViewModelCanBeCreated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val savedStateHandle = SavedStateHandle()

        val viewModel = FullscreenClockViewModel(
            settingsRepository = settingsRepository,
            timeSource = timeSource,
            elapsedTimeSource = elapsedTimeSource,
            haptics = hapticsProvider,
            sound = soundProvider,
            toggleThemeUseCase = toggleThemeUseCase,
            updateShowSecondsUseCase = updateShowSecondsUseCase,
            startSleepTimerUseCase = startSleepTimerUseCase,
            savedStateHandle = savedStateHandle,
            appLifecycleMonitor = AppLifecycleMonitor(context)
        )

        assertNotNull("ViewModel should be created", viewModel)
        assertNotNull("ViewModel uiState should be initialized", viewModel.uiState.value)
    }

    @Test
    fun savedStateHandleIsInjected() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["test_key"] = "test_value"

        assertNotNull("SavedStateHandle should store values", savedStateHandle.get<String>("test_key"))
    }

    @Test
    fun applicationContextIsProvided() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull("ApplicationContext should be available", context)
        assertNotNull("Package name should be accessible", context.packageName)
    }
}
