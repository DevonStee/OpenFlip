package com.bokehforu.openflip.feature.chime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.feature.chime.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service for playing hourly chime sounds with high priority.
 * Ensures immediate playback even when the app is in the background or device is in Doze mode.
 */
@AndroidEntryPoint
class ChimeForegroundService : Service() {

    @Inject
    lateinit var soundManager: SoundProvider

    private val mainHandler = Handler(Looper.getMainLooper())
    private var pendingStopRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val debugEnabled = isDebuggable()
        logDebug(debugEnabled, "onStartCommand called")

        val chimeCount = (intent?.getIntExtra(EXTRA_CHIME_COUNT, 1) ?: 1).coerceIn(1, 12)
        val isTest = intent?.getBooleanExtra(EXTRA_IS_TEST, false) ?: false

        // Start as foreground service immediately with a minimal notification
        startForeground(NOTIFICATION_ID, createNotification(chimeCount, isTest))

        // Play the chime sound
        logDebug(debugEnabled, "Playing chime with count: $chimeCount")
        soundManager.playChimeSound(chimeCount)

        // Stop the service shortly after playback starts
        // The sound will continue playing even after service stops
        val stopDelay = soundManager
            .getEstimatedChimePlaybackDurationMs(chimeCount)
            .coerceAtLeast(if (isTest) 1200L else 2000L)
        pendingStopRunnable?.let { mainHandler.removeCallbacks(it) }
        val stopRunnable = Runnable {
            logDebug(debugEnabled, "Stopping foreground service")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
            pendingStopRunnable = null
        }
        pendingStopRunnable = stopRunnable
        mainHandler.postDelayed(stopRunnable, stopDelay)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        pendingStopRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingStopRunnable = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.chime_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW // Low importance to minimize visual interruption
            ).apply {
                description = getString(R.string.chime_notification_channel_description)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(chimeCount: Int, isTest: Boolean): Notification {
        val contentText = if (isTest) {
            getString(R.string.chime_notification_test_text)
        } else {
            getString(R.string.chime_notification_text, chimeCount)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.chime_notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.icon_chime_notification_24dp)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSilent(true) // No sound for the notification itself
            .build()
    }

    private fun isDebuggable(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun logDebug(debugEnabled: Boolean, message: String) {
        if (debugEnabled) {
            Log.d(TAG, message)
        }
    }

    companion object {
        private const val TAG = "ChimeForegroundService"
        private const val CHANNEL_ID = "chime_foreground_service"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_CHIME_COUNT = "chime_count"
        const val EXTRA_IS_TEST = "is_test"

        fun start(context: Context, chimeCount: Int, isTest: Boolean = false) {
            val intent = Intent(context, ChimeForegroundService::class.java).apply {
                putExtra(EXTRA_CHIME_COUNT, chimeCount)
                putExtra(EXTRA_IS_TEST, isTest)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
