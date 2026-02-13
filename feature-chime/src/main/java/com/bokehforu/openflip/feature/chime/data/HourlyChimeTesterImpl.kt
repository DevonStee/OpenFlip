package com.bokehforu.openflip.feature.chime.data

import com.bokehforu.openflip.domain.gateway.HourlyChimeTester
import com.bokehforu.openflip.feature.chime.HourlyChimeManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HourlyChimeTesterImpl @Inject constructor(
    private val hourlyChimeManager: HourlyChimeManager
) : HourlyChimeTester {
    override fun testChime(hour: Int, minute: Int) {
        hourlyChimeManager.testChime(hour, minute)
    }
}
