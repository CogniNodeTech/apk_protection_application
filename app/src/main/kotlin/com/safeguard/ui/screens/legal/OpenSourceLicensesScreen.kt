package com.safeguard.ui.screens.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.safeguard.ui.theme.Dimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("Loading…") }
    LaunchedEffect(Unit) {
        text = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open("open_source_licenses.txt").bufferedReader().use(BufferedReader::readText)
            }.getOrElse { "Could not load license file." }
        }
    }
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Open source licenses", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimensions.ScreenSidePadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
