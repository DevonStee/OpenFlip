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

package com.kyant.backdrop.effects

/**
 * Effect scope used by drawBackdrop; functions are no-ops in this simplified version.
 */
class BackdropEffectScope {
    internal var blurRadius: Float? = null
    internal var vibrancyRequested: Boolean = false

    // Instance methods kept for compatibility; extension versions below call these.
    fun vibrancy() {
        vibrancyRequested = true
    }

    fun blur(radius: Float) {
        blurRadius = radius
    }
}

// Top-level extension functions to mirror original API
fun BackdropEffectScope.vibrancy() = this.vibrancy()
fun BackdropEffectScope.blur(radius: Float) = this.blur(radius)
