package com.safeguard.ui.screens.quarantine;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ButtonDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.SnackbarHostState;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.style.TextOverflow;
import com.safeguard.core.domain.repository.QuarantineRecord;
import com.safeguard.ui.theme.Dimensions;
import java.io.File;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\u001a$\u0010\u0000\u001a\u00020\u00012\u0010\b\u0002\u0010\u0002\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001aB\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u0003H\u0003\u001a\u0016\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0002\u001a\f\u0010\u0011\u001a\u00020\u0010*\u00020\bH\u0002\u00a8\u0006\u0012"}, d2 = {"QuarantineScreen", "", "onBack", "Lkotlin/Function0;", "viewModel", "Lcom/safeguard/ui/screens/quarantine/QuarantineViewModel;", "VaultHorizontalCard", "record", "Lcom/safeguard/core/domain/repository/QuarantineRecord;", "isExpanded", "", "onClick", "onDelete", "onInstallAnyway", "generateRiskFactors", "", "", "displayName", "app_debug"})
public final class QuarantineScreenKt {
    
    private static final java.util.List<java.lang.String> generateRiskFactors(com.safeguard.core.domain.repository.QuarantineRecord record) {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void QuarantineScreen(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.quarantine.QuarantineViewModel viewModel) {
    }
    
    private static final java.lang.String displayName(com.safeguard.core.domain.repository.QuarantineRecord $this$displayName) {
        return null;
    }
    
    @androidx.compose.runtime.Composable
    private static final void VaultHorizontalCard(com.safeguard.core.domain.repository.QuarantineRecord record, boolean isExpanded, kotlin.jvm.functions.Function0<kotlin.Unit> onClick, kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, kotlin.jvm.functions.Function0<kotlin.Unit> onInstallAnyway) {
    }
}