package com.example.omr

import android.graphics.PointF

object OmrLayout {
    const val PAGE_WIDTH_MM = 210.0f
    const val PAGE_HEIGHT_MM = 297.0f
    const val MARKER_SIZE_MM = 6.0f

    // Corner Alignment Marker Centers in mm
    val MARKER_TL = PointF(11.0f, 11.0f)
    val MARKER_TR = PointF(199.0f, 11.0f)
    val MARKER_BL = PointF(11.0f, 286.0f)
    val MARKER_BR = PointF(199.0f, 286.0f)

    const val BUBBLE_RADIUS_MM = 1.95f
    const val INNER_SAMPLE_RADIUS_MM = 1.30f
    const val RING_INNER_RADIUS_MM = 2.40f
    const val RING_OUTER_RADIUS_MM = 3.00f

    const val TOTAL_ROLL_DIGITS = 4
    const val TOTAL_QUESTIONS = 100

    val OPTION_LABELS = listOf("ক", "খ", "গ", "ঘ")
    val OPTION_LABELS_ENG = listOf("A", "B", "C", "D")

    /**
     * Get center coordinates of Roll bubble in mm
     * col: 0..3 (left to right)
     * digit: 0..9 (top to bottom)
     */
    fun getRollBubbleCenter(col: Int, digit: Int): PointF {
        val x = 20.0f + 12.0f * col
        val y = 56.0f + 5.2f * digit
        return PointF(x, y)
    }

    /**
     * Get center coordinates of Answer bubble in mm
     * question: 1..100
     * option: 0..3 (ক, খ, গ, ঘ)
     */
    fun getAnswerBubbleCenter(question: Int, option: Int): PointF {
        val qIndex = question - 1
        val block = qIndex / 25
        val row = qIndex % 25
        val x = 12.0f + 47.5f * block + 14.5f + 7.5f * option
        val y = 133.75f + 5.5f * row
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
