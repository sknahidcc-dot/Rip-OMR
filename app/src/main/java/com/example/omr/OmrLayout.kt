package com.example.omr

import android.graphics.PointF

object OmrLayout {
    const val PAGE_WIDTH_MM = 210.0f
    const val PAGE_HEIGHT_MM = 297.0f

    // 4 Corner Alignment Marker Centers in mm (Solid Black 9mm x 7mm)
    val MARKER_TL = PointF(12.5f, 11.5f)
    val MARKER_TR = PointF(197.5f, 11.5f)
    val MARKER_BL = PointF(12.5f, 285.5f)
    val MARKER_BR = PointF(197.5f, 285.5f)

    const val MARKER_WIDTH_MM = 9.0f
    const val MARKER_HEIGHT_MM = 7.0f

    const val BUBBLE_RADIUS_MM = 2.0f
    const val TOTAL_ROLL_DIGITS = 4
    const val TOTAL_QUESTIONS = 100

    // 4 Options: ক, খ, গ, ঘ (RIP-100-4 Standard)
    val OPTION_LABELS = listOf("ক", "খ", "গ", "ঘ")
    val OPTION_LABELS_ENG = listOf("A", "B", "C", "D")

    // Roll Number Section Coordinates
    const val ROLL_START_X = 138.0f
    const val ROLL_STEP_X = 16.0f
    const val ROLL_START_Y = 54.0f
    const val ROLL_STEP_Y = 5.5f

    /**
     * Center coordinates of Roll bubble in mm
     * col: 0..3 (left to right)
     * digit: 0..9 (top to bottom)
     */
    fun getRollBubbleCenter(col: Int, digit: Int): PointF {
        val x = ROLL_START_X + ROLL_STEP_X * col.coerceIn(0, 3)
        val y = ROLL_START_Y + ROLL_STEP_Y * digit.coerceIn(0, 9)
        return PointF(x, y)
    }

    // 4-Column Layout Coordinates in mm
    // Col 0: Q 1..25, Col 1: Q 26..50, Col 2: Q 51..75, Col 3: Q 76..100
    const val COL_WIDTH_MM = 45.5f
    const val COL_GAP_MM = 2.5f
    val COL_X_OFFSETS = floatArrayOf(
        10.0f,
        10.0f + 45.5f + 2.5f,        // 58.0f
        10.0f + (45.5f + 2.5f) * 2f, // 106.0f
        10.0f + (45.5f + 2.5f) * 3f  // 154.0f
    )

    const val TABLE_TOP_Y = 114.0f
    const val HEADER_HEIGHT_MM = 7.5f
    const val ROW_HEIGHT_MM = 5.9f
    const val BUBBLE_START_OFFSET_X = 15.0f
    const val BUBBLE_STEP_X = 8.0f

    /**
     * Center coordinates of Answer bubble in mm
     * question: 1..100
     * option: 0..3 (ক, খ, গ, ঘ)
     */
    fun getAnswerBubbleCenter(question: Int, option: Int): PointF {
        val qIndex = (question - 1).coerceIn(0, 99)
        val colIndex = qIndex / 25
        val rowIndex = qIndex % 25

        val colLeft = COL_X_OFFSETS[colIndex]
        val x = colLeft + BUBBLE_START_OFFSET_X + BUBBLE_STEP_X * option.coerceIn(0, 3)
        val y = TABLE_TOP_Y + HEADER_HEIGHT_MM + ROW_HEIGHT_MM * rowIndex + (ROW_HEIGHT_MM / 2f)
        return PointF(x, y)
    }

    fun parseOptionIndex(char: Char): Int {
        return when (char) {
            'ক', '1', 'A', 'a' -> 0
            'খ', '2', 'B', 'b' -> 1
            'গ', '3', 'C', 'c' -> 2
            'ঘ', '4', 'D', 'd' -> 3
            else -> -1
        }
    }

    fun optionIndexToBengali(index: Int): String {
        return if (index in OPTION_LABELS.indices) OPTION_LABELS[index] else "-"
    }

    fun optionIndexToDigit(index: Int): String {
        return if (index in 0..3) (index + 1).toString() else "0"
    }
}
