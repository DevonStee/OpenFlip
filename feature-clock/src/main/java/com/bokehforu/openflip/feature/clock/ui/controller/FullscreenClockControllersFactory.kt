package com.bokehforu.openflip.feature.clock.ui.controller

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bokehforu.openflip.feature.clock.controller.SettingsCoordinator
import com.bokehforu.openflip.feature.clock.controller.SystemIntegrationController
import com.bokehforu.openflip.feature.clock.controller.TimeManagementController
import com.bokehforu.openflip.feature.clock.controller.UIStateController
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.feature.clock.databinding.ActivityMainBinding
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.feature.clock.manager.LightEffectManager
import com.bokehforu.openflip.feature.clock.ui.FullscreenClockActivity
import com.bokehforu.openflip.feature.clock.ui.WindowConfigurator
import com.bokehforu.openflip.feature.clock.ui.theme.ThemeApplier
import com.bokehforu.openflip.feature.clock.ui.transition.ColorTransitionController
import com.bokehforu.openflip.core.util.FontProvider
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel

/**
 * Creates and wires all clock-screen controllers in one place.
 * This keeps FullscreenClockActivity focused on lifecycle and event routing.
 */
object FullscreenClockControllersFactory {

    data class Bundle(
        val stateToggleButton: com.bokehforu.openflip.feature.clock.view.StateToggleGlowView,
        val stateToggleIcon: android.widget.ImageView,
        val lightToggleController: LightToggleController,
        val themeToggleController: ThemeToggleController,
        val systemIntegrationController: SystemIntegrationController,
        val knobInteractionController: KnobInteractionController,
        val timeManagementController: TimeManagementController,
        val shortcutIntentHandler: ShortcutIntentHandler,
        val settingsCoordinator: SettingsCoordinator,
        val uiStateController: UIStateController,
        val lightEffectManager: LightEffectManager,
        val gearAnimationController: GearAnimationController,
        val flipAnimationsController: FlipAnimationsController,
    )

