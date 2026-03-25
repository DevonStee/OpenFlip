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

import com.bokehforu.openflip.core.controller.interfaces.SoundProvider

class FakeSoundProvider : SoundProvider {
    var playToggleSoundCalled = false
    var playClickSoundCalled = false
    var playFlipSoundCalled = false
    var playChimeSoundCalled = false
    
    override fun setSoundEnabled(enabled: Boolean) {}
    
    override fun playFlipSound() {
        playFlipSoundCalled = true
    }
    
    override fun playClickSound() {
        playClickSoundCalled = true
    }
    
    override fun playToggleSound() {
        playToggleSoundCalled = true
    }

    override fun playChimeSound(count: Int) {
        playChimeSoundCalled = true
    }

    override fun getEstimatedChimePlaybackDurationMs(count: Int): Long {
        return 0L
    }
    
    fun reset() {
        playToggleSoundCalled = false
        playClickSoundCalled = false
        playFlipSoundCalled = false
        playChimeSoundCalled = false
    }
}
