package com.bokehforu.openflip.feature.clock.view.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.feature.clock.view.card.FlipCardComponent
import com.bokehforu.openflip.core.R as CoreR

import android.content.Context
import com.bokehforu.openflip.feature.clock.R

/**
 * Manages flip animations for the clock cards.
 * Handles entrance sequences, regular minute/hour flips, and time travel (reverse) flips.
 */
class FlipAnimationManager(
    private val context: Context,
    private val hourCard: FlipCardComponent,
    private val minuteCard: FlipCardComponent,
    private val onInvalidate: () -> Unit
) {

    // Animation Timings (Loaded from Resources)
    private val flipDuration = context.resources.getInteger(CoreR.integer.flip_clock_flip_duration_ms).toLong()
    private val flipFallDuration = context.resources.getInteger(CoreR.integer.flip_clock_fall_duration_ms).toLong()
    private val flipBounceDuration = context.resources.getInteger(CoreR.integer.flip_clock_bounce_duration_ms).toLong()
    private val entranceDelay = context.resources.getInteger(CoreR.integer.entrance_delay_ms).toLong()
    private val entranceHourDuration = context.resources.getInteger(CoreR.integer.entrance_hour_duration_ms).toLong()
    private val entranceMinuteDuration = context.resources.getInteger(CoreR.integer.entrance_minute_duration_ms).toLong()
    private val entranceMinuteStartDelay = context.resources.getInteger(CoreR.integer.entrance_minute_start_delay_ms).toLong()

    // Dependencies (set later via setters or init)
    var soundManager: SoundProvider? = null
    var hapticManager: HapticsProvider? = null
    var showFlaps = true
    var isHourlyChimeEnabled = false

    // State
    private var isHourFlipping = false
    private var isMinuteFlipping = false
    private var currentHourAnimator: ValueAnimator? = null
    private var currentMinuteAnimator: Animator? = null
    
    // Entrance state
    private var entranceRunnable: Runnable? = null
    private val entranceAnimators = mutableListOf<android.animation.AnimatorSet>()
    
    // View interaction needed for postDelayed
    var viewPoster: ((Runnable, Long) -> Unit)? = null
    var viewRemover: ((Runnable) -> Unit)? = null


    fun cancelAll() {
        currentHourAnimator?.cancel()
        currentMinuteAnimator?.cancel()
        
        entranceRunnable?.let { viewRemover?.invoke(it) }
        entranceRunnable = null
        entranceAnimators.forEach { it.cancel() }
        entranceAnimators.clear()
        
        // Reset flags
        isHourFlipping = false
        isMinuteFlipping = false
        hourCard.flipDegree = 0f
        minuteCard.flipDegree = 0f
    }

    fun playEntranceAnimation(isAttachedToWindow: Boolean) {
        entranceRunnable?.let { viewRemover?.invoke(it) }

        entranceRunnable = Runnable {
            if (!isAttachedToWindow) return@Runnable
            playDecorativeFlip(hourCard, entranceHourDuration, 0L, playSound = true)
            playDecorativeFlip(minuteCard, entranceMinuteDuration, entranceMinuteStartDelay, playSound = false)
        }
        entranceRunnable?.let { viewPoster?.invoke(it, entranceDelay) }
    }

    private fun playDecorativeFlip(card: FlipCardComponent, duration: Long, startDelay: Long, playSound: Boolean = true) {
        card.nextValue = card.currentValue

        if (playSound) {
            soundManager?.playFlipSound()
        }

        val fallAnimator = createFallAnimator(card, duration, startDelay)
        val bounceAnimator = createBounceAnimator(card)

        val animatorSet = android.animation.AnimatorSet().apply {
            playSequentially(fallAnimator, bounceAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    entranceAnimators.remove(this@apply)
                }
            })
        }
        entranceAnimators.add(animatorSet)
        animatorSet.start()
    }

    private fun createFallAnimator(card: FlipCardComponent, duration: Long, startDelay: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 180f).apply {
            this.duration = duration
            this.startDelay = startDelay
            interpolator = android.view.animation.AccelerateInterpolator(1.5f)
            addUpdateListener {
                card.flipDegree = it.animatedValue as Float
                onInvalidate()
            }
        }
    }

    private fun createBounceAnimator(card: FlipCardComponent): ValueAnimator {
        return ValueAnimator.ofFloat(180f, 172f, 180f).apply {
            duration = 120
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener {
                card.flipDegree = it.animatedValue as Float
                onInvalidate()
            }
            addListener(createBounceListener(card))
        }
    }

     private fun createBounceListener(card: FlipCardComponent): AnimatorListenerAdapter {
         return object : AnimatorListenerAdapter() {
             override fun onAnimationStart(animation: Animator) {
                 if (showFlaps) hapticManager?.performToggle()
             }

             override fun onAnimationEnd(animation: Animator) {
                 resetCardFlip(card)
             }

             override fun onAnimationCancel(animation: Animator) {
                 resetCardFlip(card)
             }
         }
     }

    private fun resetCardFlip(card: FlipCardComponent) {
        card.flipDegree = 0f
        onInvalidate()
    }

    fun flipHour(newHour: String, isReverse: Boolean = false, onAnimationEnd: (() -> Unit)? = null) {
        currentHourAnimator?.cancel()

        hourCard.isReverseFlip = isReverse
        hourCard.nextValue = newHour
        isHourFlipping = true
        soundManager?.playFlipSound()

        currentHourAnimator = createFlipAnimator(
            card = hourCard,
            newValue = newHour,
            duration = flipDuration,
            onComplete = { 
                isHourFlipping = false
                onAnimationEnd?.invoke()
            }
        )
        currentHourAnimator?.start()
    }

    fun flipMinute(newMinute: String, isReverse: Boolean = false, onAnimationEnd: (() -> Unit)? = null) {
        currentMinuteAnimator?.cancel()

        minuteCard.isReverseFlip = isReverse
        minuteCard.nextValue = newMinute
        isMinuteFlipping = true
        val isChimeMinute = newMinute == "00" || newMinute == "15" || newMinute == "30" || newMinute == "45"
        if (!isChimeMinute || !isHourlyChimeEnabled) {
            soundManager?.playFlipSound()
        }

        var hasTriggeredImpact = false

        val animator = ValueAnimator.ofFloat(0f, 180f + 8f).apply {
            duration = flipFallDuration + flipBounceDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val rawDegree = anim.animatedValue as Float
                val degree = calculateBounceAngle(rawDegree)
                minuteCard.flipDegree = degree
                onInvalidate()

                if (shouldTriggerBounceHaptic(rawDegree, hasTriggeredImpact)) {
                     hasTriggeredImpact = true
                     hapticManager?.performToggle()
                 }
            }
            addListener(createFlipCompleteListener(minuteCard, newMinute) { 
                isMinuteFlipping = false
                onAnimationEnd?.invoke()
            })
        }

        currentMinuteAnimator = animator
        animator.start()
    }

    private fun calculateBounceAngle(rawDegree: Float): Float {
        if (rawDegree <= 180f) return rawDegree

        val bounceProgress = (rawDegree - 180f) / 8f
        return if (bounceProgress < 0.5f) {
            180f - (16f * bounceProgress)
        } else {
            172f + (16f * (bounceProgress - 0.5f))
        }
    }

    private fun shouldTriggerBounceHaptic(rawDegree: Float, hasTriggered: Boolean): Boolean {
        return rawDegree >= 180f && !hasTriggered && showFlaps
    }

    private fun createFlipAnimator(
        card: FlipCardComponent,
        newValue: String,
        duration: Long,
        onComplete: () -> Unit
    ): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 180f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                card.flipDegree = it.animatedValue as Float
                onInvalidate()
            }
            addListener(createFlipCompleteListener(card, newValue, onComplete))
        }
    }

    private fun createFlipCompleteListener(
        card: FlipCardComponent,
        newValue: String,
        onComplete: () -> Unit
    ): AnimatorListenerAdapter {
        return object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                card.currentValue = newValue
                card.flipDegree = 0f
                card.isReverseFlip = false
                onComplete()
                onInvalidate()
            }

            override fun onAnimationCancel(animation: Animator) {
                card.flipDegree = 0f
                card.isReverseFlip = false
                onComplete()
            }
        }
    }
}
