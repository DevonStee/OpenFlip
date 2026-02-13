package com.bokehforu.openflip.di.module

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.data.settings.SettingsStore
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.core.controller.interfaces.TimeSource
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.data.repository.SettingsRepositoryImpl
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import com.bokehforu.openflip.manager.FeedbackSoundManager
import com.bokehforu.openflip.core.manager.HapticFeedbackManager
import com.bokehforu.openflip.feature.clock.manager.TimeProvider
import com.bokehforu.openflip.data.settings.AppSettingsManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {

    @Binds
    abstract fun bindHapticsProvider(impl: HapticFeedbackManager): HapticsProvider

    @Binds
    abstract fun bindSoundProvider(impl: FeedbackSoundManager): SoundProvider

    @Binds
    abstract fun bindSettingsStore(impl: AppSettingsManager): SettingsStore

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindTimeSource(impl: TimeProvider): TimeSource

    companion object {
        @Provides
        @Singleton
        fun provideVibrator(@ApplicationContext context: Context): Vibrator? {
            val fromManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            return fromManager ?: run {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }

        @Provides
        @Singleton
        fun provideAppLifecycleMonitor(@ApplicationContext context: Context): AppLifecycleMonitor {
            return AppLifecycleMonitor(context).apply { initialize() }
        }

    }
}
