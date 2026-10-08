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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExamEntity
import com.example.omr.OmrLayout
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import com.example.util.BengaliUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExamEditScreen(
    examId: Long?,
    viewModel: OmrViewModel,
    onBack: () -> Unit
) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    val existingExam = remember(exams, examId) {
        if (examId != null) exams.firstOrNull { it.id == examId } else null
    }

    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var name by remember { mutableStateOf(existingExam?.name ?: "নতুন পরীক্ষা") }
    var date by remember { mutableStateOf(existingExam?.date ?: currentDateStr) }
    var totalQuestions by remember { mutableIntStateOf(existingExam?.totalQuestions ?: 100) }
    var passPercent by remember { mutableStateOf(existingExam?.passPercent?.toString() ?: "80.0") }
    var negativeMark by remember { mutableStateOf(existingExam?.negativeMark?.toString() ?: "0.25") }

    // Map: question number 1..100 -> option 0..3 (or -1 if unset)
    val answerKeyMap = remember {
        val map = mutableMapOf<Int, Int>()
        if (existingExam != null) {
            val str = existingExam.answers
            for (i in 0 until existingExam.totalQuestions) {
                if (i < str.length) {
                    val ch = str[i]
                    val opt = when (ch) {
                        '1', 'ক' -> 0
                        '2', 'খ' -> 1
                        '3', 'গ' -> 2
                        '4', 'ঘ' -> 3
                        else -> -1
                    }
                    map[i + 1] = opt
                }
            }
        }
        mutableStateOf(map)
    }

    var showQuickTypeDialog by remember { mutableStateOf(false) }
    var quickTypeText by remember { mutableStateOf("") }

    if (showQuickTypeDialog) {
        AlertDialog(
            onDismissRequest = { showQuickTypeDialog = false },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = "দ্রুত উত্তরমালা টাইপ / পেস্ট",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "একসাথে একাধিক উত্তর পেস্ট করুন। যেমন: '12341234...' অথবা 'কখগঘকখগঘ...':",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = quickTypeText,
                        onValueChange = { quickTypeText = it },
                        placeholder = { Text("১=ক, ২=খ, ৩=গ, ৪=ঘ", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        minLines = 3,
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "টাইপ করা অক্ষর সংখ্যা: ${quickTypeText.length}",
                        color = CyanAccent,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentMap = answerKeyMap.value.toMutableMap()
                        var qIndex = 1
                        for (ch in quickTypeText) {
                            if (qIndex > totalQuestions) break
                            val opt = when (ch) {
                                '1', 'ক' -> 0
                                '2', 'খ' -> 1
                                '3', 'গ' -> 2
                                '4', 'ঘ' -> 3
                                else -> continue // Skip spaces/newlines
                            }
                            currentMap[qIndex] = opt
                            qIndex++
                        }
                        answerKeyMap.value = currentMap
                        showQuickTypeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Text("প্রয়োগ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickTypeDialog = false }) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    val filledCount = answerKeyMap.value.values.count { it in 0..3 }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AmoledBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
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
                    Text(
                        text = if (examId == null) "নতুন পরীক্ষা তৈরি" else "পরীক্ষা সম্পাদনা",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Save button
                Button(
                    onClick = {
                        val sb = StringBuilder()
                        for (q in 1..totalQuestions) {
                            val opt = answerKeyMap.value[q]
                            val digitChar = if (opt != null && opt in 0..3) (opt + 1).toString() else "0"
                            sb.append(digitChar)
                        }

                        val entity = ExamEntity(
                            id = examId ?: 0L,
                            name = name.ifBlank { "পরীক্ষা-$date" },
                            date = date,
                            totalQuestions = totalQuestions,
                            passPercent = passPercent.toDoubleOrNull() ?: 80.0,
                            negativeMark = negativeMark.toDoubleOrNull() ?: 0.25,
                            answers = sb.toString()
                        )
                        viewModel.saveExam(entity) {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সংরক্ষণ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Exam Basic Info Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "পরীক্ষার বিবরণ",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("পরীক্ষার নাম") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanAccent,
                                    unfocusedBorderColor = AmoledCardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = date,
                                    onValueChange = { date = it },
                                    label = { Text("তারিখ") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = AmoledCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = totalQuestions.toString(),
                                    onValueChange = {
                                        val num = it.toIntOrNull() ?: 1
                                        totalQuestions = num.coerceIn(1, 100)
                                    },
                                    label = { Text("মোট প্রশ্ন (১-১০০)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = AmoledCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = passPercent,
                                    onValueChange = { passPercent = it },
                                    label = { Text("পাস মার্ক %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = AmoledCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = negativeMark,
                                    onValueChange = { negativeMark = it },
                                    label = { Text("ভুল উত্তরের নেগেটিভ") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyanAccent,
                                        unfocusedBorderColor = AmoledCardBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Answer Key Header & Fast Tool Buttons
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "সঠিক উত্তরমালা (Answer Key)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "সেট করা হয়েছে: ${BengaliUtils.formatNumber(filledCount, useBn)} / ${BengaliUtils.formatNumber(totalQuestions, useBn)} টি",
                                    color = if (filledCount == totalQuestions) EmeraldPass else AmberWarning,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showQuickTypeDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmoledSurface,
                                        contentColor = CyanAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                ) {
                                    Text("দ্রুত টাইপ", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = {
                                        // Auto-generate sample pattern
                                        val current = answerKeyMap.value.toMutableMap()
                                        for (i in 1..totalQuestions) {
                                            current[i] = (i - 1) % 4
                                        }
                                        answerKeyMap.value = current
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AmoledSurface)
                                        .border(1.dp, AmoledCardBorder, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Fill All",
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        answerKeyMap.value = mutableMapOf()
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AmoledSurface)
                                        .border(1.dp, AmoledCardBorder, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear All",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Answer Key Rows: 1..totalQuestions
                items(totalQuestions) { index ->
                    val qNum = index + 1
                    val selectedOption = answerKeyMap.value[qNum] ?: -1

                    AnswerKeyRow(
                        questionNumber = qNum,
                        selectedOption = selectedOption,
                        useBn = useBn,
                        onSelectOption = { opt ->
                            val current = answerKeyMap.value.toMutableMap()
                            if (current[qNum] == opt) {
                                current.remove(qNum) // Toggle off if clicked again
                            } else {
                                current[qNum] = opt
                            }
                            answerKeyMap.value = current
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun AnswerKeyRow(
    questionNumber: Int,
    selectedOption: Int,
    useBn: Boolean,
    onSelectOption: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AmoledSurface)
            .border(
                1.dp,
                if (selectedOption != -1) AmoledCardBorder else AmoledCardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Question number
        Text(
            text = "প্রশ্ন ${BengaliUtils.formatNumber(questionNumber, useBn)}.",
            color = if (selectedOption != -1) TextPrimary else TextTertiary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.width(72.dp)
        )

        // 4 Option chips: ক, খ, গ, ঘ
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (opt in 0..3) {
                val isSelected = selectedOption == opt
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) CyanAccent else AmoledBlack)
                        .border(
                            1.dp,
                            if (isSelected) CyanAccent else AmoledCardBorder,
                            CircleShape
                        )
                        .clickable { onSelectOption(opt) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = OmrLayout.OPTION_LABELS[opt],
                        color = if (isSelected) AmoledBlack else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
