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

package com.bokehforu.openflip.feature.clock.ui.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bokehforu.openflip.core.util.resolveThemeColor
import com.bokehforu.openflip.core.R as CoreR

class ColorTransitionController(
    private var targetView: View?
) : DefaultLifecycleObserver {
    private var animator: ValueAnimator? = null
    private var isDestroyed = false
    private var _isTransitioning = false
    private var lastAnimatedColor: Int? = null

    val isTransitioning: Boolean
        get() = _isTransitioning

    fun startTransition(
        fromIsDark: Boolean,
        targetIsDark: Boolean,
        durationMs: Long = 300,
        colorAttr: Int = CoreR.attr.appBackgroundColor,
        onUpdate: ((Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onApplyTheme: () -> Unit
    ) {
        val inFlightColor = lastAnimatedColor
        animator?.cancel()

        val view = targetView ?: return
        val context = view.context ?: return
        val darkColor = context.resolveThemeColor(colorAttr, CoreR.style.Theme_OpenFlip_Dark)
        val lightColor = context.resolveThemeColor(colorAttr, CoreR.style.Theme_OpenFlip_Light)
        val currentColor = inFlightColor ?: if (fromIsDark) darkColor else lightColor
        val targetColor = if (targetIsDark) darkColor else lightColor

        _isTransitioning = true
        onApplyTheme()

        animator = ValueAnimator.ofArgb(currentColor, targetColor).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator(1.5f)
            addUpdateListener { animation ->
                val animatedColor = animation.animatedValue as Int
                lastAnimatedColor = animatedColor
                targetView?.setBackgroundColor(animatedColor)
                onUpdate?.invoke(animatedColor)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animator = null
                    lastAnimatedColor = null
                    _isTransitioning = false
                    val currentView = targetView
                    if (!isDestroyed && currentView != null && currentView.isAttachedToWindow) {
                        onComplete?.invoke()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    animator = null
                    _isTransitioning = false
                }
            })
            start()
        }
    }

    fun destroy() {
        isDestroyed = true
        _isTransitioning = false
        animator?.cancel()
        animator = null
        targetView = null
    }

    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
    }
}
