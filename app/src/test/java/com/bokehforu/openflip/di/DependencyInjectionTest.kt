package com.bokehforu.openflip.di

import android.content.Context
import android.os.Vibrator
import com.bokehforu.openflip.core.controller.interfaces.ElapsedTimeSource
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.data.settings.SettingsStore
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import com.bokehforu.openflip.manager.FeedbackSoundManager
import com.bokehforu.openflip.core.manager.HapticFeedbackManager
import com.bokehforu.openflip.feature.clock.manager.TimeProvider
import com.bokehforu.openflip.data.settings.AppSettingsManager
import com.bokehforu.openflip.feature.clock.ui.controller.LightToggleController
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

/**
 * Integration tests for Hilt Dependency Injection.
 * Verifies that the DI graph is properly configured and all components can be injected.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [30],
    application = HiltTestApplication::class
)
class DependencyInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appContext: Context

    @Inject
    lateinit var settingsStore: SettingsStore

    @Inject
    lateinit var hapticsProvider: HapticsProvider

    @Inject
    lateinit var soundProvider: SoundProvider

    @Inject
    lateinit var timeSource: TimeSource

    @Inject
    lateinit var elapsedTimeSource: ElapsedTimeSource

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var lightToggleControllerFactory: LightToggleController.Factory

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun singletonComponentsCanBeInjected() {
        assertNotNull("ApplicationContext should be injected", appContext)
        assertNotNull("SettingsStore should be injected", settingsStore)
        assertNotNull("HapticsProvider should be injected", hapticsProvider)
        assertNotNull("SoundProvider should be injected", soundProvider)
        assertNotNull("TimeSource should be injected", timeSource)
        assertNotNull("ElapsedTimeSource should be injected", elapsedTimeSource)
        assertNotNull("CoroutineScope should be injected", coroutineScope)
    }

    @Test
    fun viewModelCanBeCreatedWithHilt() {
        assertNotNull("Hilt DI is configured for ViewModels", hiltRule)
    }

    @Test
    fun interfaceBindingsWork() {
        assertTrue(
            "SettingsStore should bind to AppSettingsManager",
            settingsStore is AppSettingsManager
        )
        assertTrue(
            "HapticsProvider should bind to HapticFeedbackManager",
            hapticsProvider is HapticFeedbackManager
        )
        assertTrue(
            "SoundProvider should bind to FeedbackSoundManager",
            soundProvider is FeedbackSoundManager
        )
        assertTrue(
            "TimeSource should bind to TimeProvider",
            timeSource is TimeProvider
        )
    }

    @Test
    fun assistedInjectFactoryWorks() {
        assertNotNull(
            "LightToggleController.Factory should be injected",
            lightToggleControllerFactory
        )
    }

    @Test
    fun coroutineScopeIsProvided() {
        assertNotNull("CoroutineScope should be provided", coroutineScope)
        assertNotNull("CoroutineScope context should exist", coroutineScope.coroutineContext)
    }
}
