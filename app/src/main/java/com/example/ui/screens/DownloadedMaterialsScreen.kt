package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppLuminousBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedMaterialsScreen(
    onBack: () -> Unit
) {
    AppLuminousBackground(screenName = "Downloaded Materials") {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Downloaded Materials") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            // Re-use back icon if possible or just use Icons.Default.ArrowBack
                            Text("Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No materials downloaded yet.")
            }
        }
    }
}
