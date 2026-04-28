package com.safeguard.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferences: SecurePreferencesManager
) : ViewModel() {

    private val _useDarkTheme = MutableStateFlow<Boolean?>(null)
    val useDarkTheme: StateFlow<Boolean?> = _useDarkTheme.asStateFlow()

    init {
        _useDarkTheme.value = when (preferences.themeMode) {
            SecurePreferencesManager.THEME_DARK -> true
            SecurePreferencesManager.THEME_LIGHT -> false
            else -> null // system
        }
    }

    fun setThemeMode(mode: String) {
        preferences.themeMode = mode
        _useDarkTheme.update {
            when (mode) {
                SecurePreferencesManager.THEME_DARK -> true
                SecurePreferencesManager.THEME_LIGHT -> false
                else -> null
            }
        }
    }
}
