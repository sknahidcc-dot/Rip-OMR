package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExamEntity
import com.example.data.model.ResultEntity
import com.example.ui.components.DeveloperInfoDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledDivider
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.AmoledSurfaceVariant
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
fun HomeScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit
) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val selectedExam by viewModel.selectedExam.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessingScan.collectAsStateWithLifecycle()
    val scanError by viewModel.scanErrorMessage.collectAsStateWithLifecycle()
    val useBnDigits by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    var showDevDialog by remember { mutableStateOf(false) }

    if (showDevDialog) {
        DeveloperInfoDialog(onDismiss = { showDevDialog = false })
    }

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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .border(1.dp, CyanAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Logo",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RIP OMR",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "স্মার্ট ওএমআর মূল্যায়নকারী",
                            color = CyanAccent,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigate(Screen.PrintableSheet) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Printable Sheet",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showDevDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer Info",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onNavigate(Screen.Settings) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Exam Card
                item {
                    ActiveExamBanner(
                        selectedExam = selectedExam ?: exams.firstOrNull(),
                        totalExams = exams.size,
                        useBn = useBnDigits,
                        onSwitchExam = { onNavigate(Screen.Exams) }
                    )
                }

                // Error Message banner if any
                if (scanError != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CrimsonFail.copy(alpha = 0.15f))
                                .border(1.dp, CrimsonFail, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = CrimsonFail,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scanError ?: "",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearScanError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("✕", color = CrimsonFail, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Primary Scan CTA Button (Big, Glowing)
                item {
                    BigScanButton(
                        isProcessing = isProcessing,
                        onClick = { onNavigate(Screen.Scanner) }
                    )
                }

                // Quick Demo Scan (1-Tap instant sample sheet evaluator)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.testWithSampleSheet(fillSample = true, sampleRoll = "1248")
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPass.copy(alpha = 0.15f))
                                    .border(1.dp, EmeraldPass, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test",
                                    tint = EmeraldPass,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "নমুনা শিট দিয়ে তাৎক্ষণিক টেস্ট",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "কাগজ ছাড়াই ১-ক্লিকে ওএমআর চেকিং যাচাই করুন",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "টেস্ট শুরু ➔",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Quick Menu Grid (2 columns)
                item {
                    Text(
                        text = "কার্যক্রম মেনু",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeNavCard(
                            title = "পরীক্ষা",
                            subtitle = "${BengaliUtils.formatNumber(exams.size, useBnDigits)}টি তৈরি করা আছে",
                            icon = Icons.Default.Assignment,
                            accentColor = CyanAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.Exams) }
                        )
                        HomeNavCard(
                            title = "শিক্ষার্থী",
                            subtitle = "${BengaliUtils.formatNumber(students.size, useBnDigits)} জন নিবন্ধিত",
                            icon = Icons.Default.People,
                            accentColor = AmberWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.Students) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HomeNavCard(
                            title = "অ্যানালাইসিস",
                            subtitle = "মেধা তালিকা ও পরিসংখ্যান",
                            icon = Icons.Default.Analytics,
                            accentColor = EmeraldPass,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.Analytics) }
                        )
                        HomeNavCard(
                            title = "ওএমআর শিট",
                            subtitle = "A4 অফিশিয়াল শিট দেখুন",
                            icon = Icons.Default.Description,
                            accentColor = Color(0xFF7C4DFF),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.PrintableSheet) }
                        )
                    }
                }

                // Recent Scans Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "সাম্প্রতিক স্ক্যান (${BengaliUtils.formatNumber(results.size, useBnDigits)})",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (results.isNotEmpty()) {
                            Text(
                                text = "সব দেখুন",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onNavigate(Screen.Analytics) }
                            )
                        }
                    }
                }

                if (results.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmoledSurface)
                                .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Empty",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "এখনও কোনো শিট স্ক্যান করা হয়নি",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "উপরের বোতামে চাপ দিয়ে প্রথম ওএমআর স্ক্যান করুন",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(results.take(5)) { result ->
                        RecentResultCard(
                            result = result,
                            useBn = useBnDigits,
                            onClick = { onNavigate(Screen.ResultDetail(result.id)) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveExamBanner(
    selectedExam: ExamEntity?,
    totalExams: Int,
    useBn: Boolean,
    onSwitchExam: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AmoledSurfaceVariant,
                        AmoledSurface
                    )
                )
            )
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
            .clickable { onSwitchExam() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldPass)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সক্রিয় পরীক্ষা",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAccent.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "বদলান",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedExam?.name ?: "কোনো পরীক্ষা নির্বাচন করা হয়নি",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedExam != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExamMiniBadge(
                        label = "প্রশ্ন",
                        value = BengaliUtils.formatNumber(selectedExam.totalQuestions, useBn)
                    )
                    ExamMiniBadge(
                        label = "পাস মার্ক",
                        value = "${BengaliUtils.formatNumber(selectedExam.passPercent.toInt(), useBn)}%"
                    )
                    ExamMiniBadge(
                        label = "নেগেটিভ",
                        value = "-${BengaliUtils.formatDecimal(selectedExam.negativeMark, useBn)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamMiniBadge(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AmoledBlack.copy(alpha = 0.6f))
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            color = TextTertiary,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun BigScanButton(
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        CyanAccent,
                        Color(0xFF00B0FF)
                    )
                )
            )
            .clickable(enabled = !isProcessing) { onClick() }
            .padding(vertical = 18.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AmoledBlack,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ওএমআর শিট বিশ্লেষণ হচ্ছে...",
                    color = AmoledBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AmoledBlack.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = AmoledBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ওএমআর শিট স্ক্যান করুন",
                        color = AmoledBlack,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ক্যামেরা বা গ্যালারি থেকে তাত্ক্ষণিক যাচাই",
                        color = AmoledBlack.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AmoledSurface)
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RecentResultCard(
    result: ResultEntity,
    useBn: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmoledSurface)
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (result.passed) "Pass" else "Fail",
                tint = if (result.passed) EmeraldPass else CrimsonFail,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.studentName,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(রোল: ${BengaliUtils.formatNumber(result.roll, useBn)})",
                        color = CyanAccent,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${result.examName} • সঠিক: ${BengaliUtils.formatNumber(result.correct, useBn)}",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${BengaliUtils.formatDecimal(result.netMarks, useBn)} নম্বর",
                color = if (result.passed) EmeraldPass else CrimsonFail,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (result.passed) "পাস" else "ফেইল",
                color = if (result.passed) EmeraldPass else CrimsonFail,
                fontSize = 11.sp
            )
        }
    }
}
