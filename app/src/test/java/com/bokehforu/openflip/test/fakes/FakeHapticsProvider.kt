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
