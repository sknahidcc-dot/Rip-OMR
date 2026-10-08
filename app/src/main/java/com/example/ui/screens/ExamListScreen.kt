package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExamEntity
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BengaliUtils

@Composable
fun ExamListScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val selectedExam by viewModel.selectedExam.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    var examToDelete by remember { mutableStateOf<ExamEntity?>(null) }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                importError = null
            },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = "QR কোড / JSON ইমপোর্ট",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "অন্য ডিভাইস থেকে প্রাপ্ত পরীক্ষার QR কোড বা JSON টেক্সট পেস্ট করুন:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importError = null
                        },
                        placeholder = { Text("{\"v\":1,\"n\":\"...\",\"q\":100,...}", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3,
                        maxLines = 5
                    )
                    if (importError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = importError ?: "",
                            color = CrimsonFail,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importExamFromQr(importJsonText.trim())
                        if (success) {
                            showImportDialog = false
                            importJsonText = ""
                        } else {
                            importError = "অবৈধ JSON ফরম্যাট! সঠিক QR JSON দিন।"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Text("ইমপোর্ট করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    if (examToDelete != null) {
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            containerColor = AmoledSurface,
            title = { Text("পরীক্ষা মুছে ফেলবেন?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "\"${examToDelete?.name}\" মুছে ফেললে এর সকল পরীক্ষার ফলাফলও মুছে যাবে। আপনি কি নিশ্চিত?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        examToDelete?.let { viewModel.deleteExam(it) }
                        examToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonFail,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmoledSurface)
                                .border(1.dp, AmoledCardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "পরীক্ষা ব্যবস্থাপনা",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "মোট ${BengaliUtils.formatNumber(exams.size, useBn)}টি সংরক্ষিত পরীক্ষা",
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // QR Import button
                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledSurface,
                            contentColor = CyanAccent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Import QR",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR ইমপোর্ট", fontSize = 12.sp)
                    }
                }

                if (exams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "কোনো পরীক্ষা তৈরি করা হয়নি",
                                color = TextSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "নতুন পরীক্ষা তৈরি করতে নিচের '+' বোতাম চাপুন",
                                color = TextTertiary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(exams) { exam ->
                            val isActive = selectedExam?.id == exam.id
                            ExamCard(
                                exam = exam,
                                isActive = isActive,
                                useBn = useBn,
                                onSelect = { viewModel.selectExam(exam) },
                                onEdit = { onNavigate(Screen.ExamEdit(exam.id)) },
                                onDuplicate = { viewModel.duplicateExam(exam) },
                                onShareQr = { onNavigate(Screen.ExamQrShare(exam)) },
                                onDelete = { examToDelete = exam }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            // FAB: Add Exam
            FloatingActionButton(
                onClick = { onNavigate(Screen.ExamEdit(null)) },
                containerColor = CyanAccent,
                contentColor = AmoledBlack,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Exam",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ExamCard(
    exam: ExamEntity,
    isActive: Boolean,
    useBn: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onShareQr: () -> Unit,
    onDelete: () -> Unit
) {
    val filledAnswersCount = exam.answers.count { it in '1'..'4' || it in listOf('ক', 'খ', 'গ', 'ঘ') }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AmoledSurface)
            .border(
                1.dp,
                if (isActive) CyanAccent else AmoledCardBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanAccent)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "সক্রিয়",
                                color = AmoledBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = exam.date.ifEmpty { "তারিখ বিহীন" },
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                // Make active button
                if (!isActive) {
                    Text(
                        text = "সক্রিয় করুন ➔",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onSelect() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = exam.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    label = "প্রশ্ন",
                    value = BengaliUtils.formatNumber(exam.totalQuestions, useBn)
                )
                Badge(
                    label = "উত্তর সেট",
                    value = "${BengaliUtils.formatNumber(filledAnswersCount, useBn)}/${BengaliUtils.formatNumber(exam.totalQuestions, useBn)}"
                )
                Badge(
                    label = "নেগেটিভ",
                    value = "-${BengaliUtils.formatDecimal(exam.negativeMark, useBn)}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShareQr,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Share",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CrimsonFail,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AmoledBlack)
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", color = TextTertiary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}
