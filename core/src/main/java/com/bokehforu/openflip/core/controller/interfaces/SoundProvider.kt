package com.bokehforu.openflip.core.controller.interfaces

interface SoundProvider {
    fun setSoundEnabled(enabled: Boolean)
    fun playFlipSound()
    fun playClickSound()
    fun playToggleSound()
    fun playChimeSound(count: Int)
    fun getEstimatedChimePlaybackDurationMs(count: Int): Long
}
