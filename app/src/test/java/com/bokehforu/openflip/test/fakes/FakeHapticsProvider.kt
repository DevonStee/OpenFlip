package com.bokehforu.openflip.test.fakes

import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider

class FakeHapticsProvider : HapticsProvider {
    var performToggleCalled = false
    var performClickCalled = false
    var performLongPressCalled = false
    var performScaleCalled = false
    
    override fun setHapticEnabled(enabled: Boolean) {}
    
    override fun performClick() {
        performClickCalled = true
    }
    
    override fun performLongPress() {
        performLongPressCalled = true
    }
    
    override fun performToggle() {
        performToggleCalled = true
    }
    
    override fun performScale() {
        performScaleCalled = true
    }

    override fun performSecondsTick() {}

    fun reset() {
        performToggleCalled = false
        performClickCalled = false
        performLongPressCalled = false
        performScaleCalled = false
    }
}
