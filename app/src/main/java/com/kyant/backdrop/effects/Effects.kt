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
