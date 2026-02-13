package com.kyant.backdrop.backdrops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kyant.backdrop.LayerBackdrop

/**
 * Returns a remember'ed backdrop handle. In this lightweight replacement
 * it simply holds an empty object to satisfy call sites.
 */
@Composable
fun rememberLayerBackdrop(): LayerBackdrop = remember { LayerBackdrop() }
