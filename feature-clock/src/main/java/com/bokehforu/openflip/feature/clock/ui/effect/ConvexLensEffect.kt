package com.bokehforu.openflip.feature.clock.ui.effect

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Convex lens distortion effect for Compose UI elements.
 * 
 * Creates a spherical lens "bulge" effect - content at the center appears
 * magnified (closer), while edges are compressed (farther), simulating
 * an embedded glass dome or bubble window.
 * 
 * Unlike edge magnification, this effect is natural even when static,
 * as it mimics how real convex glass lenses distort light.
 * 
 * Requires Android 13+ (API 33) for RuntimeShader support.
 * 
 * @param lensStrength Intensity of the convex bulge (0.0-1.0, recommended: 0.15-0.3)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object ConvexLensEffect {
    
    /**
     * AGSL shader for convex lens distortion.
     * 
     * Applies radial distortion with maximum magnification at center,
     * smoothly fading to minimal distortion at edges.
     * 
     * Physics: Center appears "pushed forward" (magnified), edges appear
     * "pulled back" (compressed), creating a 3D dome illusion.
     */
    private const val SHADER_SOURCE = """
        uniform shader content;
        uniform float2 resolution;
        uniform float lensStrength;
        
        half4 main(float2 coord) {
            // Normalize coordinates to [0, 1]
            float2 uv = coord / resolution;
            float2 center = float2(0.5, 0.5);
            
            // Calculate distance from center (0 at center, 1 at corner)
            float2 toCenter = uv - center;
            float distFromCenter = length(toCenter);
            
            // Maximum possible distance (half diagonal of unit square)
            float maxDist = 0.7071; // sqrt(0.5^2 + 0.5^2)
            
            // Normalize distance [0, 1]
            float normalizedDist = min(distFromCenter / maxDist, 1.0);
            
            // Convex lens distortion factor
            // 1.0 at center (max magnification), 0.0 at edges (min distortion)
            // Applies smoothstep function for natural optical falloff
            float lensFactor = 1.0 - smoothstep(0.0, 1.0, normalizedDist);
            
            // Calculate scale factor for convex bulge
            // Center: scale < 1.0 (magnify = shrink UV space)
            // Edge: scale ≈ 1.0 (no distortion)
            float scale = 1.0 / (1.0 + (lensStrength * lensFactor));
            
            // Apply radial distortion from center
            float2 lensUV = center + (toCenter * scale);
            
            // Convert back to pixel coordinates
            float2 lensCoord = lensUV * resolution;
            
            // Sample the content shader at distorted position
            return content.eval(lensCoord);
        }
    """
    
    /**
     * Creates a RenderEffect with convex lens distortion applied.
     * 
     * @param widthPx Width of the target view in pixels
     * @param heightPx Height of the target view in pixels
     * @param lensStrength Convex bulge intensity (0.0-1.0, higher = more bulge)
     * @return RenderEffect to apply via Modifier.graphicsLayer { renderEffect = ... }
     */
    fun create(
        widthPx: Float,
        heightPx: Float,
        lensStrength: Float = 0.2f
    ): androidx.compose.ui.graphics.RenderEffect {
        val shader = RuntimeShader(SHADER_SOURCE)
        
        shader.setFloatUniform("resolution", widthPx, heightPx)
        shader.setFloatUniform("lensStrength", lensStrength.coerceIn(0f, 1f))
        
        return RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
    
    /**
     * Check if convex lens effect is supported on current device.
     * Requires Android 13+ (API 33).
     */
    fun isSupported(): Boolean {
        return true
    }
}
