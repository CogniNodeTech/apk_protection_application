package com.safeguard.ui.theme;

import androidx.lifecycle.ViewModel;
import com.safeguard.data.local.preferences.SecurePreferencesManager;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fR\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0010"}, d2 = {"Lcom/safeguard/ui/theme/ThemeViewModel;", "Landroidx/lifecycle/ViewModel;", "preferences", "Lcom/safeguard/data/local/preferences/SecurePreferencesManager;", "(Lcom/safeguard/data/local/preferences/SecurePreferencesManager;)V", "_useDarkTheme", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "useDarkTheme", "Lkotlinx/coroutines/flow/StateFlow;", "getUseDarkTheme", "()Lkotlinx/coroutines/flow/StateFlow;", "setThemeMode", "", "mode", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ThemeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.safeguard.data.local.preferences.SecurePreferencesManager preferences = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _useDarkTheme = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> useDarkTheme = null;
    
    @javax.inject.Inject
    public ThemeViewModel(@org.jetbrains.annotations.NotNull
    com.safeguard.data.local.preferences.SecurePreferencesManager preferences) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getUseDarkTheme() {
        return null;
    }
    
    public final void setThemeMode(@org.jetbrains.annotations.NotNull
    java.lang.String mode) {
    }
}