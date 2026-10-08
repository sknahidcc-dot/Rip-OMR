package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ResultEntity
import com.example.omr.OmrLayout
import com.example.ui.theme.AmberWarning
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
fun ResultDetailScreen(
    resultId: Long,
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val results by viewModel.results.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    val result = remember(results, resultId) {
        results.firstOrNull { it.id == resultId }
    }

    val exam = remember(exams, result) {
        if (result != null) exams.firstOrNull { it.id == result.examId } else null
    }

    var isQuestionListExpanded by remember { mutableStateOf(false) }

    if (result == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("ফলাফল খুঁজে পাওয়া যায়নি", color = TextSecondary)
        }
        return
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
                    Text(
                        text = "মূল্যায়ন ফলাফল",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        val reportText = buildResultReportText(result, useBn)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "ফলাফল শেয়ার করুন"))
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AmoledSurface)
                        .border(1.dp, CyanAccent.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pass/Fail Hero Card
                item {
                    ResultHeroCard(result = result, useBn = useBn)
                }

                // 6 Core Metrics Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "সঠিক উত্তর",
                                value = BengaliUtils.formatNumber(result.correct, useBn),
                                subtext = "+${BengaliUtils.formatDecimal(result.totalMarks, useBn)} নম্বর",
                                color = EmeraldPass,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "ভুল উত্তর",
                                value = BengaliUtils.formatNumber(result.wrong, useBn),
                                subtext = "-${BengaliUtils.formatDecimal(result.negativeTotal, useBn)} কাটা",
                                color = CrimsonFail,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "অনুত্তরিত",
                                value = BengaliUtils.formatNumber(result.unanswered, useBn),
                                subtext = "০ নম্বর",
                                color = TextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "নেট নম্বর",
                                value = BengaliUtils.formatDecimal(result.netMarks, useBn),
                                subtext = "চূড়ান্ত স্কোর",
                                color = CyanAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Collapsible Question Breakdown
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isQuestionListExpanded = !isQuestionListExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "প্রশ্নভিত্তিক বিস্তারিত ফলাফল",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "শিক্ষার্থীর উত্তর বনাম সঠিক উত্তরমালা",
                                        color = TextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (isQuestionListExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = CyanAccent
                                )
                            }

                            AnimatedVisibility(visible = isQuestionListExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val givenList = result.answersGiven.split(",")
                                    val examAnswers = exam?.answers ?: ""

                                    val totalQ = exam?.totalQuestions ?: givenList.size

                                    for (q in 1..totalQ) {
                                        val givenStr = givenList.getOrNull(q - 1) ?: ""
                                        val keyCh = examAnswers.getOrNull(q - 1) ?: '0'

                                        val keyOpt = when (keyCh) {
                                            '1', 'ক' -> 0
                                            '2', 'খ' -> 1
                                            '3', 'গ' -> 2
                                            '4', 'ঘ' -> 3
                                            else -> -1
                                        }

                                        val givenOpts = givenStr.split("+").mapNotNull { it.toIntOrNull() }

                                        val isCorrect = givenOpts.size == 1 && givenOpts[0] == keyOpt
                                        val isUnanswered = givenOpts.isEmpty()

                                        QuestionReviewRow(
                                            qNum = q,
                                            givenOpts = givenOpts,
                                            keyOpt = keyOpt,
                                            isCorrect = isCorrect,
                                            isUnanswered = isUnanswered,
                                            useBn = useBn
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(Screen.Home) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledSurface,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("হোম")
                        }

                        Button(
                            onClick = { onNavigate(Screen.Scanner) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = AmoledBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন স্ক্যান", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHeroCard(result: ResultEntity, useBn: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (result.passed) EmeraldPass.copy(alpha = 0.15f) else CrimsonFail.copy(alpha = 0.15f),
                        AmoledSurface
                    )
                )
            )
            .border(
                1.dp,
                if (result.passed) EmeraldPass.copy(alpha = 0.5f) else CrimsonFail.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = result.studentName,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "রোল: ${BengaliUtils.formatNumber(result.roll, useBn)} • ${result.examName}",
                color = CyanAccent,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Big Pass/Fail Badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(if (result.passed) EmeraldPass.copy(alpha = 0.2f) else CrimsonFail.copy(alpha = 0.2f))
                    .border(
                        2.5.dp,
                        if (result.passed) EmeraldPass else CrimsonFail,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (result.passed) "পাস" else "ফেইল",
                        color = if (result.passed) EmeraldPass else CrimsonFail,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${BengaliUtils.formatDecimal(result.percentage, useBn, 1)}%",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "প্রাপ্ত মোট নেট নম্বর: ${BengaliUtils.formatDecimal(result.netMarks, useBn)}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AmoledSurface)
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(text = title, color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuestionReviewRow(
    qNum: Int,
    givenOpts: List<Int>,
    keyOpt: Int,
    isCorrect: Boolean,
    isUnanswered: Boolean,
    useBn: Boolean
) {
    val givenLabel = if (givenOpts.isEmpty()) "ফাঁকা" else givenOpts.joinToString("+") { OmrLayout.optionIndexToBengali(it) }
    val keyLabel = OmrLayout.optionIndexToBengali(keyOpt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AmoledBlack)
            .border(1.dp, AmoledCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "প্রশ্ন ${BengaliUtils.formatNumber(qNum, useBn)}",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.width(60.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "উত্তর: ", color = TextTertiary, fontSize = 11.sp)
            Text(
                text = givenLabel,
                color = when {
                    isCorrect -> EmeraldPass
                    isUnanswered -> TextTertiary
                    else -> CrimsonFail
                },
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "সঠিক: ", color = TextTertiary, fontSize = 11.sp)
            Text(
                text = keyLabel,
                color = CyanAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Text(
            text = when {
                isCorrect -> "+১"
                isUnanswered -> "০"
                else -> "-নেগেটিভ"
            },
            color = when {
                isCorrect -> EmeraldPass
                isUnanswered -> TextTertiary
                else -> CrimsonFail
            },
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

private fun buildResultReportText(result: ResultEntity, useBn: Boolean): String {
    return """
        📋 RIP OMR মূল্যায়ন ফলাফল কার্ড
        ━━━━━━━━━━━━━━━━━━━━
        👨‍🎓 শিক্ষার্থী: ${result.studentName}
        🔢 রোল: ${BengaliUtils.formatNumber(result.roll, useBn)}
        📝 পরীক্ষা: ${result.examName}
        ━━━━━━━━━━━━━━━━━━━━
        ✅ সঠিক উত্তর: ${BengaliUtils.formatNumber(result.correct, useBn)}
        ❌ ভুল উত্তর: ${BengaliUtils.formatNumber(result.wrong, useBn)}
        ⚪ অনুত্তরিত: ${BengaliUtils.formatNumber(result.unanswered, useBn)}
        ━━━━━━━━━━━━━━━━━━━━
        🎯 মোট নেট নম্বর: ${BengaliUtils.formatDecimal(result.netMarks, useBn)}
        📊 শতকরা হার: ${BengaliUtils.formatDecimal(result.percentage, useBn, 1)}%
        🏆 ফলাফল: ${if (result.passed) "উত্তীর্ণ (PASSED)" else "অনুত্তীর্ণ (FAILED)"}
        ━━━━━━━━━━━━━━━━━━━━
        Powered by RIP OMR Scanner
    """.trimIndent()
}
