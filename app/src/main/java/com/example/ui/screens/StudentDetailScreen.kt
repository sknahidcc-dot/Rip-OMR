package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ResultEntity
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
fun StudentDetailScreen(
    roll: String,
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allResults by viewModel.results.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    val student = remember(students, roll) {
        students.firstOrNull { it.roll == roll }
    }

    val studentResults = remember(allResults, roll) {
        allResults.filter { it.roll == roll }.sortedBy { it.scannedAt }
    }

    val avgMarks = remember(studentResults) {
        if (studentResults.isNotEmpty()) studentResults.map { it.netMarks }.average() else 0.0
    }
    val bestMarks = remember(studentResults) {
        if (studentResults.isNotEmpty()) studentResults.maxOf { it.netMarks } else 0.0
    }
    val passCount = remember(studentResults) {
        studentResults.count { it.passed }
    }

    // Trend calculation
    val trendDiff = remember(studentResults) {
        if (studentResults.size >= 2) {
            val last = studentResults[studentResults.size - 1].percentage
            val prev = studentResults[studentResults.size - 2].percentage
            last - prev
        } else null
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
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = student?.name ?: "শিক্ষার্থী বিবরণ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "রোল: ${BengaliUtils.formatNumber(roll, useBn)}",
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Overview metrics card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MiniStat(
                                    label = "মোট পরীক্ষা",
                                    value = BengaliUtils.formatNumber(studentResults.size, useBn),
                                    color = TextPrimary
                                )
                                MiniStat(
                                    label = "গড় নম্বর",
                                    value = BengaliUtils.formatDecimal(avgMarks, useBn),
                                    color = CyanAccent
                                )
                                MiniStat(
                                    label = "সর্বোচ্চ",
                                    value = BengaliUtils.formatDecimal(bestMarks, useBn),
                                    color = EmeraldPass
                                )
                                MiniStat(
                                    label = "পাশের হার",
                                    value = if (studentResults.isNotEmpty()) {
                                        "${BengaliUtils.formatNumber((passCount * 100 / studentResults.size), useBn)}%"
                                    } else "০%",
                                    color = EmeraldPass
                                )
                            }

                            if (trendDiff != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (trendDiff >= 0) EmeraldPass.copy(alpha = 0.12f) else CrimsonFail.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (trendDiff >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = "Trend",
                                        tint = if (trendDiff >= 0) EmeraldPass else CrimsonFail,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (trendDiff >= 0) {
                                            "পূর্ববর্তী পরীক্ষার তুলনায় +${BengaliUtils.formatDecimal(trendDiff, useBn, 1)}% উন্নতি হয়েছে!"
                                        } else {
                                            "পূর্ববর্তী পরীক্ষার চেয়ে ${BengaliUtils.formatDecimal(trendDiff, useBn, 1)}% কমেছে"
                                        },
                                        color = if (trendDiff >= 0) EmeraldPass else CrimsonFail,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Progress Trend Line Chart
                if (studentResults.size >= 2) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AmoledSurface)
                                .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "অগ্রগতির রেখাচিত্র (Percentage %)",
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                StudentProgressChart(results = studentResults)
                            }
                        }
                    }
                }

                // Exam History List
                item {
                    Text(
                        text = "সকল পরীক্ষার ফলাফল (${BengaliUtils.formatNumber(studentResults.size, useBn)})",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (studentResults.isEmpty()) {
                    item {
                        Text(
                            text = "এই শিক্ষার্থী এখনও কোনো পরীক্ষায় অংশগ্রহণ করেনি",
                            color = TextTertiary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(studentResults.reversed()) { res ->
                        StudentResultHistoryCard(
                            result = res,
                            useBn = useBn,
                            onClick = { onNavigate(Screen.ResultDetail(res.id)) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun StudentProgressChart(results: List<ResultEntity>) {
    val percentages = remember(results) { results.map { it.percentage.toFloat().coerceIn(0f, 100f) } }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        val w = size.width
        val h = size.height

        val stepX = if (percentages.size > 1) w / (percentages.size - 1) else w
        val path = Path()

        val points = percentages.mapIndexed { idx, pct ->
            val x = idx * stepX
            val y = h - (pct / 100f) * h
            Offset(x, y)
        }

        points.forEachIndexed { i, pt ->
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }

        // Draw guideline at 80% pass mark
        val passY = h - 0.8f * h
        drawLine(
            color = Color.DarkGray,
            start = Offset(0f, passY),
            end = Offset(w, passY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw line
        drawPath(
            path = path,
            color = CyanAccent,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw dots
        points.forEach { pt ->
            drawCircle(
                color = AmoledBlack,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = CyanAccent,
                radius = 3.5.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun StudentResultHistoryCard(
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = result.examName,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "সঠিক: ${BengaliUtils.formatNumber(result.correct, useBn)} • ভুল: ${BengaliUtils.formatNumber(result.wrong, useBn)} • অনুত্তরিত: ${BengaliUtils.formatNumber(result.unanswered, useBn)}",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${BengaliUtils.formatDecimal(result.netMarks, useBn)} নম্বর",
                color = if (result.passed) EmeraldPass else CrimsonFail,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = if (result.passed) "পাস (${BengaliUtils.formatDecimal(result.percentage, useBn, 1)}%)" else "ফেইল",
                color = if (result.passed) EmeraldPass else CrimsonFail,
                fontSize = 11.sp
            )
        }
    }
}
