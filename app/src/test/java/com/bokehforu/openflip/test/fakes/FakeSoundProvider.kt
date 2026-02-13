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
