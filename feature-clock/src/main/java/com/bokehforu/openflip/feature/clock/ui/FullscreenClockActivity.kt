package com.bokehforu.openflip.feature.clock.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.bokehforu.openflip.feature.clock.databinding.ActivityMainBinding
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider
import com.bokehforu.openflip.core.controller.interfaces.SoundProvider
import com.bokehforu.openflip.domain.repository.SettingsRepository
import com.bokehforu.openflip.domain.result.DomainError
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.domain.usecase.StartSleepTimerError
import com.bokehforu.openflip.domain.usecase.ToggleThemeUseCase
import com.bokehforu.openflip.domain.usecase.UpdateShowSecondsUseCase
import com.bokehforu.openflip.feature.clock.manager.TimeProvider
import com.bokehforu.openflip.core.settings.OledProtectionController
import com.bokehforu.openflip.core.settings.SettingsDefaults
import com.bokehforu.openflip.core.settings.SettingsHostController
import com.bokehforu.openflip.core.settings.SettingsSleepTimerState
import com.bokehforu.openflip.core.settings.SleepTimerDialogProvider
import com.bokehforu.openflip.core.settings.ThemeTransitionProvider
import com.bokehforu.openflip.feature.clock.ui.compose.MainOptionsButton
import com.bokehforu.openflip.feature.clock.ui.controller.FlipAnimationsController
import com.bokehforu.openflip.feature.clock.ui.controller.FullscreenClockControllersFactory
import com.bokehforu.openflip.feature.clock.ui.controller.FullscreenClockStateCollector
import com.bokehforu.openflip.feature.clock.ui.controller.LightToggleController
import com.bokehforu.openflip.feature.clock.ui.helper.GestureRouter
import com.bokehforu.openflip.feature.clock.ui.transition.ColorTransitionController
import com.bokehforu.openflip.core.ui.feedback.performSystemHapticClick
import com.bokehforu.openflip.feature.clock.ui.theme.OpenFlipTheme
import com.bokehforu.openflip.feature.clock.ui.theme.ThemeApplier
import com.bokehforu.openflip.feature.settings.controller.HourlyChimeSettingsController
import com.bokehforu.openflip.feature.settings.ui.settings.SettingsComposeSheet
import com.bokehforu.openflip.feature.settings.ui.theme.OpenFlipTheme as SettingsOpenFlipTheme
import com.bokehforu.openflip.feature.settings.viewmodel.SettingsViewModel
import com.bokehforu.openflip.feature.clock.viewmodel.ClockUiState
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@AndroidEntryPoint
class FullscreenClockActivity : AppCompatActivity(), OledProtectionController, SleepTimerDialogProvider, ThemeTransitionProvider, SettingsHostController {

    companion object {
        private const val STATE_CLOCK_SCALE = "state_clock_scale"
    }

    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var haptics: HapticsProvider
    @Inject lateinit var sound: SoundProvider
    @Inject lateinit var timeProvider: TimeProvider
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var toggleThemeUseCase: ToggleThemeUseCase
    @Inject lateinit var updateShowSecondsUseCase: UpdateShowSecondsUseCase
    @Inject lateinit var lightToggleControllerFactory: LightToggleController.Factory
    @Inject lateinit var hourlyChimeSettingsController: HourlyChimeSettingsController
    
    private val viewModel: FullscreenClockViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var isSettingsSheetVisible by mutableStateOf(false)
    
    // UI Helpers
    private lateinit var windowConfigurator: WindowConfigurator
    private lateinit var themeApplier: ThemeApplier
    
    // Controllers
    private lateinit var stateCollector: FullscreenClockStateCollector
    private lateinit var gestureRouter: GestureRouter
    private lateinit var swipeHintIcon: ImageView
    private var controllersBundle: FullscreenClockControllersFactory.Bundle? = null
    private lateinit var colorTransitionController: ColorTransitionController
    
