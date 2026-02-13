package com.bokehforu.openflip.feature.settings.controller

import com.bokehforu.openflip.domain.usecase.ToggleHourlyChimeUseCase
import com.bokehforu.openflip.domain.gateway.HourlyChimeTester
import com.bokehforu.openflip.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton

typealias HourlyChimeToggleResult = Result<Unit>

@Singleton
class HourlyChimeSettingsController @Inject constructor(
    private val toggleHourlyChimeUseCase: ToggleHourlyChimeUseCase,
    private val hourlyChimeTester: HourlyChimeTester
) {

    fun handleToggle(enabled: Boolean): HourlyChimeToggleResult {
        return toggleHourlyChimeUseCase.execute(enabled)
    }

    fun testChime(hour: Int, minute: Int) {
        hourlyChimeTester.testChime(hour, minute)
    }
}
