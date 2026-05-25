package com.safeguard.ui.screens.detailedanalysis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.core.domain.model.MalwareCategory
import com.safeguard.core.domain.model.Verdict
import com.safeguard.ui.theme.DangerRed
import com.safeguard.ui.theme.Dimensions
import com.safeguard.ui.theme.SafeGreen
import com.safeguard.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedAnalysisScreen(
    scanId: String,
    onBack: () -> Unit,
    viewModel: DetailedAnalysisViewModel = hiltViewModel()
) {
    LaunchedEffect(scanId) { viewModel.loadScan(scanId) }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Analysis", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val result = state.scanResult
        if (result == null) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(Dimensions.ScreenPadding)
            ) {
                Text(
                    "No scan data.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(Dimensions.ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            result.installerSource?.let { source ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = Dimensions.SectionSpacing),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Source: ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            source,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                "Risk Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            for (layer in result.layerResults) {
                val (title, riskLabel) = when (layer.layerId) {
                    1 -> "File / Hash" to verdictLabel(layer.verdict)
                    2 -> "Hash Check" to verdictLabel(layer.verdict)
                    3 -> "Permissions Risk" to verdictLabel(layer.verdict)
                    4 -> "Signature Check" to verdictLabel(layer.verdict)
                    5 -> "ML Analysis" to verdictLabel(layer.verdict)
                    6 -> "Cloud Verification" to verdictLabel(layer.verdict)
                    else -> layer.layerName to verdictLabel(layer.verdict)
                }
                RiskDetailCard(
                    title = title,
                    riskLabel = riskLabel,
                    detail = layer.evidence.firstOrNull() ?: layer.layerName,
                    showTrailingChevron = false
                )
                Spacer(Modifier.height(Dimensions.TightSpacing))
            }

            val pipelineSignals = result.aggregatedEvidence.filter { line ->
                line.startsWith("apk_") || line.startsWith("layers_") || line.startsWith("total_scan") ||
                    line.startsWith("pipeline_") || line.startsWith("slow_layers") || line.startsWith("trusted_")
            }.distinct()
            if (pipelineSignals.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.SectionSpacing))
                Text(
                    "Pipeline & integrity signals",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(Dimensions.CardPadding)) {
                        pipelineSignals.forEach { line ->
                            val pretty = line.replaceFirst("=", ": ").replace("_", " ")
                            Text(
                                pretty,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimensions.SectionSpacing))

            Text(
                "Explanation",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(Dimensions.CardPadding)) {
                    // 1. Forensic Taxonomy Badge & Summary
                    val threat = result.threatInfo
                    val displayCategory = when (result.finalVerdict) {
                        Verdict.SAFE -> MalwareCategory.CLEAN
                        Verdict.MALICIOUS ->
                            threat?.category?.takeIf { it != MalwareCategory.CLEAN } ?: MalwareCategory.UNSPECIFIED
                        Verdict.SUSPICIOUS ->
                            threat?.category?.takeIf { it != MalwareCategory.CLEAN } ?: MalwareCategory.RISKWARE
                        else ->
                            threat?.category?.takeIf { it != MalwareCategory.CLEAN } ?: MalwareCategory.UNSPECIFIED
                    }
                    val isMalicious = result.finalVerdict == Verdict.MALICIOUS
                    val headlineColor = if (isMalicious) DangerRed else if (result.finalVerdict == Verdict.SUSPICIOUS) WarningAmber else SafeGreen
                    
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Surface(
                            color = headlineColor.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                text = displayCategory.name.replace('_', ' '),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = headlineColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    val headlineText = if (result.finalVerdict == Verdict.MALICIOUS) {
                        "🚨 High Alert: This application exhibits malicious behavior and poses a severe threat."
                    } else if (result.finalVerdict == Verdict.SUSPICIOUS) {
                        "⚠️ Suspicious Activity: Indicators of risk detected. Handle with caution."
                    } else {
                        "✅ Secure: No significant threats were detected."
                    }
                    
                    Text(
                        text = headlineText,
                        style = MaterialTheme.typography.titleMedium,
                        color = headlineColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 2. Expert Forensic Reasoning
                    val reasons = threat?.technicalReasoning ?: emptyList()
                    if (reasons.isNotEmpty()) {
                        Text(
                            "Expert Forensic Insight:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        reasons.forEach { reason ->
                            androidx.compose.foundation.layout.Row(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text(
                                    "•",
                                    color = headlineColor,
                                    modifier = Modifier.padding(top = 2.dp, end = 8.dp)
                                )
                                Text(
                                    reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 3. Recommended Action
                    val advice = if (result.finalVerdict == Verdict.MALICIOUS) {
                        "We strongly recommend immediate quarantine to prevent data theft or system compromise."
                    } else if (result.finalVerdict == Verdict.SUSPICIOUS) {
                        "Consider the source's reputation. If in doubt, uninstall the app."
                    } else {
                        "Verified safe. Signature integrity and behavioral scans confirmed."
                    }

                    Text(
                        advice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))


            Spacer(Modifier.height(Dimensions.SectionSpacing))
        }
    }
}

@Composable
private fun RiskDetailCard(
    title: String,
    riskLabel: String,
    detail: String,
    showTrailingChevron: Boolean = false
) {
    val riskColor = when (riskLabel) {
        "High Risk", "Invalid", "Malicious" -> DangerRed
        "Suspicious", "Caution" -> WarningAmber
        else -> SafeGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(Dimensions.CardPadding)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        riskLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = riskColor
                    )
                }
                if (showTrailingChevron) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (detail.isNotBlank()) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun verdictLabel(v: Verdict): String = when (v) {
    Verdict.SAFE -> "Safe"
    Verdict.SUSPICIOUS -> "Suspicious"
    Verdict.MALICIOUS -> "High Risk"
    Verdict.UNKNOWN -> "Unknown"
}
