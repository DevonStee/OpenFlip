package com.bokehforu.openflip.feature.clock.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.bokehforu.openflip.core.R as CoreR

import android.view.MotionEvent
import android.view.View
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.core.ui.feedback.performSystemHapticClick
import com.bokehforu.openflip.core.util.FontProvider
import kotlin.math.*

class CircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Configurable properties
    private val strokeWidthToSizeRatio = 0.08f // Stroke width relative to view size
    private val thumbRadiusToStrokeRatio = 1.0f // Thumb size relative to stroke width

    // Paints
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val thumbBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        try {
            typeface = FontProvider.getClockBoldTypeface(context)
        } catch (_: Exception) {
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    // State
    private var baseTextColor = 0
    private var action_redColor = 0
    private var maxMinutes = 300
    private var currentMinutes = 60
    private var currentMinutesText = currentMinutes.toString()

    // Drawing metrics
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var strokeWidth = 0f
    private val trackRect = RectF()
    private val arrowPath = Path()

    // Listener
    var onTimeChangedListener: ((Int) -> Unit)? = null

    var hapticManager: HapticsProvider? = null

    init {
        // Initialize with default colors
        action_redColor = resolveColor(context, CoreR.attr.actionPrimaryColor)
        trackPaint.color = resolveColor(context, CoreR.attr.settingsSectionColor)
        progressPaint.color = action_redColor
        thumbPaint.color = Color.WHITE

        baseTextColor = resolveColor(context, CoreR.attr.settingsPrimaryTextColor)
        textPaint.color = baseTextColor
        subTextPaint.color = resolveColor(context, CoreR.attr.settingsSecondaryTextColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minDim = min(w, h).toFloat()
        centerX = w / 2f
        centerY = h / 2f

        // Calculate dimensions based on view size
        strokeWidth = minDim * strokeWidthToSizeRatio
        // Calculate thumb size first to ensure proper padding
        val thumbRadius = strokeWidth * thumbRadiusToStrokeRatio * 1.2f
        
        // Increased padding to ensure proper thumb clearance
        val padding = strokeWidth + thumbRadius * 2.5f
        
        radius = (minDim - padding) / 2f
        
        trackRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        trackPaint.strokeWidth = strokeWidth
        progressPaint.strokeWidth = strokeWidth
        
        // Text size scaling
        textPaint.textSize = minDim * 0.2f
        subTextPaint.textSize = minDim * 0.06f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw track
        canvas.drawCircle(centerX, centerY, radius, trackPaint)

        // Calculate sweep angle based on minutes
        // Map 0..maxMinutes to 0..360 degrees
        // Start from top (-90 degrees)
        val sweepAngle = (currentMinutes.toFloat() / maxMinutes) * 360f

        // Draw progress arc
        if (sweepAngle > 0) {
            canvas.drawArc(trackRect, -90f, sweepAngle, false, progressPaint)
        }

        // Draw thumb (Arrow)
        val angleRad = Math.toRadians((sweepAngle - 90).toDouble())
        val thumbX = centerX + radius * cos(angleRad).toFloat()
        val thumbY = centerY + radius * sin(angleRad).toFloat()
        
        // Draw arrow pointing along the tangent of the arc
        canvas.save()
        canvas.translate(thumbX, thumbY)
        canvas.rotate(sweepAngle)

        val thumbSize = strokeWidth * thumbRadiusToStrokeRatio * 1.0f
        
        // Draw Triangle
        arrowPath.rewind()
        
        // Draw an isosceles triangle pointing along the tangent

        
        arrowPath.moveTo(0f, -thumbSize * 0.6f) // Bottom Check
        arrowPath.lineTo(thumbSize * 0.8f, 0f) // Tip
        arrowPath.lineTo(0f, thumbSize * 0.6f) // Top Check
        arrowPath.close()
        
        canvas.drawPath(arrowPath, thumbPaint)
        
        canvas.restore()


        // Dynamic Text Color Logic - Both number and "Minutes" change color
        if (currentMinutes > 0) {
            textPaint.color = action_redColor
            subTextPaint.color = action_redColor
        } else {
            textPaint.color = baseTextColor
            subTextPaint.color = resolveColor(context, CoreR.attr.settingsSecondaryTextColor)
        }

        // Draw text
        canvas.drawText(currentMinutesText, centerX, centerY + (textPaint.textSize * 0.3f), textPaint)
        canvas.drawText("Minutes", centerX, centerY + (textPaint.textSize * 0.9f), subTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                updateProgressFromTouch(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        performSystemHapticClick()
        return true
    }

    private fun updateProgressFromTouch(touchX: Float, touchY: Float) {
        val dx = touchX - centerX
        val dy = touchY - centerY
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        angle += 90f 
        if (angle < 0) angle += 360f
        
        var minutes = ((angle / 360f) * maxMinutes).roundToInt()

        if (minutes < 1) minutes = 1
        if (minutes > maxMinutes) minutes = maxMinutes

        if (minutes != currentMinutes) {
            currentMinutes = minutes
            currentMinutesText = currentMinutes.toString()

            // Haptic Feedback: Heavy click on 5-minute marks, light tick otherwise
            if (minutes % 5 == 0) {
                hapticManager?.performLongPress()
            } else {
                hapticManager?.performClick()
            }

            onTimeChangedListener?.invoke(currentMinutes)
            invalidate()
        }
    }

    fun setMinutes(minutes: Int) {
        currentMinutes = minutes.coerceIn(1, maxMinutes)
        currentMinutesText = currentMinutes.toString()
        invalidate()
    }

    fun getMinutes(): Int {
        return currentMinutes
    }

    fun setColors(isDark: Boolean) {
        val themeResId = if (isDark) CoreR.style.Theme_OpenFlip_Dark else CoreR.style.Theme_OpenFlip_Light
        val themedContext = android.view.ContextThemeWrapper(context, themeResId)

        // Resolve Attributes
        val trackColor = resolveColor(themedContext, CoreR.attr.timerTrackColor)
        val progressColor = resolveColor(themedContext, CoreR.attr.timerProgressColor)
        val primaryTextColor = resolveColor(themedContext, CoreR.attr.settingsPrimaryTextColor)
        val secondaryTextColor = resolveColor(themedContext, CoreR.attr.settingsSecondaryTextColor)

        // Apply
        trackPaint.color = trackColor
        progressPaint.color = progressColor
        baseTextColor = primaryTextColor
        subTextPaint.color = secondaryTextColor

        // Sync action color with progress color (usually Red)
        action_redColor = resolveColor(themedContext, CoreR.attr.actionPrimaryColor)

        if (isDark) {
            thumbPaint.color = Color.WHITE
            thumbPaint.clearShadowLayer()
        } else {
            thumbPaint.color = Color.WHITE
            thumbPaint.setShadowLayer(4f, 0f, 2f, 0x40000000)
        }

        // Update current paint immediately
        textPaint.color = if (currentMinutes > 0) action_redColor else baseTextColor

        invalidate()
    }

    private fun resolveColor(context: Context, attrId: Int): Int {
        val typedValue = android.util.TypedValue()
        val theme = context.theme
        if (theme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                return androidx.core.content.ContextCompat.getColor(context, typedValue.resourceId)
            }
            return typedValue.data
        }
        return Color.MAGENTA
    }
}
