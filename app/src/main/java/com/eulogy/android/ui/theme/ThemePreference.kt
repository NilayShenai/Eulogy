package com.eulogy.android.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * App theme preference is fixed to Dark. This placeholder keeps the existing API surface
 * in case other parts of the UI still reference the manager.
 */
enum class ThemePreference {
    Dark;

    val isDark: Boolean get() = true
}

object ThemePreferenceManager {
    private val _themeFlow = MutableStateFlow(ThemePreference.Dark)
    val themeFlow: StateFlow<ThemePreference> = _themeFlow

    fun init(context: Context) {
        // No-op: the theme is always dark
    }

    fun set(context: Context, preference: ThemePreference) {
        _themeFlow.value = ThemePreference.Dark
    }
}
