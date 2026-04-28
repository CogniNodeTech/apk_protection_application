package com.safeguard.ui.screens.detailedanalysis;

import androidx.compose.material.icons.Icons;
import androidx.compose.material3.CardDefaults;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import com.safeguard.core.domain.model.MalwareCategory;
import com.safeguard.core.domain.model.Verdict;
import com.safeguard.ui.theme.Dimensions;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u0007\u001a*\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\rH\u0003\u001a\u0010\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0010H\u0002\u00a8\u0006\u0011"}, d2 = {"DetailedAnalysisScreen", "", "scanId", "", "onBack", "Lkotlin/Function0;", "viewModel", "Lcom/safeguard/ui/screens/detailedanalysis/DetailedAnalysisViewModel;", "RiskDetailCard", "title", "riskLabel", "detail", "showTrailingChevron", "", "verdictLabel", "v", "Lcom/safeguard/core/domain/model/Verdict;", "app_debug"})
public final class DetailedAnalysisScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void DetailedAnalysisScreen(@org.jetbrains.annotations.NotNull
    java.lang.String scanId, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull
    com.safeguard.ui.screens.detailedanalysis.DetailedAnalysisViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    private static final void RiskDetailCard(java.lang.String title, java.lang.String riskLabel, java.lang.String detail, boolean showTrailingChevron) {
    }
    
    private static final java.lang.String verdictLabel(com.safeguard.core.domain.model.Verdict v) {
        return null;
    }
}