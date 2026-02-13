package com.bokehforu.openflip.feature.clock.controller

import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bokehforu.openflip.feature.clock.R
import com.bokehforu.openflip.feature.clock.manager.DisplayBurnInProtectionManager
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.settings.OledProtectionController
import com.bokehforu.openflip.core.settings.SleepTimerDialogProvider
import com.bokehforu.openflip.feature.clock.ui.dialog.SleepTimerDialogManager
import com.bokehforu.openflip.feature.clock.ui.controller.SleepWakeController
import com.bokehforu.openflip.feature.clock.view.FullscreenFlipClockView
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.bokehforu.openflip.core.R as CoreR

/**
 * Controller responsible for system-level integrations including:
 * - OLED Burn-in protection
 * - Sleep/Wake handling
 * - Wake locks
 * - Sleep Timer Dialog management
 */
class SystemIntegrationController(
    private val activity: AppCompatActivity,
    private val window: Window,
    private val viewModel: FullscreenClockViewModel,
    private val clockView: FullscreenFlipClockView,
    private val haptics: HapticsProvider,
    private val onBurnInShift: (() -> Unit)? = null
) : DefaultLifecycleObserver, OledProtectionController, SleepTimerDialogProvider {

    private lateinit var burnInProtectionManager: DisplayBurnInProtectionManager
    private lateinit var sleepWakeController: SleepWakeController
    private var isDestroyed = false

    // Lazy initialization for dialog manager as it requires fragments
    private val sleepTimerDialogManager by lazy {
        SleepTimerDialogManager(activity, viewModel, haptics)
    }

    fun initialize() {
        if (isDestroyed) return
        setupBurnInProtection()
        applyPersistentBrightness()

        // Add observers
        activity.lifecycle.addObserver(this)
        activity.lifecycle.addObserver(burnInProtectionManager)
    }

    private fun applyPersistentBrightness() {
         val brightness = viewModel.uiState.value.brightnessOverride
         if (brightness >= 0) {
             val lp = window.attributes
             lp.screenBrightness = brightness
             window.attributes = lp
         }
     }

    private fun setupBurnInProtection() {
         val shiftRange = activity.resources.getDimension(CoreR.dimen.oledShiftRange)
        burnInProtectionManager = DisplayBurnInProtectionManager(
            clockView,
            maxShiftPx = shiftRange,
            shiftApplier = { x, y ->
                clockView.translationX = x
                clockView.translationY = y
                clockView.setLightSourcePosition(
                    (clockView.width / 2f) + x,
                    (clockView.height / 2f) + y
                )
            }
        ).apply {
            setOnShiftListener {
                onBurnInShift?.invoke()
            }
        }

         // Initialize SleepWakeController which depends on BurnInManager
         sleepWakeController = SleepWakeController(
             activity = activity,
             window = window,
             viewModel = viewModel,
             burnInProtectionManager = burnInProtectionManager
         )
         sleepWakeController.bind(activity)
     }

    override fun onResume(owner: LifecycleOwner) {
        applyWakeLockMode()
        sleepWakeController.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        sleepWakeController.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        destroy()
    }

    fun destroy() {
        if (isDestroyed) return
        isDestroyed = true

        if (::sleepWakeController.isInitialized) {
            sleepWakeController.unbind()
            sleepWakeController.onPause()
        }

        if (::burnInProtectionManager.isInitialized) {
            activity.lifecycle.removeObserver(burnInProtectionManager)
            burnInProtectionManager.cleanup()
        }

        activity.lifecycle.removeObserver(this)
    }

    fun applyWakeLockMode() {
        if (::sleepWakeController.isInitialized) {
            sleepWakeController.applyWakeLockMode()
        }
    }

    // OledProtectionController implementation
    override fun setOledProtection(enabled: Boolean) {
        if (::burnInProtectionManager.isInitialized) {
            if (enabled) burnInProtectionManager.start()
            else burnInProtectionManager.stop()
            // Force light source recalculation immediately after toggle
            onBurnInShift?.invoke()
        }
    }

    // SleepTimerDialogProvider implementation
    override fun openSleepTimerDialog() {
        sleepTimerDialogManager.handleSleepTimerClick()
    }

    override fun openCustomSleepTimerDialog() {
        sleepTimerDialogManager.showCustomSleepTimerDialog()
    }
}
