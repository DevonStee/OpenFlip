package com.bokehforu.openflip.feature.clock.ui.effect

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Glass lens magnification effect for Compose UI elements.
 * 
 * Creates a distortion effect at the edges of the target region,
 * simulating a glass lens magnification as content passes through boundaries.
 * 
 * Requires Android 13+ (API 33) for RuntimeShader support.
 * 
 * @param magnificationStrength Intensity of the magnification (0.0-1.0, recommended: 0.3-0.5)
 * @param edgeZonePx Width of the edge zone in pixels where magnification is applied (recommended: 30-50)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object GlassMagnificationEffect {
    
    /**
     * AGSL shader for edge magnification effect.
     * 
     * Applies radial distortion within an edge zone, creating a lens-like
     * magnification effect as pixels approach the boundary.
     */
    private const val SHADER_SOURCE = """
        uniform shader content;
        uniform float2 resolution;
        uniform float magnification;
        uniform float edgeZone;
        
        half4 main(float2 coord) {
            // Normalize coordinates to [0, 1]
            float2 uv = coord / resolution;
            float2 center = float2(0.5, 0.5);
            
            // Calculate distance from edges (0 at edge, 1 at center)
            float distFromLeft = uv.x;
            float distFromRight = 1.0 - uv.x;
            float distFromTop = uv.y;
            float distFromBottom = 1.0 - uv.y;
            
            // Minimum distance to any edge (normalized)
            float edgeDistNorm = min(
                min(distFromLeft, distFromRight),
                min(distFromTop, distFromBottom)
            );
            
            // Convert to pixel distance
            float minSize = min(resolution.x, resolution.y);
            float edgeDist = edgeDistNorm * minSize;
            
            // Edge factor: 1.0 at edge, 0.0 beyond edgeZone
            float edgeFactor = smoothstep(edgeZone, 0.0, edgeDist);
            
            // Calculate magnification scale
            // At edge: scale = 1.0 + magnification
            // Beyond edge zone: scale = 1.0
            float scale = 1.0 + (magnification * edgeFactor);
            
            // Apply radial distortion from center
            float2 toCenter = uv - center;
            float2 magnifiedUV = center + (toCenter / scale);
            
            // Convert back to pixel coordinates
            float2 magnifiedCoord = magnifiedUV * resolution;
            
            // Sample the content shader at magnified position
            return content.eval(magnifiedCoord);
        }
    """
    
    /**
     * Creates a RenderEffect with glass magnification applied.
     * 
     * @param widthPx Width of the target view in pixels
     * @param heightPx Height of the target view in pixels
     * @param magnificationStrength Magnification intensity (0.0-1.0)
     * @param edgeZonePx Edge zone width in pixels
     * @return RenderEffect to apply via Modifier.graphicsLayer { renderEffect = ... }
     */
    fun create(
        widthPx: Float,
        heightPx: Float,
        magnificationStrength: Float = 0.4f,
        edgeZonePx: Float = 40f
    ): androidx.compose.ui.graphics.RenderEffect {
        val shader = RuntimeShader(SHADER_SOURCE)
        
        shader.setFloatUniform("resolution", widthPx, heightPx)
        shader.setFloatUniform("magnification", magnificationStrength.coerceIn(0f, 1f))
        shader.setFloatUniform("edgeZone", edgeZonePx.coerceIn(10f, 100f))
        
        return RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
    
    /**
     * Check if glass magnification effect is supported on current device.
     * Requires Android 13+ (API 33).
     */
    fun isSupported(): Boolean {
        return true
    }
}
