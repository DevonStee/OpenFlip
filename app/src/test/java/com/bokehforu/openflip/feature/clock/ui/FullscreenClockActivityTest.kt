package com.bokehforu.openflip.feature.clock.ui

import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.feature.clock.manager.TimeProvider
import com.bokehforu.openflip.data.settings.AppSettingsManager
import com.bokehforu.openflip.feature.clock.ui.controller.LightToggleController
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
class FullscreenClockActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var appSettingsManager: AppSettingsManager

    @Inject
    lateinit var haptics: HapticsProvider

    @Inject
    lateinit var sound: SoundProvider

    @Inject
    lateinit var timeProvider: TimeProvider

    @Inject
    lateinit var lightToggleControllerFactory: LightToggleController.Factory

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appSettingsManagerIsInjected() {
        assertNotNull("appSettingsManager should be injected", appSettingsManager)
    }

    @Test
    fun hapticsProviderIsInjected() {
        assertNotNull("haptics should be injected", haptics)
    }

    @Test
    fun soundProviderIsInjected() {
        assertNotNull("sound should be injected", sound)
    }

    @Test
    fun timeProviderIsInjected() {
        assertNotNull("timeProvider should be injected", timeProvider)
    }

    @Test
    fun lightToggleControllerFactoryIsInjected() {
        assertNotNull("lightToggleControllerFactory should be injected", lightToggleControllerFactory)
    }
}
