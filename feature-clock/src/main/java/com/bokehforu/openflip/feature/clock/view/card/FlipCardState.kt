package com.bokehforu.openflip.feature.clock.view.card

/**
 * Immutable state representing what the card should display.
 * Used for animation and rendering.
 */
data class FlipCardState(
    /** The currently displayed value (visible when at rest) */
    val currentValue: String = "00",
    
    /** The value being flipped to (revealed during animation) */
    val nextValue: String = "00",
    
    /** 
     * Flip animation progress: 0.0 = at rest, 1.0 = flip complete.
     * This maps to 0° -> 180° in the old degree-based system.
     */
    val flipProgress: Float = 0f,
    
    /** Optional AM/PM indicator text */
    val amPmText: String? = null,
    
    /** True when performing a reverse (upward) flip animation */
    val isReversing: Boolean = false
) {
    companion object {
        /** Progress threshold below which we consider the card "at rest" */
        const val REST_THRESHOLD = 0.003f // ~0.5 degrees / 180
        
        /** Progress at which the flap crosses the midpoint (90 degrees) */
        const val MIDPOINT = 0.5f
    }
    
    /** True if the card is effectively at rest (not animating) */
    val isAtRest: Boolean get() = flipProgress < REST_THRESHOLD
    
    /** True if the animation is in the first half (before 90 degrees) */
    val isFirstHalf: Boolean get() = flipProgress < MIDPOINT
    
    /** Convert progress to degrees for 3D rotation calculations */
    val flipDegrees: Float get() = flipProgress * 180f
    
    /** 
     * Computed shadow intensity for the "revealed" top half (0.0 to 1.0).
     * Forward: shadow decreases as top flap rotates away
     * Reverse: shadow on bottom decreases as bottom flap rotates away
     */
    val topRevealedShadowIntensity: Float
        get() = if (isAtRest) 0f else if (isReversing) {
            // Reverse: top is revealed in second half
            if (isFirstHalf) 0f else (1f - flipProgress) / MIDPOINT
        } else {
            // Forward: top is revealed in first half
            if (isFirstHalf) 1f - (flipProgress / MIDPOINT) else 0f
        }
    
    /**
     * Computed shadow intensity for the bottom half.
     */
    val bottomRevealedShadowIntensity: Float
        get() = if (isReversing) {
            // Reverse: bottom has shadow in first half
            if (isFirstHalf) 1f - (flipProgress / MIDPOINT) else 0f
        } else {
            // Forward: bottom has shadow in second half
            if (flipProgress > MIDPOINT) (1f - flipProgress) / MIDPOINT else 0f
        }
    
    /**
     * Shadow intensity on the rotating flap itself (simulates self-shadowing).
     */
    val flapShadowIntensity: Float
        get() = if (isAtRest) 0f else if (isFirstHalf) flipProgress / MIDPOINT else (1f - flipProgress) / MIDPOINT
    
    /** Which value should be shown on the top static layer */
    val topLayerValue: String
        get() = if (isAtRest) {
            currentValue
        } else {
            if (isReversing) currentValue else nextValue
        }
    
    /** Which value should be shown on the bottom static layer */
    val bottomLayerValue: String
        get() = if (isReversing) nextValue else currentValue
    
    /** Which value should be shown on the rotating flap */
    val flapValue: String
        get() = if (isReversing) {
            // Reverse: bottom flap shows current, then next
            if (isFirstHalf) currentValue else nextValue
        } else {
            // Forward: top flap shows current, then next
            if (isFirstHalf) currentValue else nextValue
        }
    
    /** 
     * Is the flap currently representing the top half of the card?
     * Forward: flap starts as top half
     * Reverse: flap starts as bottom half
     */
    val isFlapTop: Boolean
        get() = if (isReversing) {
            // Reverse: starts as bottom, becomes top after midpoint
            !isFirstHalf
        } else {
            // Forward: starts as top, becomes bottom after midpoint
            isFirstHalf
        }
    
    /** Rotation angle for the flap in degrees (for Camera.rotateX) */
    val flapRotationDegrees: Float
        get() = if (isReversing) {
            // Reverse: flip upward (positive rotation)
            if (isFirstHalf) flipDegrees else -(180f - flipDegrees)
        } else {
            // Forward: flip downward (negative rotation)
            if (isFirstHalf) -flipDegrees else 180f - flipDegrees
        }
}
