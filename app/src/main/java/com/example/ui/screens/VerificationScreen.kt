package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.omr.AnswerStatus
import com.example.omr.OmrScanEvaluation
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AmoledCardBorder
import com.example.ui.theme.AmoledSurface
import com.example.ui.theme.BlueInfo
import com.example.ui.theme.CrimsonFail
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.OmrViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BengaliUtils
import kotlin.math.roundToInt

@Composable
fun VerificationScreen(
    viewModel: OmrViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val evaluation by viewModel.currentEvaluation.collectAsStateWithLifecycle()
    val existingConflict by viewModel.existingResultPrompt.collectAsStateWithLifecycle()
    val useBn by viewModel.useBengaliDigits.collectAsStateWithLifecycle()

    var showRollEditDialog by remember { mutableStateOf(false) }
    var manualRollInput by remember { mutableStateOf(evaluation?.roll?.replace("?", "0") ?: "0000") }

    // Roll prompt dialog if incomplete
    if (showRollEditDialog) {
        AlertDialog(
            onDismissRequest = { showRollEditDialog = false },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = "রোল নম্বর যাচাই ও সংশোধন",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "শিট থেকে পঠিত রোল: ${evaluation?.roll ?: ""}\nঅনুগ্রহ করে ৪-সংখ্যার সঠিক রোল নিশ্চিত করুন:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualRollInput,
                        onValueChange = {
                            if (it.length <= 4) manualRollInput = it
                        },
                        label = { Text("৪-সংখ্যার রোল (Roll No)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = AmoledCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formattedRoll = manualRollInput.padStart(4, '0')
                        viewModel.updateRollInCurrentScan(formattedRoll)
                        showRollEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Text("নিশ্চিত করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRollEditDialog = false }) {
                    Text("বাতিল", color = TextSecondary)
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    // Existing Result Conflict Dialog
    if (existingConflict != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelResultConflict() },
            containerColor = AmoledSurface,
            title = {
                Text(
                    text = "পূর্ববর্তী ফলাফল বিদ্যমান",
                    color = AmberWarning,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "শিক্ষার্থী ${evaluation?.roll ?: ""} ইতিমধ্যে এই পরীক্ষায় অংশগ্রহণ করেছে (নম্বর: ${existingConflict?.netMarks})। আপনি কি ফলাফল প্রতিস্থাপন করতে চান নাকি দুটিই রাখবেন?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmOverwriteExistingResult { savedId ->
                            onNavigate(Screen.ResultDetail(savedId))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    )
                ) {
                    Text("প্রতিস্থাপন (Overwrite)")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.confirmKeepBothResults { savedId ->
                                onNavigate(Screen.ResultDetail(savedId))
                            }
                        }
                    ) {
                        Text("দুটিই রাখুন", color = EmeraldPass)
                    }
                    TextButton(onClick = { viewModel.cancelResultConflict() }) {
                        Text("বাতিল", color = TextSecondary)
                    }
                }
            },
            modifier = Modifier.border(1.dp, AmoledCardBorder, RoundedCornerShape(20.dp))
        )
    }

    val currentEval = evaluation
    if (currentEval == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("কোনো ওএমআর স্ক্যান ডেটা পাওয়া যায়নি", color = TextSecondary)
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
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            text = "ওএমআর শিট যাচাইকরণ",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "পিঞ্চ করে জুম করুন • নির্ভুলতা নিশ্চিত করুন",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Roll edit chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentEval.isRollComplete) AmoledSurface else AmberWarning.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            if (currentEval.isRollComplete) CyanAccent else AmberWarning,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            manualRollInput = currentEval.roll.replace("?", "0")
                            showRollEditDialog = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "রোল: ${currentEval.roll}",
                        color = if (currentEval.isRollComplete) CyanAccent else AmberWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Roll",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Summary Bar (Correct, Wrong, Unanswered, Marks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledSurface)
                    .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(label = "সঠিক", count = BengaliUtils.formatNumber(currentEval.correctCount, useBn), color = EmeraldPass)
                StatusBadge(label = "ভুল", count = BengaliUtils.formatNumber(currentEval.wrongCount, useBn), color = CrimsonFail)
                StatusBadge(label = "অনুত্তরিত", count = BengaliUtils.formatNumber(currentEval.unansweredCount, useBn), color = TextTertiary)
                StatusBadge(label = "নম্বর", count = BengaliUtils.formatDecimal(currentEval.netMarks, useBn), color = CyanAccent)
                StatusBadge(
                    label = "ফলাফল",
                    count = if (currentEval.isPassed) "পাস" else "ফেইল",
                    color = if (currentEval.isPassed) EmeraldPass else CrimsonFail
                )
            }

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = EmeraldPass, label = "সঠিক")
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(color = CrimsonFail, label = "ভুল/একাধিক")
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(color = BlueInfo, label = "সঠিক উত্তর")
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(color = Color.Gray, label = "অনুত্তরিত")
            }

            // Zoomable Interactive Sheet Canvas with Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AmoledSurface)
                    .border(1.dp, AmoledCardBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                ZoomableOmrSheet(evaluation = currentEval)
            }

            // Bottom Actions (Save Result, Rescan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rescan
                Button(
                    onClick = { onNavigate(Screen.Scanner) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmoledSurface,
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পুনরায় স্ক্যান")
                }

                // Save & View Result
                Button(
                    onClick = {
                        viewModel.checkAndSaveResult(
                            onSuccess = { savedId ->
                                onNavigate(Screen.ResultDetail(savedId))
                            },
                            onRollPromptNeeded = { currentRoll ->
                                manualRollInput = currentRoll.replace("?", "0")
                                showRollEditDialog = true
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = AmoledBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সংরক্ষণ ও ফলাফল", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 10.sp)
        Text(text = count, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun ZoomableOmrSheet(evaluation: OmrScanEvaluation) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val bitmap = remember(evaluation) { evaluation.sheetBitmap }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                    offset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val imgW = bitmap.width.toFloat()
            val imgH = bitmap.height.toFloat()

            // Calculate base fit scale
            val fitScale = minOf(canvasW / imgW, canvasH / imgH)
            val baseW = imgW * fitScale
            val baseH = imgH * fitScale

            val centerX = canvasW / 2f + offset.x
            val centerY = canvasH / 2f + offset.y

            val drawW = baseW * scale
            val drawH = baseH * scale

            val drawLeft = centerX - drawW / 2f
            val drawTop = centerY - drawH / 2f

            // 1. Draw base sheet image
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(drawLeft.roundToInt(), drawTop.roundToInt()),
                dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
            )

            // 2. Overlay detection bubbles
            val pxScale = drawW / imgW

            for (q in evaluation.questions) {
                for (sample in q.bubbleSamples) {
                    val bubbleCenterPx = sample.centerPixels
                    val screenX = drawLeft + bubbleCenterPx.x * pxScale
                    val screenY = drawTop + bubbleCenterPx.y * pxScale
                    val bubbleRadius = 4.5f * pxScale * scale

                    if (sample.isFilled) {
                        // Color based on status
                        val overlayColor = when (q.status) {
                            AnswerStatus.CORRECT -> Color(0xFF00E676)
                            AnswerStatus.WRONG, AnswerStatus.MULTIPLE -> Color(0xFFFF1744)
                            AnswerStatus.UNANSWERED -> Color.Gray
                        }

                        drawCircle(
                            color = overlayColor,
                            radius = bubbleRadius,
                            center = Offset(screenX, screenY),
                            style = Stroke(width = 2.dp.toPx() * scale.coerceAtMost(2f))
                        )
                    } else if (q.status != AnswerStatus.CORRECT && sample.optionIndex == q.keyOption && q.keyOption != -1) {
                        // Highlight the correct key in Blue so user can visually verify
                        drawCircle(
                            color = Color(0xFF2979FF),
                            radius = bubbleRadius,
                            center = Offset(screenX, screenY),
                            style = Stroke(width = 1.8.dp.toPx() * scale.coerceAtMost(2f))
                        )
                    }
                }
            }
        }
    }
}
