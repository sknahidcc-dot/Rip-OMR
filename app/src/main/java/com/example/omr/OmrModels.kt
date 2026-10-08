package com.example.omr

import android.graphics.Bitmap
import android.graphics.PointF

enum class AnswerStatus {
    CORRECT,
    WRONG,
    UNANSWERED,
    MULTIPLE
}

data class BubbleSample(
    val optionIndex: Int,
    val centerPixels: PointF,
    val fillRatio: Float,
    val isFilled: Boolean
)

data class QuestionDetection(
    val question: Int,
    val detectedOptions: List<Int>, // 0=ক, 1=খ, 2=গ, 3=ঘ
    val keyOption: Int, // 0..3 or -1 if not configured
    val status: AnswerStatus,
    val bubbleSamples: List<BubbleSample>
)

data class RollDetection(
    val column: Int,
    val detectedDigits: List<Int>,
    val finalDigit: Int?
)

data class OmrScanEvaluation(
    val roll: String,
    val isRollComplete: Boolean,
    val examId: Long,
    val examTitle: String,
    val totalQuestions: Int,
    val passPercent: Double,
    val negativeMark: Double,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val totalMarks: Double,
    val negativeMarks: Double,
    val netMarks: Double,
    val percentage: Double,
    val isPassed: Boolean,
    val questions: List<QuestionDetection>,
    val rollDetections: List<RollDetection>,
    val cornerMarkers: List<PointF>,
    val sheetBitmap: Bitmap
)
