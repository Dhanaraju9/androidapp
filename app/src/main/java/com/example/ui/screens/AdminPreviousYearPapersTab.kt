package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PreviousYearPaper
import com.example.util.PreviousYearPaperStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPreviousYearPapersTab() {
    val context = LocalContext.current
    var papers by remember { mutableStateOf(PreviousYearPaperStore.getAllPapers(context)) }

    var jsonInput by remember {
        mutableStateOf(
            """{
  "appId": "${SampleData.getActiveApp().id}",
  "examName": "AP TET",
  "year": 2025,
  "examDate": "2025-06-15",
  "shift": "Shift 1",
  "language": "English",
  "subject": "Mathematics",
  "questions": [
    {
      "questionNumber": 1,
      "question": "What is the area of an equilateral triangle with side 4 cm?",
      "options": {
        "A": "4√3 cm²",
        "B": "2√3 cm²",
        "C": "8 cm²",
        "D": "16 cm²"
      },
      "correctAnswer": "A",
      "explanation": "Area = (√3 / 4) * side² = (√3 / 4) * 16 = 4√3 cm²."
    }
  ]
}"""
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var previewPaper by remember { mutableStateOf<PreviousYearPaper?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().use { r -> r.readText() }
                    jsonInput = content
                    val result = PreviousYearPaperStore.parseJsonImport(content)
                    if (result.isSuccess) {
                        previewPaper = result.getOrNull()
                        validationError = null
                        Toast.makeText(context, "✅ JSON parsed & validated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        validationError = result.exceptionOrNull()?.message ?: "Validation failed"
                        previewPaper = null
                    }
                }
            } catch (e: Exception) {
                validationError = "Error reading file: ${e.message}"
                previewPaper = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Previous Year Papers (PYP) Manager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Upload, validate, and tenant-map bulk previous year exam papers",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Button(
                onClick = { filePickerLauncher.launch("application/json") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import JSON File", fontSize = 12.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Paste Paper JSON (Bulk Support)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = {
                        jsonInput = it
                        val res = PreviousYearPaperStore.parseJsonImport(it)
                        if (res.isSuccess) {
                            previewPaper = res.getOrNull()
                            validationError = null
                        } else {
                            previewPaper = null
                            validationError = res.exceptionOrNull()?.message
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    placeholder = { Text("Paste JSON here...") }
                )

                if (validationError != null) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = validationError ?: "", fontSize = 11.5.sp, color = Color(0xFFDC2626))
                        }
                    }
                }

                if (previewPaper != null) {
                    val p = previewPaper!!
                    Surface(
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Validation Successful!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                            }
                            Text("Exam: ${p.examName} (${p.year}) | Shift: ${p.shift} | Lang: ${p.language}", fontSize = 11.5.sp, color = Color(0xFF166534))
                            Text("Target App ID: ${p.appId}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("Questions Loaded: ${p.questions.size}", fontSize = 11.5.sp, color = Color(0xFF166534))
                        }
                    }

                    Button(
                        onClick = {
                            PreviousYearPaperStore.savePaper(context, p)
                            papers = PreviousYearPaperStore.getAllPapers(context)
                            Toast.makeText(context, "🚀 PYP published successfully for app '${p.appId}'!", Toast.LENGTH_SHORT).show()
                            previewPaper = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm & Publish to App (${p.appId})")
                    }
                }
            }
        }

        Text(
            text = "Published Previous Year Papers (${papers.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        if (papers.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No previous year papers uploaded yet.", fontSize = 13.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            papers.forEach { paper ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${paper.examName} (${paper.year}) - ${paper.shift}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "AppID: ${paper.appId} | Lang: ${paper.language} | Questions: ${paper.questions.size}",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (paper.isPublished) Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = if (paper.isPublished) "Published" else "Draft",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (paper.isPublished) Color(0xFF166534) else Color(0xFF475569),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                PreviousYearPaperStore.togglePublish(context, paper.id)
                                papers = PreviousYearPaperStore.getAllPapers(context)
                            }) {
                                Text(if (paper.isPublished) "Unpublish" else "Publish", fontSize = 11.5.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    PreviousYearPaperStore.deletePaper(context, paper.id)
                                    papers = PreviousYearPaperStore.getAllPapers(context)
                                    Toast.makeText(context, "Paper deleted", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                            ) {
                                Text("Delete", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
