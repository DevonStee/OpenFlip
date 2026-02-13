package com.bokehforu.openflip.feature.clock.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bokehforu.openflip.feature.clock.R as AppR
import com.bokehforu.openflip.core.R as CoreR
import com.bokehforu.openflip.domain.result.Result
import com.bokehforu.openflip.domain.usecase.StartSleepTimerError
import com.bokehforu.openflip.feature.clock.viewmodel.ThemeMode
import com.bokehforu.openflip.feature.clock.view.CircularTimerView
import com.bokehforu.openflip.feature.clock.viewmodel.FullscreenClockViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.bokehforu.openflip.feature.clock.ui.helper.SystemBarStyleHelper
import com.bokehforu.openflip.core.controller.interfaces.HapticsProvider

class SleepTimerDialogManager(
    private val context: Context,
    private val viewModel: FullscreenClockViewModel,
    private val haptics: HapticsProvider?
) {

    private data class DurationOption(val label: String, val iconRes: Int)

    fun handleSleepTimerClick() {
        val state = viewModel.sleepTimerState.value
         val themeRes = if (viewModel.uiState.value.theme == ThemeMode.DARK) {
            CoreR.style.Theme_OpenFlip_Dialog_Dark
        } else {
            CoreR.style.Theme_OpenFlip_Dialog_Light
        }
        val themedContext = android.view.ContextThemeWrapper(context, themeRes)

        if (state.isActive) {
            showStopTimerDialog(themedContext)
        } else {
            showDurationSelectionDialog(themedContext)
        }
    }

    private fun configureDialogWindow(window: android.view.Window?) {
        window ?: return
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
         SystemBarStyleHelper.applyTransparentBars(
             window = window,
             useLightIcons = viewModel.uiState.value.theme != ThemeMode.DARK
         )
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .apply {
                hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
    }

    private fun showStopTimerDialog(themedContext: Context) {
        val titleView = createDialogTitleView(themedContext, CoreR.string.titleSleepTimer)
        val dialog = MaterialAlertDialogBuilder(themedContext)
            .setCustomTitle(titleView)
            .setMessage(themedContext.getString(CoreR.string.actionStopTimer) + "?")
            .setPositiveButton(CoreR.string.actionOK) { _, _ ->
                viewModel.stopSleepTimer()
            }
            .setNegativeButton(CoreR.string.actionCancel, null)
            .create()

        titleView.findViewById<View>(AppR.id.buttonClose).setOnClickListener {
            dialog.dismiss()
        }

        // Apply FLAG_NOT_FOCUSABLE before show() to prevent system UI from reacting to focus change
        dialog.window?.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        dialog.show()

        // Configure flags and immersive behavior, then clear NOT_FOCUSABLE so user can interact
        configureDialogWindow(dialog.window)
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun showDurationSelectionDialog(themedContext: Context) {
        val options = listOf(
            DurationOption(
                themedContext.getString(CoreR.string.optionSleep15Min),
                CoreR.drawable.icon_sleep_15_min
            ),
            DurationOption(
                themedContext.getString(CoreR.string.optionSleep30Min),
                CoreR.drawable.icon_sleep_30_min
            ),
            DurationOption(
                themedContext.getString(CoreR.string.optionSleep1Hour),
                CoreR.drawable.icon_sleep_1_hour
            ),
            DurationOption(
                themedContext.getString(CoreR.string.optionSleep2Hours),
                CoreR.drawable.icon_sleep_2_hours
            ),
            DurationOption(
                themedContext.getString(CoreR.string.optionSleep3Hours),
                CoreR.drawable.icon_sleep_3_hours
            ),
            DurationOption(
                themedContext.getString(CoreR.string.optionSleepCustom),
                CoreR.drawable.icon_sleep_custom
            )
        )

        val titleView = createDialogTitleView(themedContext, CoreR.string.titleSelectDuration)

        val scrollView = android.widget.ScrollView(themedContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val itemsContainer = android.widget.LinearLayout(themedContext).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val padding = themedContext.resources.getDimensionPixelSize(CoreR.dimen.spacingSmall)
            setPadding(0, 0, 0, padding)
        }

        scrollView.addView(itemsContainer)

        val dialog = MaterialAlertDialogBuilder(themedContext)
            .setCustomTitle(titleView)
            .setView(scrollView)
            .create()

        // Add description text
        val descriptionView = LayoutInflater.from(themedContext).inflate(
            AppR.layout.layout_dialog_description_text,
            itemsContainer,
            false
        )
        itemsContainer.addView(descriptionView)

        options.forEachIndexed { index, option ->
            val itemView = LayoutInflater.from(themedContext).inflate(
                AppR.layout.layout_dialog_item_duration,
                itemsContainer,
                false
            )

            itemView.findViewById<ImageView>(AppR.id.imageIcon).apply {
                if (option.iconRes != 0) {
                    setImageResource(option.iconRes)
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            itemView.findViewById<TextView>(AppR.id.textLabel).text = option.label

            // Add some vertical spacing between capsules
            val params = itemView.layoutParams as android.widget.LinearLayout.LayoutParams
            params.setMargins(0, 4, 0, 4)
            itemView.layoutParams = params

            itemView.setOnClickListener {
                when (index) {
                    0 -> viewModel.startSleepTimer(15)
                    1 -> viewModel.startSleepTimer(30)
                    2 -> viewModel.startSleepTimer(60)
                    3 -> viewModel.startSleepTimer(120)
                    4 -> viewModel.startSleepTimer(180)
                    5 -> showCustomSleepTimerDialog()
                }
                dialog.dismiss()
            }

            itemsContainer.addView(itemView)
        }

        titleView.findViewById<View>(AppR.id.buttonClose).setOnClickListener {
            dialog.dismiss()
        }

        // Apply FLAG_NOT_FOCUSABLE before show() to prevent system UI from reacting to focus change
        dialog.window?.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        dialog.show()

        // Configure flags and immersive behavior, then clear NOT_FOCUSABLE so user can interact
        configureDialogWindow(dialog.window)
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    fun showCustomSleepTimerDialog() {
         val themeRes = if (viewModel.uiState.value.theme == ThemeMode.DARK) {
            CoreR.style.Theme_OpenFlip_Dialog_Dark
        } else {
            CoreR.style.Theme_OpenFlip_Dialog_Light
        }
        val themedContext = android.view.ContextThemeWrapper(context, themeRes)

        val dialogView = LayoutInflater.from(themedContext).inflate(
            AppR.layout.layout_dialog_sleep_timer_circular,
            null
        )
         val circularTimer = dialogView.findViewById<CircularTimerView>(AppR.id.circularTimerView)
         circularTimer.hapticManager = haptics
        val btnCancel = dialogView.findViewById<Button>(AppR.id.buttonCancel)
        val btnStart = dialogView.findViewById<Button>(AppR.id.buttonStart)

         circularTimer.setColors(viewModel.uiState.value.theme == ThemeMode.DARK)

        val titleView = createDialogTitleView(themedContext, CoreR.string.hintCustomDurationAction)

        val dialog = MaterialAlertDialogBuilder(themedContext)
            .setCustomTitle(titleView)
            .setView(dialogView)
            .create()

        titleView.findViewById<View>(AppR.id.buttonClose).setOnClickListener {
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnStart.setOnClickListener {
            val minutes = circularTimer.getMinutes()
            val result = viewModel.startSleepTimer(minutes)
            if (result is Result.Success) {
                dialog.dismiss()
            } else if (result is Result.Failure) {
                val messageRes = when (result.error) {
                    is StartSleepTimerError.InvalidDuration,
                    is StartSleepTimerError.DurationTooLarge -> CoreR.string.errorSleepTimerInvalidDuration
                    else -> CoreR.string.errorSleepTimerStartFailed
                }
                Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
            }
        }

        // Apply FLAG_NOT_FOCUSABLE before show() to prevent system UI from reacting to focus change
        dialog.window?.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        )

        dialog.show()

        // Configure flags and immersive behavior, then clear NOT_FOCUSABLE so user can interact
        configureDialogWindow(dialog.window)
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun createDialogTitleView(context: Context, titleRes: Int): View {
        val parent = FrameLayout(context)
        return LayoutInflater.from(context).inflate(AppR.layout.layout_dialog_header, parent, false).apply {
            findViewById<TextView>(AppR.id.textTitle).text = context.getString(titleRes)
        }
    }
}
