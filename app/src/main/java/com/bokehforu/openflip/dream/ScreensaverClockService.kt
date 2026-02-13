package com.bokehforu.openflip.dream

import android.graphics.Color
import android.os.Build
import android.service.dreams.DreamService
import android.view.View
import android.view.WindowManager
import com.bokehforu.openflip.core.R as CoreR

import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bokehforu.openflip.R
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import com.bokehforu.openflip.feature.clock.manager.TimeSecondsTicker
import com.bokehforu.openflip.feature.clock.manager.DisplayBurnInProtectionManager
import java.util.Calendar
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreensaverClockService : DreamService() {
    
    private lateinit var flipClockView: FullscreenFlipClockView
    private lateinit var rootContainer: View
    @Inject lateinit var settingsRepository: SettingsRepository
    private val secondsTicker = TimeSecondsTicker { updateTime() }
    private lateinit var burnInManager: DisplayBurnInProtectionManager
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        
        isInteractive = false
        isFullscreen = true
        isScreenBright = true
        
        setContentView(R.layout.dream_flip_clock)
        
        rootContainer = findViewById(R.id.dreamRootContainer)
        flipClockView = findViewById(R.id.dreamFlipClockView)
        
        // DreamService doesn't inherit app theme, so set background programmatically
        // Always use black for screensaver (OLED-friendly, power-saving)
        rootContainer.setBackgroundColor(Color.BLACK)
        
        window?.let { applyImmersiveFullscreen(it) }
        
        flipClockView.setDarkTheme(settingsRepository.isDarkTheme())
        flipClockView.showSeconds = settingsRepository.showSeconds()
        flipClockView.showFlaps = true
        
        updateTime(animate = false)
    }
    
    private fun applyImmersiveFullscreen(window: android.view.Window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }
        
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
        }
    }
    
    override fun onDreamingStarted() {
        super.onDreamingStarted()
        
        window?.let { applyImmersiveFullscreen(it) }
        
        secondsTicker.setEnabled(true)
        secondsTicker.onResume(object : androidx.lifecycle.LifecycleOwner {
            override val lifecycle = androidx.lifecycle.LifecycleRegistry(this).apply {
                currentState = androidx.lifecycle.Lifecycle.State.RESUMED
            }
        })
        
        val shiftRange = resources.getDimension(CoreR.dimen.oledShiftRange)
        burnInManager = DisplayBurnInProtectionManager(flipClockView, maxShiftPx = shiftRange)
        if (settingsRepository.isOledProtectionEnabled()) {
            burnInManager.start()
        }
    }
    
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        // Simulate ON_PAUSE
        secondsTicker.onPause(object : androidx.lifecycle.LifecycleOwner {
            override val lifecycle = androidx.lifecycle.LifecycleRegistry(this).apply {
                currentState = androidx.lifecycle.Lifecycle.State.CREATED
            }
        })
        secondsTicker.setEnabled(false)
        if (::burnInManager.isInitialized) {
            burnInManager.stop()
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Final cleanup - critical for preventing leaks
        secondsTicker.setEnabled(false)
        if (::burnInManager.isInitialized) {
            burnInManager.cleanup() // Perform complete cleanup
        }
    }
    
    private fun updateTime(animate: Boolean = true) {
         val calendar = Calendar.getInstance()
         val formatMode = settingsRepository.getTimeFormatMode()
         val is24H = formatMode != 0
         
         val hourInt = if (is24H) {
             calendar.get(Calendar.HOUR_OF_DAY)
         } else {
             calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
         }
         val minuteInt = calendar.get(Calendar.MINUTE)
         
         val hourStr = if (is24H) {
             if (formatMode == 1) String.format(Locale.US, "%02d", hourInt) else hourInt.toString()
         } else {
             hourInt.toString()
         }
         val minuteStr = String.format(Locale.US, "%02d", minuteInt)
         
         val amPmStr = if (!is24H) {
             if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
         } else null
         
         flipClockView.setTime(hourStr, minuteStr, animate = animate, amPm = amPmStr)
     }
}
