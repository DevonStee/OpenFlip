package com.bokehforu.openflip.domain.gateway

interface HourlyChimeTester {
    fun testChime(hour: Int, minute: Int)
}
