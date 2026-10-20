package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.model.LuxuryFontConfig

@Composable
fun LuxuryFontStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentConfig by LuxuryFontStudioManager.fontConfig.collectAsState()
    var tempConfig by remember(currentConfig) { mutableStateOf(currentConfig) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF7F2)).padding(16.dp)) {
        Text("Luxury Font Studio", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF292524))
        
        // Background Color Swatches
        Text("Background Colour", style = MaterialTheme.typography.titleMedium, color = Color(0xFF292524))
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(150.dp)) {
            items(BackgroundColors.toList()) { (name, hex) ->
                Box(modifier = Modifier.padding(4.dp).size(50.dp).background(Color(android.graphics.Color.parseColor(hex))).clickable {
                    tempConfig = tempConfig.copy(backgroundColor = hex)
                }, contentAlignment = Alignment.Center) {
                    if (tempConfig.backgroundColor == hex) Text("✓", color = Color.Black)
                }
            }
        }

        // Font Size Slider
        Text("Font Size: ${tempConfig.fontSizeSp.toInt()}sp", style = MaterialTheme.typography.titleMedium)
        Slider(value = tempConfig.fontSizeSp, onValueChange = { tempConfig = tempConfig.copy(fontSizeSp = it) }, valueRange = 10f..30f)

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { 
                LuxuryFontStudioManager.updateConfig(context, tempConfig)
                onBack()
            }) { Text("Apply") }
            Button(onClick = { tempConfig = LuxuryFontConfig() }) { Text("Reset") }
            Button(onClick = onBack) { Text("Cancel") }
        }
    }
}
