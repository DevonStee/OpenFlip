package com.bokehforu.openflip.core.ui.feedback

import android.view.HapticFeedbackConstants
import android.view.View

fun View.performSystemHapticClick(): Boolean {
    return performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}
