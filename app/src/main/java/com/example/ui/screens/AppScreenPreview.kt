package com.example.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DynamicAppTheme
import com.example.ui.components.PremiumScreenSurface

@Preview(name = "Login Screen Preview", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun LoginScreenPreview() {
    DynamicAppTheme {
        PremiumScreenSurface {
            LoginScreen(
                onLoginSuccess = {},
                onBack = {},
                onNavigateToAdminDashboard = {}
            )
        }
    }
}

@Preview(name = "Simplified Basic Screen Preview", showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun BasicScreenPreview() {
    DynamicAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "APPSC & Competitive Exam Portal",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Jetpack Compose Preview Operational & Verified",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Explore Practice Arena")
                }
            }
        }
    }
}
