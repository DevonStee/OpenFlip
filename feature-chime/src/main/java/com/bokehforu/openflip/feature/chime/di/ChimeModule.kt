/*
 * Copyright (C) 2026 DevonStee
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
