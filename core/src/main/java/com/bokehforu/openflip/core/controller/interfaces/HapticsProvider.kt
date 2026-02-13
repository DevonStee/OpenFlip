package com.bokehforu.openflip.core.controller.interfaces

interface HapticsProvider {
    fun setHapticEnabled(enabled: Boolean)
    fun performClick()
    fun performLongPress()
    fun performToggle()
    fun performScale()
    fun performSecondsTick()
}
