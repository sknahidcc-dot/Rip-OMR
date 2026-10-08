package com.example.omr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.random.Random

object OmrSheetGenerator {

    /**
     * Generates a high-resolution A4 bitmap of the official OMR sheet.
     * scaleDpi: standard 150 dpi or 200 dpi (200 dpi -> 1654 x 2338 px)
     * if fillSampleAnswers is true, fills in a sample student roll and 100 answers for testing.
     */
    fun generateSheet(
        scaleDpi: Int = 150,
        fillSampleAnswers: Boolean = false,
        sampleRoll: String = "1248",
        sampleAnswers: Map<Int, Int>? = null // question 1..100 -> option 0..3
    ): Bitmap {
        // 25.4 mm = 1 inch
        val pxPerMm = scaleDpi / 25.4f
        val widthPx = (OmrLayout.PAGE_WIDTH_MM * pxPerMm).toInt()
        val heightPx = (OmrLayout.PAGE_HEIGHT_MM * pxPerMm).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Clean white page
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 1. Draw 4 solid black corner alignment markers (6mm x 6mm squares)
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        // TL: (8, 8) to (14, 14) -> center (11, 11)
        canvas.drawRect(
            8f * pxPerMm, 8f * pxPerMm,
            14f * pxPerMm, 14f * pxPerMm,
            paint
        )
        // TR: (196, 8) to (202, 8) -> center (199, 11)
        canvas.drawRect(
            196f * pxPerMm, 8f * pxPerMm,
            202f * pxPerMm, 14f * pxPerMm,
            paint
        )
        // BL: (8, 283) to (14, 289) -> center (11, 286)
        canvas.drawRect(
            8f * pxPerMm, 283f * pxPerMm,
            14f * pxPerMm, 289f * pxPerMm,
            paint
        )
        // BR: (196, 283) to (202, 289) -> center (199, 286)
        canvas.drawRect(
            196f * pxPerMm, 283f * pxPerMm,
            202f * pxPerMm, 289f * pxPerMm,
            paint
        )

        // 2. Header and Instructions (Bengali)
        textPaint.textSize = 6.0f * pxPerMm
        textPaint.color = Color.BLACK
        canvas.drawText("RIP ওএমআর মূল্যায়ন শিট (A4)", (widthPx / 2).toFloat(), 20f * pxPerMm, textPaint)

        textPaint.textSize = 3.2f * pxPerMm
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("কালো বা নীল বলপেন দিয়ে বৃত্ত সম্পূর্ণ ভরাট করুন • কোনো ভাঁজ করবেন না", (widthPx / 2).toFloat(), 26f * pxPerMm, textPaint)

        // 3. Roll Number Section
        // Header
        textPaint.textSize = 3.5f * pxPerMm
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("রোল নম্বর (ROLL NO):", 18f * pxPerMm, 44f * pxPerMm, textPaint)

        // Roll digit header boxes
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.35f * pxPerMm
        paint.color = Color.DKGRAY

        for (col in 0..3) {
            val cx = (20f + 12f * col) * pxPerMm
            val boxLeft = cx - 4.5f * pxPerMm
            val boxRight = cx + 4.5f * pxPerMm
            val boxTop = 46f * pxPerMm
            val boxBottom = 52f * pxPerMm
            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint)

            if (fillSampleAnswers && col < sampleRoll.length) {
                textPaint.textSize = 3.8f * pxPerMm
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(sampleRoll[col].toString(), cx, 50.5f * pxPerMm, textPaint)
            }
        }

        // Draw Roll Bubbles (digits 0..9)
        val bubbleRadiusPx = OmrLayout.BUBBLE_RADIUS_MM * pxPerMm
        for (col in 0..3) {
            for (digit in 0..9) {
                val center = OmrLayout.getRollBubbleCenter(col, digit)
                val cx = center.x * pxPerMm
                val cy = center.y * pxPerMm

                // Draw bubble outline
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.3f * pxPerMm
                paint.color = Color.GRAY
                canvas.drawCircle(cx, cy, bubbleRadiusPx, paint)

                // Digit inside bubble
                textPaint.textSize = 2.4f * pxPerMm
                textPaint.color = Color.GRAY
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(digit.toString(), cx, cy + 0.8f * pxPerMm, textPaint)

                // Fill sample roll if requested
                if (fillSampleAnswers && col < sampleRoll.length && sampleRoll[col].digitToIntOrNull() == digit) {
                    drawFilledBubble(canvas, cx, cy, bubbleRadiusPx, pxPerMm)
                }
            }
        }