    private var displayManager: android.hardware.display.DisplayManager? = null
    private var currentOrientation: Int = Configuration.ORIENTATION_UNDEFINED
    private var restoredScale: Float? = null
    private var pendingScaleOnReinflate: Float? = null
    private val settingsSleepTimerState: StateFlow<SettingsSleepTimerState> by lazy(LazyThreadSafetyMode.NONE) {
        viewModel.sleepTimerState
            .map { SettingsSleepTimerState(isActive = it.isActive, remainingSeconds = it.remainingSeconds) }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = SettingsSleepTimerState()
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentOrientation = resources.configuration.orientation
        restoredScale = savedInstanceState?.getFloat(STATE_CLOCK_SCALE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        haptics.setHapticEnabled(viewModel.uiState.value.hapticEnabled)
        sound.setSoundEnabled(viewModel.uiState.value.soundEnabled)

        windowConfigurator = WindowConfigurator(this) { viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK }

        // Setup all UI components
        setupUI()
        initializeStateCollector()
        registerDisplayListener()
        applyRestoredScaleIfNeeded()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(STATE_CLOCK_SCALE, binding.flipClockView.getCurrentScale())
        super.onSaveInstanceState(outState)
    }

    /**
     * Setup all UI components - called on onCreate and after orientation change re-inflate
     */
    private fun setupUI() {
        binding.flipClockView.updateTheme(viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK)
        swipeHintIcon = binding.swipeHintIcon

        windowConfigurator.applyBackgroundColor(viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK)
        themeApplier = ThemeApplier(binding, binding.flipClockView)
        colorTransitionController = ColorTransitionController(binding.root)

        // Setup clock view dependencies BEFORE creating timeManagementController
        // to ensure isHourlyChimeEnabled is set before any time updates trigger flip animations
        binding.flipClockView.sound = sound
        binding.flipClockView.haptics = haptics
        binding.flipClockView.isHourlyChimeEnabled = viewModel.uiState.value.isHourlyChimeEnabled

        // Initialize controllers
        setupControllers()

        // Setup initial UI states
        controllersBundle?.uiStateController?.initializeVisibility()
        applyOrientation(viewModel.uiState.value.orientationMode)

        // Initialize Compose Settings Button
        setupComposeSettingsButton()
        setupComposeSettingsSheetHost()

        binding.themeToggleButtonInclude.root.setOnClickListener {
            binding.themeToggleButtonInclude.root.performSystemHapticClick()
            controllersBundle?.themeToggleController?.handleThemeToggleClick {}
        }

        binding.stateToggleButtonInclude.root.setOnClickListener {
            binding.stateToggleButtonInclude.stateGlowView.performClick()
        }

        windowConfigurator.hideSystemUI()

        setupGestureRouter()

        // Initial setup complete
        controllersBundle?.timeManagementController?.updateTime(animate = false)
        controllersBundle?.timeManagementController?.updateSeconds()
        binding.flipClockView.setDarkTheme(viewModel.uiState.value.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK)
        controllersBundle?.shortcutIntentHandler?.handleIntent(intent)
    }

    private fun setupComposeSettingsButton() {
        binding.settingsButtonInclude.composeSettingsButton.apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val animState by viewModel.settingsButtonAnimState.collectAsState()

                val isDark = uiState.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK
                OpenFlipTheme(darkTheme = isDark) {
                    MainOptionsButton(
                        isDark = isDark,
                        showSeconds = uiState.showSeconds,
                        secondsText = animState.currentSeconds,
                        nextSecondsText = animState.nextSeconds,
                        gearRotationTrigger = viewModel.gearRotationTrigger,
                        activeTranslationY = animState.activeTranslationY,
                        incomingTranslationY = animState.incomingTranslationY,
                        activeAlpha = animState.activeAlpha,
                        incomingAlpha = animState.incomingAlpha,
                        onClick = {
                            if (openSettingsSheet()) {
                                binding.settingsButtonInclude.composeSettingsButton.performSystemHapticClick()
                                controllersBundle?.gearAnimationController?.rotateOnce()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun setupComposeSettingsSheetHost() {
        binding.composeSettingsSheetHost.apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val sheetTimerState by sleepTimerState.collectAsState()
                val isDark = uiState.theme == com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode.DARK

                SettingsOpenFlipTheme(darkTheme = isDark) {
                    SettingsComposeSheet(
                        visible = isSettingsSheetVisible,
                        settingsViewModel = settingsViewModel,
                        sleepTimerState = sheetTimerState,
                        onDismiss = { isSettingsSheetVisible = false },
                        onPerformClickFeedback = { performSettingsClickFeedback() },
                        onSetInteracting = { setSettingsInteracting(it) },
                        onApplyThemeTransition = { applyThemeTransition(it) },
                        onSetOledProtection = { setOledProtection(it) },
                        onStartSleepTimer = { startSleepTimer(it) },
                        onStopSleepTimer = { stopSleepTimer() },
                        onOpenCustomSleepTimerDialog = { openCustomSleepTimerDialog() },
                        onOpenScreensaverSettings = { openScreensaverSettings() },
                        onOpenAlarmPermissionSettings = { startActivitySafely(it) },
                        onToggleHourlyChime = { enabled -> hourlyChimeSettingsController.handleToggle(enabled) },
                        onTestChime = { hourlyChimeSettingsController.testChime(3, 0) },
                        onOpenOriginalApp = {
                            startActivitySafely(
                                Intent(Intent.ACTION_VIEW, getString(com.bokehforu.openflip.feature.settings.R.string.urlFliqloIos).toUri())
                            )
                        },
                        onContact = { sendContactEmail() },
                        onQuitApp = { finishAffinity() },
                        onResetToDefaults = {
                            applyThemeTransition(SettingsDefaults.DARK_THEME)
                            settingsViewModel.resetToDefaults()
                        },
                        packageNameProvider = { packageName }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        windowConfigurator.hideSystemUI()
        controllersBundle?.uiStateController?.updateVisibilityInstant()
        controllersBundle?.timeManagementController?.updateTime(animate = false)
    }

    override fun onPause() {
        super.onPause()

        if (::binding.isInitialized) {
            binding.flipClockView.pauseAnimations()
        }

        controllersBundle?.flipAnimationsController?.cleanup()
        controllersBundle?.knobInteractionController?.stopKnobFling()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        controllersBundle?.shortcutIntentHandler?.handleIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Check if orientation actually changed
        val newOrientation = newConfig.orientation
        if (newOrientation != currentOrientation && newOrientation != Configuration.ORIENTATION_UNDEFINED) {
            handleOrientationConfigurationChange(newOrientation)
        } else {
            handleNonOrientationConfigurationChange()
        }

        // Re-hide system UI after configuration change
        windowConfigurator.hideSystemUI()
    }

    /**
     * Cleanup controllers before re-inflating layout on orientation change
     */
    private fun cleanupControllersForReinflate() {
        FullscreenClockControllersFactory.cleanupForReinflate(
            bundle = controllersBundle,
            lifecycleOwner = this,
            lifecycle = lifecycle
        )
        controllersBundle = null
    }

    override fun onDestroy() {
        super.onDestroy()
        displayManager?.let { windowConfigurator.unregisterDisplayListener(it) }
        FullscreenClockControllersFactory.cleanupOnDestroy(
            bundle = controllersBundle,
            lifecycleOwner = this,
            lifecycle = lifecycle
        )
        controllersBundle = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (binding.infiniteKnobButtonInclude.infiniteKnobView.isInteracting) return super.onTouchEvent(event)
        return if (::gestureRouter.isInitialized) gestureRouter.onTouchEvent(event) else false || super.onTouchEvent(event)
    }

    private fun toggleInteractionState() {
        viewModel.isInteracting = !viewModel.isInteracting
        controllersBundle?.uiStateController?.onInteractionStateChanged()
        controllersBundle?.lightEffectManager?.updateLightSourcePosition()
    }

    private fun applyOrientation(mode: Int) {
        requestedOrientation = when (mode) {
            1 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            3 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun openSleepTimerDialog() {
        controllersBundle?.systemIntegrationController?.openSleepTimerDialog()
    }

    override fun openCustomSleepTimerDialog() {
        controllersBundle?.systemIntegrationController?.openCustomSleepTimerDialog()
    }

    override fun requestThemeChange(isDark: Boolean, force: Boolean) {
        controllersBundle?.themeToggleController?.requestThemeChange(isDark, force)
    }

    override val sleepTimerState: StateFlow<SettingsSleepTimerState>
        get() = settingsSleepTimerState

    override fun performSettingsClickFeedback() {
        viewModel.performClickFeedback()
    }

    override fun setSettingsInteracting(interacting: Boolean) {
        viewModel.isInteracting = interacting
    }

    override fun startSleepTimer(minutes: Int) {
        val result = viewModel.startSleepTimer(minutes)
        if (result is Result.Failure) {
            showSleepTimerStartError(result.error)
        }
    }

    override fun stopSleepTimer() {
        viewModel.stopSleepTimer()
    }

    private fun showSleepTimerStartError(error: DomainError) {
        val messageRes = when (error) {
            is StartSleepTimerError.InvalidDuration,
            is StartSleepTimerError.DurationTooLarge -> com.bokehforu.openflip.feature.settings.R.string.errorSleepTimerInvalidDuration
            else -> com.bokehforu.openflip.feature.settings.R.string.errorSleepTimerStartFailed
        }
        Toast.makeText(this, getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    private fun setupControllers() {
        val controllers = FullscreenClockControllersFactory.create(
            activity = this,
            binding = binding,
            viewModel = viewModel,
            haptics = haptics,
            sound = sound,
            settingsRepository = settingsRepository,
            toggleThemeUseCase = toggleThemeUseCase,
            updateShowSecondsUseCase = updateShowSecondsUseCase,
            lightToggleControllerFactory = lightToggleControllerFactory,
            windowConfigurator = windowConfigurator,
            themeApplier = themeApplier,
            colorTransitionController = colorTransitionController,
            lifecycle = lifecycle,
            onApplyOrientation = { mode -> applyOrientation(mode) },
            onSetOledProtection = { enabled -> setOledProtection(enabled) },
            onOpenSettings = { openSettingsSheet() },
            onLightStateChanged = {
                controllersBundle?.uiStateController?.updateSecondsVisibility()
                controllersBundle?.lightEffectManager?.updateLightSourcePosition()
            }
        )
        controllersBundle = controllers
    }
    
    fun applyThemeTransition(targetIsDark: Boolean) {
        controllersBundle?.themeToggleController?.requestThemeChange(targetIsDark, force = false)
    }

    override fun setOledProtection(enabled: Boolean) {
        controllersBundle?.systemIntegrationController?.setOledProtection(enabled)
        controllersBundle?.lightEffectManager?.updateLightSourcePosition()
    }

    private fun openSettingsSheet(): Boolean {
        if (isSettingsSheetVisible) return false
        isSettingsSheetVisible = true
        viewModel.isInteracting = true
        return true
    }

    private fun openScreensaverSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
        }.onFailure {
            Toast.makeText(
                this,
                getString(com.bokehforu.openflip.feature.settings.R.string.errorNoScreensaverSettings),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startActivitySafely(intent: Intent) {
        runCatching { startActivity(intent) }
            .onFailure {
                Toast.makeText(this, "No app found to handle this action", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendContactEmail() {
        val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val timeZone = java.util.TimeZone.getDefault().id
        val deviceModel = android.os.Build.MODEL
        val androidVersion = android.os.Build.VERSION.RELEASE

        val informationHeader = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyDeviceInformation)
        val timeLabel = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyTime)
        val tzLabel = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyTimezone)
        val deviceLabel = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyDevice)
        val androidLabel = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyAndroid)
        val descHeader = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyBugDescription)
        val placeholder = getString(com.bokehforu.openflip.feature.settings.R.string.emailBodyPlaceholder)

        val body = """
$informationHeader
- $timeLabel $currentTime
- $tzLabel $timeZone
- $deviceLabel $deviceModel
- $androidLabel $androidVersion

$descHeader
$placeholder
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(com.bokehforu.openflip.feature.settings.R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(com.bokehforu.openflip.feature.settings.R.string.emailSubjectBug))
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivitySafely(intent)
    }

    private fun renderState(state: ClockUiState) {
        binding.flipClockView.apply {
            showSeconds = state.showSeconds
            showFlaps = state.showFlaps
            // Theme changes are handled by SettingsCoordinator via settingsFlow.
            // Avoid applying theme directly here to prevent transition race conditions.
            scaleX = state.scale
             scaleY = state.scale
         }

        if (state.brightnessOverride >= 0) {
            val lp = window.attributes
            lp.screenBrightness = state.brightnessOverride
            window.attributes = lp
        }

         // Bulb UI state (survives rotation via ViewModel SavedStateHandle)
        controllersBundle?.lightToggleController?.applyState(state.bulb, state.bulbCountdownSeconds)
    }

    private fun initializeStateCollector() {
        stateCollector = FullscreenClockStateCollector(
            lifecycleOwner = this,
            viewModel = viewModel,
            onRenderState = { state -> renderState(state) },
            onInteractionChanged = {
                controllersBundle?.uiStateController?.onInteractionStateChanged()
            },
            onLightSourceUpdate = {
                controllersBundle?.lightEffectManager?.updateLightSourcePosition()
            }
        )
        stateCollector.start()
    }

    private fun registerDisplayListener() {
        displayManager = getSystemService(android.hardware.display.DisplayManager::class.java)
        displayManager?.let { windowConfigurator.registerDisplayListener(it) }
    }

    private fun applyRestoredScaleIfNeeded() {
        restoredScale?.let { scale ->
            binding.root.post { binding.flipClockView.setScale(scale) }
        }
    }

    private fun setupGestureRouter() {
        val displayMetrics = resources.displayMetrics
        gestureRouter = GestureRouter(
            context = this,
            viewModel = viewModel,
            flipClockView = binding.flipClockView,
            window = window,
            screenHeight = displayMetrics.heightPixels.toFloat(),
            onToggleInteraction = this::toggleInteractionState,
            brightnessDefault = 0.5f,
            brightnessMin = 0.1f,
            brightnessMax = 1.0f,
            swipeHintView = swipeHintIcon,
            onHapticBoundary = { haptics.performLongPress() }
        )
    }

    private fun handleOrientationConfigurationChange(newOrientation: Int) {
        pendingScaleOnReinflate = if (::binding.isInitialized) binding.flipClockView.getCurrentScale() else null
        currentOrientation = newOrientation
        cleanupControllersForReinflate()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()

        pendingScaleOnReinflate?.let { scale ->
            binding.root.post { binding.flipClockView.setScale(scale) }
        }

        renderState(viewModel.uiState.value)
    }

    private fun handleNonOrientationConfigurationChange() {
        if (::gestureRouter.isInitialized) {
            val displayMetrics = resources.displayMetrics
            gestureRouter.updateDimensions(displayMetrics.heightPixels.toFloat())
        }

        binding.root.requestLayout()
        binding.root.post {
            controllersBundle?.lightEffectManager?.updateLightSourcePosition()
        }
    }
}
