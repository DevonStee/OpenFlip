package com.bokehforu.openflip.feature.clock.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Monitors the application's lifecycle state (foreground/background) and the device's screen state
 * to determine if the app should be active.
 */
class AppLifecycleMonitor(
    private val context: Context
) : DefaultLifecycleObserver {

    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    private val _isScreenInteractive = MutableStateFlow(true)
    val isScreenInteractive: StateFlow<Boolean> = _isScreenInteractive.asStateFlow()

    init {
        // Initialize with current state
        _isScreenInteractive.value = isInteractive()
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> _isScreenInteractive.value = true
                Intent.ACTION_SCREEN_OFF -> _isScreenInteractive.value = false
            }
        }
    }

    fun initialize() {
        // Observe Process Lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Observe Screen State via BroadcastReceiver
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        
        // Register receiver (context must be application context)
        context.registerReceiver(screenReceiver, screenFilter)
    }

    fun cleanup() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        _isForeground.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        _isForeground.value = false
    }

    private fun isInteractive(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isInteractive ?: true
    }
}
