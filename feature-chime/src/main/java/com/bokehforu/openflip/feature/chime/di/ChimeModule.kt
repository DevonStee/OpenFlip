package com.bokehforu.openflip.feature.chime.di

import android.content.Context
import com.bokehforu.openflip.domain.gateway.HourlyChimeScheduler
import com.bokehforu.openflip.domain.gateway.HourlyChimeTester
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.feature.chime.HourlyChimeManager
import com.bokehforu.openflip.feature.chime.data.HourlyChimeSchedulerImpl
import com.bokehforu.openflip.feature.chime.data.HourlyChimeTesterImpl
import com.bokehforu.openflip.feature.clock.manager.AppLifecycleMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChimeModule {

    @Binds
    abstract fun bindHourlyChimeScheduler(impl: HourlyChimeSchedulerImpl): HourlyChimeScheduler

    @Binds
    abstract fun bindHourlyChimeTester(impl: HourlyChimeTesterImpl): HourlyChimeTester

    companion object {
        @Provides
        @Singleton
        fun provideHourlyChimeManager(
            @ApplicationContext context: Context,
            settingsRepository: SettingsRepository,
            lifecycleMonitor: AppLifecycleMonitor
        ): HourlyChimeManager {
            return HourlyChimeManager(context, settingsRepository, lifecycleMonitor)
        }
    }
}
