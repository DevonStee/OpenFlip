package com.bokehforu.openflip.feature.chime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChimeTestReceiver : BroadcastReceiver() {

    @Inject
    lateinit var hourlyChimeManager: HourlyChimeManager

    override fun onReceive(context: Context, intent: Intent) {
        val debugEnabled = isDebuggable(context)
        if (!debugEnabled) {
            return
        }
        logDebug(debugEnabled, "Test receiver called")

        when (intent.action) {
            ACTION_TEST_CHIME -> {
                val hour = intent.getIntExtra(EXTRA_HOUR, 6)
                val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
                logDebug(debugEnabled, "Testing chime for $hour:$minute")
                hourlyChimeManager.testChime(hour, minute)
            }
            ACTION_SCHEDULE_NEXT -> {
                logDebug(debugEnabled, "Scheduling next chime")
                hourlyChimeManager.scheduleNextChime()
            }
        }
    }

    companion object {
        private const val TAG = "ChimeTest"
        const val ACTION_TEST_CHIME = "com.bokehforu.openflip.action.TEST_CHIME"
        const val ACTION_SCHEDULE_NEXT = "com.bokehforu.openflip.action.SCHEDULE_NEXT"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
    }

    private fun logDebug(debugEnabled: Boolean, message: String) {
        if (debugEnabled) {
            Log.d(TAG, message)
        }
    }

    private fun isDebuggable(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}
