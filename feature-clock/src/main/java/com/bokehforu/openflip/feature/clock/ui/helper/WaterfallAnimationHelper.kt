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

package com.bokehforu.openflip.feature.clock.ui.helper

/**
 * Logic helper for the waterfall seconds animation.
 * Refined for mechanical precision with stronger center-point emphasis:
 * - Crossfade centered at 30% progress (matching haptic trigger)
 * - Tighter alpha curves for crisper visual handover
 * - Reduced overlap for flowing number effect
 */
object WaterfallAnimationHelper {
    const val PREVIEW_ALPHA = 0f

    fun getIncomingAlpha(progress: Float): Float {
        return when {
            progress < 0.10f -> 0.0f
            progress < 0.50f -> (progress - 0.10f) / 0.40f
            else -> 1.0f
        }
    }

    fun getOutgoingAlpha(progress: Float): Float {
        return when {
            progress < 0.10f -> 1.0f
            progress < 0.50f -> 1.0f - ((progress - 0.10f) / 0.40f)
            else -> 0.0f
        }
    }
}
