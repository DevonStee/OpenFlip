package com.bokehforu.openflip.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackSoundManager @Inject constructor(context: Context) : SoundProvider {
    private val appContext = context.applicationContext
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundPool: SoundPool
    private val flipSoundId: Int
    private val knobSoundId: Int
    private var isSoundEnabled = true
    private var loadedSounds = 0
    private var totalSoundsToLoad = 2
    private var audioFocusRequest: AudioFocusRequest? = null
    private var chimeMediaPlayer: MediaPlayer? = null
    private var lastChimeTime = 0L
    
    companion object {
        private const val FLIP_SOUND_DEBOUNCE_MS = 50L
        private const val CHIME_DURATION_PER_COUNT_MS = 1200L
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(15)
            .setAudioAttributes(audioAttributes)
            .build()

        flipSoundId = soundPool.load(appContext, R.raw.flip_sound, 1)
        knobSoundId = soundPool.load(appContext, R.raw.knob_sound, 1)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds++
            }
        }
    }

    override fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    override fun playClickSound() {
        playKnobSound()
    }

    override fun playToggleSound() {
        playFlipSound()
    }

    private var lastFlipTime = 0L

    override fun playFlipSound() {
        if (!isSoundEnabled || flipSoundId == 0) return

        val now = System.currentTimeMillis()
        if (now - lastFlipTime < FLIP_SOUND_DEBOUNCE_MS) {
            return
        }
        lastFlipTime = now

        // Restore flip sound to balanced default loudness.
        soundPool.play(flipSoundId, 0.15f, 0.15f, 10, 0, 1.0f)
    }

    fun playKnobSound() {
        if (isSoundEnabled && knobSoundId != 0) {
            // Keep knob turning subtle to avoid masking clock ambience.
            soundPool.play(knobSoundId, 0.015f, 0.015f, 1, 0, 1.0f)
        }
    }

    override fun playChimeSound(count: Int) {
        if (!isSoundEnabled) return

        val requiredCooldown = count * CHIME_DURATION_PER_COUNT_MS
        val now = System.currentTimeMillis()
        if (now - lastChimeTime < requiredCooldown) {
            return
        }
        lastChimeTime = now

        // Release any previous chime playback.
        chimeMediaPlayer?.release()
        chimeMediaPlayer = null

        requestAudioFocus()

        val resId = getChimeRawId(count)
        val mediaPlayer = MediaPlayer.create(appContext, resId)
        if (mediaPlayer == null) {
            abandonAudioFocus()
            return
        }

        chimeMediaPlayer = mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setVolume(1.0f, 1.0f)
            setOnErrorListener { mp, _, _ ->
                mp.release()
                if (chimeMediaPlayer === mp) {
                    chimeMediaPlayer = null
                }
                abandonAudioFocus()
                true
            }
            setOnCompletionListener { mp ->
                mp.release()
                if (chimeMediaPlayer === mp) {
                    chimeMediaPlayer = null
                }
                abandonAudioFocus()
            }
            start()
        }
    }

    fun getChimeRemainingCooldown(count: Int): Long {
        val requiredCooldown = count * CHIME_DURATION_PER_COUNT_MS
        val now = System.currentTimeMillis()
        val remaining = requiredCooldown - (now - lastChimeTime)
        return if (remaining > 0) remaining else 0
    }

    override fun getEstimatedChimePlaybackDurationMs(count: Int): Long {
        val normalized = count.coerceIn(1, 12)
        // Keep service alive slightly beyond expected strike cadence to avoid early teardown.
        return normalized * CHIME_DURATION_PER_COUNT_MS + 800L
    }

    private fun requestAudioFocus() {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setOnAudioFocusChangeListener { }
            .build()
        audioFocusRequest = focusRequest
        audioManager.requestAudioFocus(focusRequest)
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
    }

    fun release() {
        chimeMediaPlayer?.release()
        chimeMediaPlayer = null
        abandonAudioFocus()
        soundPool.release()
    }

    private fun getChimeRawId(count: Int): Int {
        val normalized = count.coerceIn(1, 12)
        return when (normalized) {
            1 -> R.raw.chime_01
            2 -> R.raw.chime_02
            3 -> R.raw.chime_03
            4 -> R.raw.chime_04
            5 -> R.raw.chime_05
            6 -> R.raw.chime_06
            7 -> R.raw.chime_07
            8 -> R.raw.chime_08
            9 -> R.raw.chime_09
            10 -> R.raw.chime_10
            11 -> R.raw.chime_11
            else -> R.raw.chime_12
        }
    }
}
