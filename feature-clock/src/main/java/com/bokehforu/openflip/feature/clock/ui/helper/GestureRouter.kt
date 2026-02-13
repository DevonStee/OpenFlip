package com.bokehforu.openflip.feature.clock.ui.helper

import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.os.SystemClock
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel

class GestureRouter(
    context: Context,
    private val viewModel: FullscreenClockViewModel,
    private val flipClockView: FullscreenFlipClockView,
    private val window: Window,
    private val screenHeight: Float,
    private val onToggleInteraction: () -> Unit,
    private val brightnessDefault: Float,
    private val brightnessMin: Float,
    private val brightnessMax: Float,
    private val swipeHintView: ImageView?,
    private val onHapticBoundary: () -> Unit
) {
    companion object {
        private const val SWIPE_HINT_ENTER_DURATION_MS = 70L
        private const val SWIPE_HINT_ENTER_SCALE_START = 0.94f
        private const val SWIPE_HINT_RECOVER_DURATION_MS = 70L
        private const val SWIPE_HINT_SWITCH_OUT_DURATION_MS = 70L
        private const val SWIPE_HINT_SWITCH_IN_DURATION_MS = 70L
        private const val SWIPE_HINT_SWITCH_DIM_ALPHA = 0.58f
        private const val SWIPE_HINT_SWITCH_SCALE_START = 0.96f
        private const val SWIPE_HINT_SWITCH_MIN_INTERVAL_MS = 70L
        private const val SWIPE_HINT_HIDE_DURATION_MS = 220L
    }

    private var currentScreenHeight: Float = screenHeight
    private var lastSwipeIconRes: Int = 0
    private var lastIconSwitchTime: Long = 0L
    private var lastBoundaryHapticTime: Long = 0L
    private var isDimBlockedByMultiTouch: Boolean = false
    private var swipeHintIsFadingOut: Boolean = false
    private val swipeHintEnterInterpolator = DecelerateInterpolator(1.5f)
    private val swipeHintHideInterpolator = AccelerateDecelerateInterpolator()

    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
             if (viewModel.uiState.value.isScaleEnabled) {
                val scaleFactor = detector.scaleFactor
                flipClockView.applyScale(scaleFactor)
                return true
            }
            return false
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onToggleInteraction()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            // Only allow brightness scroll with single finger; ignore when multi-touch (pinch)
            if (e2.pointerCount > 1) {
                if (!isDimBlockedByMultiTouch) {
                    isDimBlockedByMultiTouch = true
                    hideSwipeHint(immediate = true)
                }
                return false
            }

            if (isDimBlockedByMultiTouch) return false

            if (viewModel.uiState.value.swipeToDimEnabled) {
                 val layoutParams = window.attributes
                 var currentBrightness = layoutParams.screenBrightness
                 if (currentBrightness < 0) {
                     currentBrightness = viewModel.uiState.value.brightnessOverride
                 }
                 if (currentBrightness < 0) {
                     currentBrightness = brightnessDefault
                 }

                 val delta = distanceY / currentScreenHeight
                 val previousBrightness = currentBrightness
                 currentBrightness = (currentBrightness + delta).coerceIn(brightnessMin, brightnessMax)
                 layoutParams.screenBrightness = currentBrightness
                 window.attributes = layoutParams

                 // Persist so it survives rotation
                 viewModel.onBrightnessChange(currentBrightness)

                val directionRes = when {
                    distanceY > 0 -> R.drawable.icon_swipe_light_max // swipe up → brighter
                    distanceY < 0 -> R.drawable.icon_swipe_light_min // swipe down → dimmer
                    else -> lastSwipeIconRes.takeIf { it != 0 } ?: R.drawable.icon_swipe_light_max
                }

                val hintRes = when {
                    currentBrightness <= brightnessMin + 0.001f -> R.drawable.icon_swipe_light_min
                    currentBrightness >= brightnessMax - 0.001f -> R.drawable.icon_swipe_light_max
                    else -> directionRes
                }
                showSwipeHint(hintRes)

                // Haptic on boundary reach (including repeated pushes against boundary with cooldown)
                val now = SystemClock.elapsedRealtime()
                val cooldownPassed = now - lastBoundaryHapticTime > 250
                val hitMin = currentBrightness == brightnessMin && (previousBrightness != brightnessMin || delta < 0f)
                val hitMax = currentBrightness == brightnessMax && (previousBrightness != brightnessMax || delta > 0f)
                if (cooldownPassed && (hitMin || hitMax)) {
                    lastBoundaryHapticTime = now
                    onHapticBoundary()
                }
                return true
            }
            return false
        }
    }).apply {
        setIsLongpressEnabled(false)
    }

    fun updateDimensions(height: Float) {
        currentScreenHeight = height
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        val handled = gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isDimBlockedByMultiTouch = false
                // Allow immediate haptic on a fresh gesture
                lastBoundaryHapticTime = 0L
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Multi-touch session takes precedence: disable dim for this whole gesture.
                isDimBlockedByMultiTouch = true
                hideSwipeHint(immediate = true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                hideSwipeHint(immediate = false)
                isDimBlockedByMultiTouch = false
                // Reset cooldown so next gesture can get instant boundary feedback
                lastBoundaryHapticTime = 0L
            }
        }
        return handled
    }

    private fun showSwipeHint(res: Int) {
        if (isDimBlockedByMultiTouch) return
        swipeHintView?.let { view ->
            view.animate().cancel()

            val hidden = view.visibility != View.VISIBLE
            val needsRecover = swipeHintIsFadingOut || view.alpha < 0.98f

            swipeHintIsFadingOut = false

            if (hidden) {
                lastSwipeIconRes = res
                lastIconSwitchTime = SystemClock.elapsedRealtime()
                view.setImageResource(res)
                view.alpha = 0f
                view.scaleX = SWIPE_HINT_ENTER_SCALE_START
                view.scaleY = SWIPE_HINT_ENTER_SCALE_START
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SWIPE_HINT_ENTER_DURATION_MS)
                    .setInterpolator(swipeHintEnterInterpolator)
                    .start()
                return@let
            }

            if (needsRecover) {
                if (res != lastSwipeIconRes) {
                    lastSwipeIconRes = res
                    lastIconSwitchTime = SystemClock.elapsedRealtime()
                    view.setImageResource(res)
                }
                view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(SWIPE_HINT_RECOVER_DURATION_MS)
                    .setInterpolator(swipeHintEnterInterpolator)
                    .start()
                return@let
            }

            if (res == lastSwipeIconRes) return@let

            val now = SystemClock.elapsedRealtime()
            if (now - lastIconSwitchTime < SWIPE_HINT_SWITCH_MIN_INTERVAL_MS) return@let

            lastSwipeIconRes = res
            lastIconSwitchTime = now

            if (view.visibility != View.VISIBLE) {
                return@let
            }
            view.animate()
                .alpha(SWIPE_HINT_SWITCH_DIM_ALPHA)
                .setDuration(SWIPE_HINT_SWITCH_OUT_DURATION_MS)
                .setInterpolator(swipeHintEnterInterpolator)
                .withEndAction {
                    if (!view.isAttachedToWindow || view.visibility != View.VISIBLE) return@withEndAction
                    view.setImageResource(res)
                    view.scaleX = SWIPE_HINT_SWITCH_SCALE_START
                    view.scaleY = SWIPE_HINT_SWITCH_SCALE_START
                    view.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(SWIPE_HINT_SWITCH_IN_DURATION_MS)
                        .setInterpolator(swipeHintEnterInterpolator)
                        .start()
                }
                .start()
        }
    }

    private fun hideSwipeHint(immediate: Boolean) {
        swipeHintView?.let { view ->
            view.animate().cancel()
            swipeHintIsFadingOut = false
            if (immediate) {
                view.alpha = 0f
                view.scaleX = 1f
                view.scaleY = 1f
                view.visibility = View.GONE
                return@let
            }

            if (view.visibility != View.VISIBLE) return@let

            swipeHintIsFadingOut = true
            view.animate()
                .alpha(0f)
                .setDuration(SWIPE_HINT_HIDE_DURATION_MS)
                .setInterpolator(swipeHintHideInterpolator)
                .withEndAction {
                    if (!view.isAttachedToWindow) return@withEndAction
                    swipeHintIsFadingOut = false
                    view.visibility = View.GONE
                    view.scaleX = 1f
                    view.scaleY = 1f
                }
                .start()
        }
    }
}
