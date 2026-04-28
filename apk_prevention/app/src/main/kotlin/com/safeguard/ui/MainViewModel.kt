package com.safeguard.ui

import androidx.lifecycle.ViewModel
import com.safeguard.data.local.preferences.SecurePreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: SecurePreferencesManager
) : ViewModel() {

    private val _isAuthenticated: MutableStateFlow<Boolean>
    private val _termsAcceptedComplete: MutableStateFlow<Boolean>
    private val _privacyOnboardingComplete: MutableStateFlow<Boolean>
    private val _requireOnboardingAfterRegistration: MutableStateFlow<Boolean>
    private val _permissionOnboardingComplete: MutableStateFlow<Boolean>

    init {
        prefs.migrateAuthenticationForExistingUsers()
        prefs.migrateTermsAcceptanceForExistingUsers()
        _isAuthenticated = MutableStateFlow(prefs.isAuthenticated)
        _termsAcceptedComplete = MutableStateFlow(prefs.termsAndConditionsAccepted)
        _privacyOnboardingComplete = MutableStateFlow(prefs.privacyOnboardingAcknowledged)
        _requireOnboardingAfterRegistration = MutableStateFlow(prefs.requireOnboardingAfterRegistration)
        _permissionOnboardingComplete = MutableStateFlow(prefs.permissionOnboardingAcknowledged)
    }

    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    val termsAcceptedComplete: StateFlow<Boolean> = _termsAcceptedComplete.asStateFlow()
    val privacyOnboardingComplete: StateFlow<Boolean> = _privacyOnboardingComplete.asStateFlow()
    val requireOnboardingAfterRegistration: StateFlow<Boolean> = _requireOnboardingAfterRegistration.asStateFlow()

    /**
     * Permission onboarding (deep-scan All-files-access disclosure). Shown once per device after
     * privacy onboarding, decoupled from [requireOnboardingAfterRegistration] so users who
     * upgrade also see it once.
     */
    val permissionOnboardingComplete: StateFlow<Boolean> = _permissionOnboardingComplete.asStateFlow()

    fun markAuthenticated() {
        prefs.isAuthenticated = true
        _isAuthenticated.value = true
        _requireOnboardingAfterRegistration.value = prefs.requireOnboardingAfterRegistration
    }

    fun logout() {
        prefs.clearAuthSession()
        _isAuthenticated.value = false
    }

    fun acknowledgeTermsAndConditions() {
        prefs.termsAndConditionsAccepted = true
        _termsAcceptedComplete.value = true
    }

    fun acknowledgePrivacyOnboarding() {
        prefs.privacyOnboardingAcknowledged = true
        _privacyOnboardingComplete.value = true
        prefs.requireOnboardingAfterRegistration = false
        _requireOnboardingAfterRegistration.value = false
    }

    fun acknowledgePermissionOnboarding() {
        prefs.permissionOnboardingAcknowledged = true
        _permissionOnboardingComplete.value = true
    }
}