        // 4. Questions & Answer Bubbles (100 questions, 4 blocks of 25)
        // Alternating row background bands for easy scanning
        val bandPaint = Paint().apply {
            color = Color.rgb(245, 246, 248)
            style = Paint.Style.FILL
        }

        for (b in 0..3) {
            val blockLeft = (12f + 47.5f * b) * pxPerMm
            val blockRight = blockLeft + 45f * pxPerMm

            // Block header
            textPaint.textSize = 3.0f * pxPerMm
            textPaint.color = Color.BLACK
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val startQ = b * 25 + 1
            val endQ = (b + 1) * 25
            canvas.drawText("প্রশ্ন $startQ - $endQ", blockLeft, 126f * pxPerMm, textPaint)

            // Column labels ক, খ, গ, ঘ
            textPaint.textSize = 2.4f * pxPerMm
            textPaint.textAlign = Paint.Align.CENTER
            for (k in 0..3) {
                val optX = (12f + 47.5f * b + 14.5f + 7.5f * k) * pxPerMm
                canvas.drawText(OmrLayout.OPTION_LABELS[k], optX, 130f * pxPerMm, textPaint)
            }

            for (r in 0..24) {
                val qNum = b * 25 + r + 1
                val rowY = (133.75f + 5.5f * r) * pxPerMm

                // Draw alternating subtle row band
                if (r % 2 == 1) {
                    canvas.drawRect(
                        blockLeft - 2f * pxPerMm,
                        rowY - 2.5f * pxPerMm,
                        blockRight + 2f * pxPerMm,
                        rowY + 2.5f * pxPerMm,
                        bandPaint
                    )
                }

                // Question number
                textPaint.textSize = 2.4f * pxPerMm
                textPaint.color = Color.DKGRAY
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.typeface = Typeface.DEFAULT
                canvas.drawText("$qNum.", blockLeft + 8f * pxPerMm, rowY + 0.9f * pxPerMm, textPaint)

                // 4 option bubbles
                for (k in 0..3) {
                    val center = OmrLayout.getAnswerBubbleCenter(qNum, k)
                    val cx = center.x * pxPerMm
                    val cy = center.y * pxPerMm

                    // Outline
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.28f * pxPerMm
                    paint.color = Color.GRAY
                    canvas.drawCircle(cx, cy, bubbleRadiusPx, paint)

                    // Option label inside
                    textPaint.textSize = 2.0f * pxPerMm
                    textPaint.color = Color.LTGRAY
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText(OmrLayout.OPTION_LABELS[k], cx, cy + 0.7f * pxPerMm, textPaint)

                    // Check if should fill for sample
                    if (fillSampleAnswers) {
                        val chosenOpt = sampleAnswers?.get(qNum) ?: when {
                            qNum % 17 == 0 -> -1 // simulate unanswered
                            qNum % 31 == 0 -> 99 // simulate multiple answers
                            else -> ((qNum * 3 + 1) % 4) // deterministic sample pattern
                        }
                        if (chosenOpt == k || (chosenOpt == 99 && (k == 0 || k == 1))) {
                            drawFilledBubble(canvas, cx, cy, bubbleRadiusPx, pxPerMm)
                        }
                    }
                }
            }
        }

        return bitmap
    }

    private fun drawFilledBubble(canvas: Canvas, cx: Float, cy: Float, radius: Float, pxPerMm: Float) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 24, 30) // Dark ballpoint ink
            style = Paint.Style.FILL
        }
        // Slightly organic ballpoint fill
        canvas.drawCircle(cx, cy, radius * 0.92f, fillPaint)
    }
}