    fun create(
        activity: FullscreenClockActivity,
        binding: ActivityMainBinding,
        viewModel: FullscreenClockViewModel,
        haptics: HapticsProvider,
        sound: SoundProvider,
        settingsRepository: SettingsRepository,
        toggleThemeUseCase: ToggleThemeUseCase,
        updateShowSecondsUseCase: UpdateShowSecondsUseCase,
        lightToggleControllerFactory: LightToggleController.Factory,
        windowConfigurator: WindowConfigurator,
        themeApplier: ThemeApplier,
        colorTransitionController: ColorTransitionController,
        lifecycle: Lifecycle,
        onApplyOrientation: (Int) -> Unit,
        onSetOledProtection: (Boolean) -> Unit,
        onOpenSettings: () -> Unit,
        onLightStateChanged: () -> Unit,
    ): Bundle {
        val stateToggleButton = binding.stateToggleButtonInclude.stateGlowView
        val stateToggleIcon = binding.stateToggleButtonInclude.stateToggleIcon.apply { visibility = View.GONE }

        val lightToggleController = lightToggleControllerFactory.create(
            stateToggleButton = stateToggleButton,
            stateToggleIcon = stateToggleIcon,
            clockView = binding.flipClockView,
            isDarkThemeProvider = { viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK },
            onLightStateChanged = onLightStateChanged,
            onToggleRequested = {
                viewModel.onLightToggle()
            }
        )
        lightToggleController.bind()
        lightToggleController.setTheme(viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK)
        try {
            lightToggleController.setTypeface(FontProvider.getClockTypeface(activity))
        } catch (_: Exception) {
        }

        val gearAnimationController = GearAnimationController(viewModel)
        val flipAnimationsController = FlipAnimationsController(
            viewModel = viewModel,
            haptics = haptics,
            resources = activity.resources
        )

        val uiStateController = UIStateController(
            binding = binding,
            viewModel = viewModel,
            gearAnimationController = gearAnimationController,
            windowConfigurator = windowConfigurator,
            stateToggleGlowView = stateToggleButton
        )

        val timeManagementController = TimeManagementController(
            context = activity,
            viewModel = viewModel,
            clockView = binding.flipClockView,
            flipAnimationsController = flipAnimationsController,
            lifecycleOwner = activity
        )

        val knobInteractionController = KnobInteractionController(
            lifecycleOwner = activity,
            settingsRepository = settingsRepository,
            viewModel = viewModel,
            sound = sound,
            haptics = haptics,
            knobView = binding.infiniteKnobButtonInclude.infiniteKnobView,
            clockView = binding.flipClockView
        )
        knobInteractionController.initialize()
        timeManagementController.timeTravelController = knobInteractionController.timeTravelController
        timeManagementController.onHourChanged = { gearAnimationController.rotateOnce() }

        val lightEffectManager = LightEffectManager(
            clockView = binding.flipClockView,
            sourceView = stateToggleButton,
            rootView = binding.rootLayout
        )
        lifecycle.addObserver(lightEffectManager)

        val themeToggleController = ThemeToggleController(
            toggleThemeUseCase = toggleThemeUseCase,
            uiStateController = uiStateController,
            windowConfigurator = windowConfigurator,
            colorTransitionController = colorTransitionController,
            clockView = binding.flipClockView,
            themeApplier = themeApplier,
            isDarkThemeProvider = { viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK }
        )

        val systemIntegrationController = SystemIntegrationController(
            activity = activity,
            window = activity.window,
            viewModel = viewModel,
            clockView = binding.flipClockView,
            haptics = haptics,
            onBurnInShift = {
                lightEffectManager.updateLightSourcePosition()
            }
        )
        systemIntegrationController.initialize()

        val settingsCoordinator = SettingsCoordinator(
            settingsRepository = settingsRepository,
            timeController = timeManagementController,
            uiStateController = uiStateController,
            themeApplier = themeApplier,
            windowConfigurator = windowConfigurator,
            haptics = haptics,
            sound = sound,
            clockView = binding.flipClockView,
            stateToggleButton = stateToggleButton,
            applyOrientationAction = onApplyOrientation,
            applyWakeLockModeAction = { systemIntegrationController.applyWakeLockMode() },
            forceTurnOffLight = { lightToggleController.forceTurnOffLight() },
            setLightToggleTheme = { isDark -> lightToggleController.setTheme(isDark) },
            ensureInteractingState = {
                if (!viewModel.isInteracting) viewModel.isInteracting = true
            },
            applyThemeAction = { isDark -> themeToggleController.requestThemeChange(isDark, force = true) },
            applyOledProtectionAction = onSetOledProtection,
            resetBrightnessAction = {
                val lp = activity.window.attributes
                lp.screenBrightness = android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                activity.window.attributes = lp
            },
            onTimedBulbModeChanged = { isTimedEnabled ->
                viewModel.onTimedBulbModeChanged(isTimedEnabled)
            },
            isThemeTransitioning = { colorTransitionController.isTransitioning }
        )
        settingsCoordinator.bind()

        val shortcutIntentHandler = ShortcutIntentHandler(
            toggleThemeUseCase = toggleThemeUseCase,
            updateShowSecondsUseCase = updateShowSecondsUseCase,
            onOpenSettings = onOpenSettings,
            isDarkThemeProvider = { viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK },
            isShowSecondsProvider = { viewModel.uiState.value.showSeconds }
        )

        return Bundle(
            stateToggleButton = stateToggleButton,
            stateToggleIcon = stateToggleIcon,
            lightToggleController = lightToggleController,
            themeToggleController = themeToggleController,
            systemIntegrationController = systemIntegrationController,
            knobInteractionController = knobInteractionController,
            timeManagementController = timeManagementController,
            shortcutIntentHandler = shortcutIntentHandler,
            settingsCoordinator = settingsCoordinator,
            uiStateController = uiStateController,
            lightEffectManager = lightEffectManager,
            gearAnimationController = gearAnimationController,
            flipAnimationsController = flipAnimationsController,
        )
    }

    fun cleanupForReinflate(
        bundle: Bundle?,
        lifecycleOwner: LifecycleOwner,
        lifecycle: Lifecycle
    ) {
        cleanup(bundle, lifecycleOwner, lifecycle)
    }

    fun cleanupOnDestroy(
        bundle: Bundle?,
        lifecycleOwner: LifecycleOwner,
        lifecycle: Lifecycle
    ) {
        cleanup(bundle, lifecycleOwner, lifecycle)
    }

    private fun cleanup(
        bundle: Bundle?,
        lifecycleOwner: LifecycleOwner,
        lifecycle: Lifecycle
    ) {
        if (bundle == null) {
            return
        }

        bundle.themeToggleController.destroy()
        bundle.lightToggleController.destroy()
        bundle.flipAnimationsController.cleanup()
        bundle.lightEffectManager.destroy()
        bundle.settingsCoordinator.unbind()
        lifecycle.removeObserver(bundle.lightEffectManager)
        bundle.timeManagementController.cleanup(lifecycleOwner)
        bundle.knobInteractionController.cleanup()
        bundle.systemIntegrationController.destroy()
        lifecycle.removeObserver(bundle.systemIntegrationController)
    }
}
