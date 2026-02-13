package com.bokehforu.openflip.feature.clock.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import com.bokehforu.openflip.core.manager.Time
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeProvider @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope
) : TimeSource {
    override fun getCurrentTime(is24Hour: Boolean): Time {
        val calendar = Calendar.getInstance()
        return Time(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            second = calendar.get(Calendar.SECOND),
            is24Hour = is24Hour
        )
    }

    override fun timeFlow(is24Hour: Boolean): Flow<Time> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(getCurrentTime(is24Hour))
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        
        context.registerReceiver(receiver, filter)
        
        send(getCurrentTime(is24Hour))
        
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    override fun secondsFlow(is24Hour: Boolean): Flow<Time> = flow {
        while (true) {
            emit(getCurrentTime(is24Hour))
            delay(1000)
        }
    }
}
