package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppConfig
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.model.Question

@Composable
fun FlagQuestionDialog(
    question: Question,
    onDismiss: () -> Unit,
    onSubmitReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    var customReason by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()
    val appConfig = LocalAppConfig.current
    
    val reasons = listOf(
        "Incorrect Answer",
        "Wrong Question Text",
        "Grammar/Typo Issues",
        "Options are confusing/repeated",
        "Irrelevant/Out of Syllabus",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF1E293B) else Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color(0XFFE11D48),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Report Question",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Why is this question incorrect or problematic?",
                    fontSize = appConfig.advancedFontSettings.explanationFontSize.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0XFFE11D48))
                            )
                            Text(
                                text = reason,
                                fontSize = appConfig.advancedFontSettings.optionFontSize.sp,
                                color = if (selectedReason == reason) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                                fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                
                if (selectedReason == "Other") {
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = { Text("Enter your reason here...", fontSize = appConfig.advancedFontSettings.explanationFontSize.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0XFFE11D48),
                            unfocusedBorderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalReason = if (selectedReason == "Other") customReason else selectedReason
                    if (finalReason.isNotBlank()) {
                        onSubmitReport(finalReason)
                    }
                },
                enabled = selectedReason.isNotBlank() && (selectedReason != "Other" || customReason.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0XFFE11D48)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit Report", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))
            }
        }
    )
}
