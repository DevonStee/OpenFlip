package com.bokehforu.openflip.core.util

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

fun Context.resolveThemeColor(@AttrRes attrRes: Int, @StyleRes themeRes: Int? = null): Int {
    val themedContext = if (themeRes != null) ContextThemeWrapper(this, themeRes) else this
    val typedValue = TypedValue()
    val theme = themedContext.theme
    if (theme.resolveAttribute(attrRes, typedValue, true)) {
        return if (typedValue.resourceId != 0) {
            themedContext.getColor(typedValue.resourceId)
        } else {
            typedValue.data
        }
    }
    return 0
}
