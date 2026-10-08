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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExamEntity
import com.example.data.model.ResultEntity
import com.example.data.model.StudentEntity
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
fun AnalyticsScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val allResults by viewModel.results.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: মেধা তালিকা ও পরিসংখান, 1: শিক্ষার্থী তুলনা

    var selectedExamId by remember {
        mutableStateOf(exams.firstOrNull()?.id ?: 0L)
    }

    val currentExam = remember(exams, selectedExamId) {
        exams.firstOrNull { it.id == selectedExamId } ?: exams.firstOrNull()
    }

    val examResults = remember(allResults, currentExam) {
        if (currentExam != null) {
            allResults.filter { it.examId == currentExam.id }.sortedByDescending { it.netMarks }
        } else emptyList()
    }

    val avgMarks = remember(examResults) {
        if (examResults.isNotEmpty()) examResults.map { it.netMarks }.average() else 0.0
    }
    val highestMarks = remember(examResults) {
        if (examResults.isNotEmpty()) examResults.maxOf { it.netMarks } else 0.0
    }
    val passRate = remember(examResults) {
        if (examResults.isNotEmpty()) (examResults.count { it.passed }.toDouble() / examResults.size) * 100.0 else 0.0
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
                        text = "অ্যানালাইসিস ও মেধা তালিকা",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "ফলাফল পরিসংখ্যান ও পারফরম্যান্স বিশ্লেষণ",
                        color = CyanAccent,
                        fontSize = 11.sp
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AmoledBlack,
                contentColor = CyanAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanAccent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "মেধা তালিকা ও পরিসংখ্যান",
                            color = if (selectedTab == 0) CyanAccent else TextTertiary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "শিক্ষার্থী তুলনা",
                            color = if (selectedTab == 1) CyanAccent else TextTertiary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Exam Dropdown selector
                var showDropdown by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
                            .clickable { showDropdown = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "নির্বাচিত পরীক্ষা", color = TextTertiary, fontSize = 10.sp)
                            Text(
                                text = currentExam?.name ?: "পরীক্ষা নির্বাচন করুন",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        Text(text = "বদলান ▼", color = CyanAccent, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(AmoledSurface)
                    ) {
                        exams.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text(exam.name, color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    selectedExamId = exam.id
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Overview stats card
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
                                    text = "শ্রেণি পরিসংখ্যান",
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    StatItem("মোট পরীক্ষার্থী", BengaliUtils.formatNumber(examResults.size, useBn), TextPrimary)
                                    StatItem("গড় নম্বর", BengaliUtils.formatDecimal(avgMarks, useBn), CyanAccent)
                                    StatItem("সর্বোচ্চ নম্বর", BengaliUtils.formatDecimal(highestMarks, useBn), EmeraldPass)
                                    StatItem("পাশের হার", "${BengaliUtils.formatDecimal(passRate, useBn, 1)}%", EmeraldPass)
                                }
                            }
                        }
                    }

                    // Score Distribution Chart
                    if (examResults.isNotEmpty()) {
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
                                        text = "নম্বর বণ্টন গ্রাফ (Score Distribution)",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ScoreDistributionBarChart(results = examResults)
                                }
                            }
                        }
                    }

                    // Leaderboard / Merit list Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "মেধা তালিকা (Leaderboard)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    if (examResults.isEmpty()) {
                        item {
                            Text(
                                text = "এই পরীক্ষার জন্য কোনো ফলাফল নেই",
                                color = TextTertiary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        itemsIndexed(examResults) { index, res ->
                            val rank = index + 1
                            LeaderboardRow(
                                rank = rank,
                                result = res,
                                useBn = useBn,
                                onClick = { onNavigate(Screen.ResultDetail(res.id)) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Tab 1: Compare 2 students
                StudentCompareView(
                    students = students,
                    allResults = allResults,
                    useBn = useBn
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    result: ResultEntity,
    useBn: Boolean,
    onClick: () -> Unit
) {
    val rankBadgeColor = when (rank) {
        1 -> AmberWarning // Gold
        2 -> Color(0xFFB0BEC5) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> AmoledCardBorder
    }

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
            // Rank Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (rank <= 3) rankBadgeColor.copy(alpha = 0.2f) else AmoledBlack)
                    .border(1.dp, rankBadgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = BengaliUtils.formatNumber(rank, useBn),
                    color = if (rank <= 3) rankBadgeColor else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = result.studentName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "রোল: ${BengaliUtils.formatNumber(result.roll, useBn)}",
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
                text = "${BengaliUtils.formatDecimal(result.percentage, useBn, 1)}%",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ScoreDistributionBarChart(results: List<ResultEntity>) {
    val bins = remember(results) {
        val b0 = results.count { it.percentage < 40 }
        val b1 = results.count { it.percentage in 40.0..59.99 }
        val b2 = results.count { it.percentage in 60.0..79.99 }
        val b3 = results.count { it.percentage >= 80 }
        listOf(
            Pair("<৪০%", b0),
            Pair("৪০-৫৯%", b1),
            Pair("৬০-৭৯%", b2),
            Pair("৮০%+", b3)
        )
    }

    val maxCount = remember(bins) { maxOf(1, bins.maxOf { it.second }) }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        val w = size.width
        val h = size.height
        val barWidth = w / (bins.size * 2f)

        bins.forEachIndexed { i, bin ->
            val barH = (bin.second.toFloat() / maxCount) * h
            val x = i * (w / bins.size) + (w / bins.size - barWidth) / 2f
            val y = h - barH

            val barColor = when (i) {
                0 -> CrimsonFail
                1 -> AmberWarning
                2 -> CyanAccent
                else -> EmeraldPass
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        bins.forEach { bin ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = bin.first, color = TextTertiary, fontSize = 10.sp)
                Text(text = "${bin.second} জন", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun StudentCompareView(
    students: List<StudentEntity>,
    allResults: List<ResultEntity>,
    useBn: Boolean
) {
    var studentA by remember { mutableStateOf(students.getOrNull(0)) }
    var studentB by remember { mutableStateOf(students.getOrNull(1)) }

    var showPickerA by remember { mutableStateOf(false) }
    var showPickerB by remember { mutableStateOf(false) }

    val resultsA = remember(studentA, allResults) {
        if (studentA != null) allResults.filter { it.roll == studentA?.roll } else emptyList()
    }
    val resultsB = remember(studentB, allResults) {
        if (studentB != null) allResults.filter { it.roll == studentB?.roll } else emptyList()
    }

    val avgA = remember(resultsA) { if (resultsA.isNotEmpty()) resultsA.map { it.netMarks }.average() else 0.0 }
    val avgB = remember(resultsB) { if (resultsB.isNotEmpty()) resultsB.map { it.netMarks }.average() else 0.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Picker Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student A Picker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledSurface)
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { showPickerA = true }
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "১ম শিক্ষার্থী", color = CyanAccent, fontSize = 10.sp)
                    Text(
                        text = studentA?.name ?: "নির্বাচন করুন",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                DropdownMenu(expanded = showPickerA, onDismissRequest = { showPickerA = false }, modifier = Modifier.background(AmoledSurface)) {
                    students.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} (${s.roll})", color = TextPrimary) },
                            onClick = {
                                studentA = s
                                showPickerA = false
                            }
                        )
                    }
                }
            }

            // Student B Picker
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledSurface)
                    .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { showPickerB = true }
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "২য় শিক্ষার্থী", color = AmberWarning, fontSize = 10.sp)
                    Text(
                        text = studentB?.name ?: "নির্বাচন করুন",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                DropdownMenu(expanded = showPickerB, onDismissRequest = { showPickerB = false }, modifier = Modifier.background(AmoledSurface)) {
                    students.forEach { s ->
                        DropdownMenuItem(
                            text = { Text("${s.name} (${s.roll})", color = TextPrimary) },
                            onClick = {
                                studentB = s
                                showPickerB = false
                            }
                        )
                    }
                }
            }
        }

        // Comparison Stats Cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AmoledSurface)
                .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "পারফরম্যান্স তুলনা", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                CompareMetricRow(
                    metricName = "পরীক্ষা দিয়েছে",
                    valA = "${BengaliUtils.formatNumber(resultsA.size, useBn)} টি",
                    valB = "${BengaliUtils.formatNumber(resultsB.size, useBn)} টি"
                )

                CompareMetricRow(
                    metricName = "গড় নেট নম্বর",
                    valA = BengaliUtils.formatDecimal(avgA, useBn),
                    valB = BengaliUtils.formatDecimal(avgB, useBn)
                )

                CompareMetricRow(
                    metricName = "সর্বোচ্চ প্রাপ্ত নম্বর",
                    valA = BengaliUtils.formatDecimal(resultsA.maxOfOrNull { it.netMarks } ?: 0.0, useBn),
                    valB = BengaliUtils.formatDecimal(resultsB.maxOfOrNull { it.netMarks } ?: 0.0, useBn)
                )

                CompareMetricRow(
                    metricName = "পাস করেছে",
                    valA = "${resultsA.count { it.passed }} টি",
                    valB = "${resultsB.count { it.passed }} টি"
                )
            }
        }
    }
}

@Composable
private fun CompareMetricRow(metricName: String, valA: String, valB: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = valA, color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(60.dp))
        Text(text = metricName, color = TextTertiary, fontSize = 12.sp)
        Text(text = valB, color = AmberWarning, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.width(60.dp))
    }
}
