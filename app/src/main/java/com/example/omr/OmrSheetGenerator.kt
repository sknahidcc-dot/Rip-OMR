package com.example.omr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

object OmrSheetGenerator {

    private val COLOR_PRIMARY = Color.rgb(15, 23, 42)      // Deep Slate / Navy
    private val COLOR_SECONDARY = Color.rgb(51, 65, 85)   // Muted Slate
    private val COLOR_BORDER = Color.rgb(71, 85, 105)     // Border
    private val COLOR_HEADER_BG = Color.rgb(241, 245, 249)// Soft Slate Tint
    private val COLOR_ROW_ALT = Color.rgb(248, 250, 252)  // Very soft row stripe
    private val COLOR_SUCCESS = Color.rgb(16, 185, 129)   // Green
    private val COLOR_DANGER = Color.rgb(239, 68, 68)     // Red
    private val COLOR_FILLED_INK = Color.rgb(25, 25, 25)  // Dark ink fill

    /**
     * Generates a high-resolution bitmap matching the official RIP Exam OMR Sheet
     */
    fun generateSheet(
        scaleDpi: Int = 150,
        fillSampleAnswers: Boolean = false,
        sampleRoll: String = "1248",
        sampleAnswers: Map<Int, Int>? = null
    ): Bitmap {
        val pxPerMm = scaleDpi / 25.4f
        val widthPx = (OmrLayout.PAGE_WIDTH_MM * pxPerMm).toInt()
        val heightPx = (OmrLayout.PAGE_HEIGHT_MM * pxPerMm).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = COLOR_PRIMARY
            strokeWidth = 0.35f * pxPerMm
        }

        val thinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = COLOR_BORDER
            strokeWidth = 0.20f * pxPerMm
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = COLOR_FILLED_INK
        }

        // 1. 4 Corner Solid Black Alignment Markers (9mm x 7mm)
        fillPaint.color = Color.BLACK
        canvas.drawRect(8f * pxPerMm, 8f * pxPerMm, 17f * pxPerMm, 15f * pxPerMm, fillPaint)
        canvas.drawRect(193f * pxPerMm, 8f * pxPerMm, 202f * pxPerMm, 15f * pxPerMm, fillPaint)
        canvas.drawRect(8f * pxPerMm, 282f * pxPerMm, 17f * pxPerMm, 289f * pxPerMm, fillPaint)
        canvas.drawRect(193f * pxPerMm, 282f * pxPerMm, 202f * pxPerMm, 289f * pxPerMm, fillPaint)

        // 2. Header: Title, Subtitle, and Badges (Y: 8mm to 28mm)
        textPaint.textSize = 5.2f * pxPerMm
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("RIP EXAM OMR SHEET", (widthPx / 2).toFloat(), 13.5f * pxPerMm, textPaint)

        textPaint.textSize = 2.4f * pxPerMm
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("ওএমআর উত্তরপত্র • OFFICIAL A4 MCQ EVALUATION SHEET", (widthPx / 2).toFloat(), 17.5f * pxPerMm, textPaint)

        // Header Badges
        val leftBadgeRect = RectF(20f * pxPerMm, 19.5f * pxPerMm, 58f * pxPerMm, 26f * pxPerMm)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(leftBadgeRect, 2f * pxPerMm, 2f * pxPerMm, fillPaint)
        canvas.drawRoundRect(leftBadgeRect, 2f * pxPerMm, 2f * pxPerMm, thinStrokePaint)
        textPaint.textSize = 2.0f * pxPerMm
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("MODEL: RIP-100-4", leftBadgeRect.centerX(), leftBadgeRect.centerY() + 0.7f * pxPerMm, textPaint)

        val centerNote = "(অবশ্যই কালো বা নীল কালির বলপয়েন্ট কলম দ্বারা বৃত্ত ভরাট করতে হবে)"
        textPaint.textSize = 2.1f * pxPerMm
        textPaint.color = Color.rgb(180, 20, 20)
        canvas.drawText(centerNote, (widthPx / 2).toFloat(), 24f * pxPerMm, textPaint)

        val rightBadgeRect = RectF(152f * pxPerMm, 19.5f * pxPerMm, 190f * pxPerMm, 26f * pxPerMm)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(rightBadgeRect, 2f * pxPerMm, 2f * pxPerMm, fillPaint)
        canvas.drawRoundRect(rightBadgeRect, 2f * pxPerMm, 2f * pxPerMm, thinStrokePaint)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("১০০ প্রশ্ন • 100 QUESTIONS", rightBadgeRect.centerX(), rightBadgeRect.centerY() + 0.7f * pxPerMm, textPaint)

        // 3. Left Box: Candidate Info & Instructions (10mm to 124mm, 32mm to 109mm)
        val candidateBox = RectF(10f * pxPerMm, 32f * pxPerMm, 124f * pxPerMm, 109f * pxPerMm)
        canvas.drawRoundRect(candidateBox, 2.5f * pxPerMm, 2.5f * pxPerMm, strokePaint)

        // Header Banner
        val candidateHeader = RectF(10f * pxPerMm, 32f * pxPerMm, 124f * pxPerMm, 38.5f * pxPerMm)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(candidateHeader, 2.5f * pxPerMm, 2.5f * pxPerMm, fillPaint)
        canvas.drawLine(10f * pxPerMm, 38.5f * pxPerMm, 124f * pxPerMm, 38.5f * pxPerMm, strokePaint)

        textPaint.textSize = 2.3f * pxPerMm
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("পরীক্ষার্থীর তথ্য ও নির্দেশাবলি (CANDIDATE INFO & RULES)", candidateBox.centerX(), 36.5f * pxPerMm, textPaint)

        // Fields
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 2.2f * pxPerMm

        canvas.drawText("পরীক্ষার্থীর নাম (NAME):", 13f * pxPerMm, 44.5f * pxPerMm, textPaint)
        canvas.drawLine(49f * pxPerMm, 45f * pxPerMm, 121f * pxPerMm, 45f * pxPerMm, thinStrokePaint)

        canvas.drawText("বিষয় (SUBJECT):", 13f * pxPerMm, 52.5f * pxPerMm, textPaint)
        canvas.drawLine(38f * pxPerMm, 53f * pxPerMm, 121f * pxPerMm, 53f * pxPerMm, thinStrokePaint)

        canvas.drawText("তারিখ (DATE):", 13f * pxPerMm, 60.5f * pxPerMm, textPaint)
        canvas.drawLine(34f * pxPerMm, 61f * pxPerMm, 68f * pxPerMm, 61f * pxPerMm, thinStrokePaint)

        canvas.drawText("রুম/ব্যাচ:", 72f * pxPerMm, 60.5f * pxPerMm, textPaint)
        canvas.drawLine(86f * pxPerMm, 61f * pxPerMm, 121f * pxPerMm, 61f * pxPerMm, thinStrokePaint)

        canvas.drawText("পরীক্ষার্থীর স্বাক্ষর (SIGNATURE):", 13f * pxPerMm, 68.5f * pxPerMm, textPaint)
        canvas.drawLine(58f * pxPerMm, 69f * pxPerMm, 121f * pxPerMm, 69f * pxPerMm, thinStrokePaint)

        if (fillSampleAnswers) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = Color.rgb(20, 60, 160)
            textPaint.textSize = 2.4f * pxPerMm
            canvas.drawText("মোঃ রাফি আহমেদ", 52f * pxPerMm, 44f * pxPerMm, textPaint)
            canvas.drawText("সাধারণ বিজ্ঞান ও গণিত", 42f * pxPerMm, 52f * pxPerMm, textPaint)
            canvas.drawText("১২/১০/২০২৬", 36f * pxPerMm, 60f * pxPerMm, textPaint)
            canvas.drawText("ব্যাচ-ক (২০৪)", 88f * pxPerMm, 60f * pxPerMm, textPaint)
            canvas.drawText("Rafi Ahmed", 62f * pxPerMm, 68f * pxPerMm, textPaint)
        }

        // Instructions Divider
        canvas.drawLine(10f * pxPerMm, 73f * pxPerMm, 124f * pxPerMm, 73f * pxPerMm, thinStrokePaint)

        textPaint.textSize = 2.0f * pxPerMm
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("নির্দেশাবলি (INSTRUCTIONS):", 13f * pxPerMm, 78f * pxPerMm, textPaint)

        textPaint.textSize = 1.8f * pxPerMm
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("১. প্রতিটি প্রশ্নের বিপরীতে কেবল একটি বৃত্ত সম্পূর্ণভাবে ভরাট করতে হবে।", 13f * pxPerMm, 82.5f * pxPerMm, textPaint)
        canvas.drawText("২. একাধিক বৃত্ত ভরাট বা কাটাকাটি করলে উত্তরটি বাতিল বলে গণ্য হবে।", 13f * pxPerMm, 86.5f * pxPerMm, textPaint)
        canvas.drawText("৩. শিটে কোনো অপ্রয়োজনীয় দাগ, ভাঁজ বা ময়লা ফেলা সম্পূর্ণ নিষেধ।", 13f * pxPerMm, 90.5f * pxPerMm, textPaint)

        // Visual Guide Icons
        val guideY = 99f * pxPerMm
        val guideR = 2.0f * pxPerMm

        fillPaint.color = Color.BLACK
        canvas.drawCircle(22f * pxPerMm, guideY, guideR, fillPaint)
        textPaint.textSize = 1.9f * pxPerMm
        textPaint.color = COLOR_SUCCESS
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("সঠিক (●)", 22f * pxPerMm, guideY + 5f * pxPerMm, textPaint)

        canvas.drawCircle(44f * pxPerMm, guideY, guideR, thinStrokePaint)
        canvas.drawLine(42f * pxPerMm, guideY - 2f * pxPerMm, 46f * pxPerMm, guideY + 2f * pxPerMm, thinStrokePaint)
        canvas.drawLine(46f * pxPerMm, guideY - 2f * pxPerMm, 42f * pxPerMm, guideY + 2f * pxPerMm, thinStrokePaint)
        textPaint.color = COLOR_DANGER
        canvas.drawText("ভুল (✖)", 44f * pxPerMm, guideY + 5f * pxPerMm, textPaint)

        canvas.drawCircle(66f * pxPerMm, guideY, guideR, thinStrokePaint)
        canvas.drawLine(64.5f * pxPerMm, guideY, 66f * pxPerMm, guideY + 1.8f * pxPerMm, thinStrokePaint)
        canvas.drawLine(66f * pxPerMm, guideY + 1.8f * pxPerMm, 68f * pxPerMm, guideY - 2f * pxPerMm, thinStrokePaint)
        canvas.drawText("ভুল (✔)", 66f * pxPerMm, guideY + 5f * pxPerMm, textPaint)

        canvas.drawCircle(88f * pxPerMm, guideY, guideR, thinStrokePaint)
        canvas.drawArc(RectF(88f * pxPerMm - guideR, guideY - guideR, 88f * pxPerMm + guideR, guideY + guideR), 90f, 180f, true, fillPaint)
        canvas.drawText("ভুল (◐)", 88f * pxPerMm, guideY + 5f * pxPerMm, textPaint)

        canvas.drawCircle(110f * pxPerMm, guideY, guideR, thinStrokePaint)
        canvas.drawCircle(110f * pxPerMm, guideY, 0.6f * pxPerMm, fillPaint)
        canvas.drawText("ভুল (·)", 110f * pxPerMm, guideY + 5f * pxPerMm, textPaint)

        // 4. Right Box: Roll Number Section (128mm to 200mm, 32mm to 109mm)
        val rollBox = RectF(128f * pxPerMm, 32f * pxPerMm, 200f * pxPerMm, 109f * pxPerMm)
        canvas.drawRoundRect(rollBox, 2.5f * pxPerMm, 2.5f * pxPerMm, strokePaint)

        val rollHeader = RectF(128f * pxPerMm, 32f * pxPerMm, 200f * pxPerMm, 38.5f * pxPerMm)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(rollHeader, 2.5f * pxPerMm, 2.5f * pxPerMm, fillPaint)
        canvas.drawLine(128f * pxPerMm, 38.5f * pxPerMm, 200f * pxPerMm, 38.5f * pxPerMm, strokePaint)

        textPaint.textSize = 2.3f * pxPerMm
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("রোল নম্বর (ROLL NUMBER)", rollBox.centerX(), 36.5f * pxPerMm, textPaint)

        // 4 Roll digit input boxes
        val rollBoxWidth = 10f * pxPerMm
        val rollBoxHeight = 7f * pxPerMm
        val rollBoxTop = 41.5f * pxPerMm
        val rollBoxBottom = rollBoxTop + rollBoxHeight

        val paddedRoll = sampleRoll.padStart(4, '0')

        for (col in 0 until OmrLayout.TOTAL_ROLL_DIGITS) {
            val cx = (OmrLayout.ROLL_START_X + OmrLayout.ROLL_STEP_X * col) * pxPerMm
            val boxLeft = cx - rollBoxWidth / 2
            val boxRight = cx + rollBoxWidth / 2
            canvas.drawRect(boxLeft, rollBoxTop, boxRight, rollBoxBottom, strokePaint)

            val filledDigit = if (fillSampleAnswers && col < paddedRoll.length) paddedRoll[col].digitToIntOrNull() else null

            if (filledDigit != null) {
                textPaint.textSize = 3.2f * pxPerMm
                textPaint.color = COLOR_PRIMARY
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(filledDigit.toString(), cx, rollBoxBottom - 1.8f * pxPerMm, textPaint)
            }

            // Digit Bubbles 0..9 below
            for (digit in 0..9) {
                val centerPt = OmrLayout.getRollBubbleCenter(col, digit)
                val bx = centerPt.x * pxPerMm
                val by = centerPt.y * pxPerMm
                val bRadius = OmrLayout.BUBBLE_RADIUS_MM * pxPerMm

                if (fillSampleAnswers && digit == filledDigit) {
                    canvas.drawCircle(bx, by, bRadius, inkPaint)
                } else {
                    canvas.drawCircle(bx, by, bRadius, strokePaint)
                    textPaint.textSize = 1.9f * pxPerMm
                    textPaint.color = COLOR_PRIMARY
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(digit.toString(), bx, by + 0.7f * pxPerMm, textPaint)
                }
            }
        }

        // 5. Questions Section (4 Columns of 25 Questions, Y: 114mm to 269mm)
        val colWidth = OmrLayout.COL_WIDTH_MM * pxPerMm
        val tableTopY = OmrLayout.TABLE_TOP_Y * pxPerMm
        val headerH = OmrLayout.HEADER_HEIGHT_MM * pxPerMm
        val rowH = OmrLayout.ROW_HEIGHT_MM * pxPerMm
        val tableBottomY = tableTopY + headerH + rowH * 25
        val bubbleR = OmrLayout.BUBBLE_RADIUS_MM * pxPerMm

        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = COLOR_ROW_ALT
        }

        for (col in 0..3) {
            val colLeft = OmrLayout.COL_X_OFFSETS[col] * pxPerMm
            val colRight = colLeft + colWidth
            val startQ = col * 25 + 1
            val endQ = (col + 1) * 25

            // Outer box
            canvas.drawRect(colLeft, tableTopY, colRight, tableBottomY, strokePaint)

            // Header Background & Text
            fillPaint.color = COLOR_HEADER_BG
            canvas.drawRect(colLeft, tableTopY, colRight, tableTopY + headerH, fillPaint)
            canvas.drawLine(colLeft, tableTopY + headerH, colRight, tableTopY + headerH, strokePaint)

            // Column Title
            textPaint.textSize = 2.1f * pxPerMm
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = COLOR_PRIMARY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val headerTitle = if (col == 0) "প্রশ্ন ১-২৫" else "প্রশ্ন $startQ-$endQ"
            canvas.drawText(headerTitle, colLeft + 1.8f * pxPerMm, tableTopY + 4.8f * pxPerMm, textPaint)

            // Header Option Labels (ক, খ, গ, ঘ)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 2.1f * pxPerMm
            for (opt in 0..3) {
                val optX = colLeft + (OmrLayout.BUBBLE_START_OFFSET_X + OmrLayout.BUBBLE_STEP_X * opt) * pxPerMm
                canvas.drawText(OmrLayout.OPTION_LABELS[opt], optX, tableTopY + 4.8f * pxPerMm, textPaint)
            }

            // 25 Question Rows
            for (r in 0..24) {
                val qNum = startQ + r
                val yRowTop = tableTopY + headerH + rowH * r
                val yRowBottom = yRowTop + rowH
                val yRowCenter = yRowTop + rowH / 2

                if (r % 2 == 1) {
                    canvas.drawRect(colLeft + 0.3f * pxPerMm, yRowTop, colRight - 0.3f * pxPerMm, yRowBottom, rowAltPaint)
                }

                canvas.drawLine(colLeft, yRowBottom, colRight, yRowBottom, thinStrokePaint)

                // Question number
                val qStr = if (qNum < 10) "০$qNum." else "$qNum."
                textPaint.textSize = 2.0f * pxPerMm
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.color = COLOR_PRIMARY
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(qStr, colLeft + 2.0f * pxPerMm, yRowCenter + 0.7f * pxPerMm, textPaint)

                // Sample answer logic (default pattern if null: cyclic answers)
                val filledOptForQ = if (fillSampleAnswers) {
                    sampleAnswers?.get(qNum) ?: ((qNum - 1) % 4)
                } else null

                // 4 option bubbles: ক, খ, গ, ঘ
                for (opt in 0..3) {
                    val centerPt = OmrLayout.getAnswerBubbleCenter(qNum, opt)
                    val bx = centerPt.x * pxPerMm
                    val by = centerPt.y * pxPerMm

                    if (filledOptForQ == opt) {
                        // Dark filled bubble
                        canvas.drawCircle(bx, by, bubbleR, inkPaint)
                    } else {
                        // Unfilled bubble outline with label
                        canvas.drawCircle(bx, by, bubbleR, strokePaint)
                        textPaint.textSize = 1.9f * pxPerMm
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.color = COLOR_PRIMARY
                        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        canvas.drawText(OmrLayout.OPTION_LABELS[opt], bx, by + 0.65f * pxPerMm, textPaint)
                    }
                }
            }
        }

        // 6. Footer (Y: 272mm to 280mm)
        textPaint.textSize = 2.2f * pxPerMm
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("★ RIP OMR AUTOMATED EVALUATION SYSTEM ★", (widthPx / 2).toFloat(), 274.5f * pxPerMm, textPaint)

        textPaint.textSize = 1.9f * pxPerMm
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("প্রিন্ট নির্দেশিকা: ১০০% স্কেলে (Actual Size / No Scaling) A4 পেপারে প্রিন্ট করুন। • [FORM ID: RIP-OMR-A4-100Q-V2]", (widthPx / 2).toFloat(), 278.5f * pxPerMm, textPaint)

        return bitmap
    }
}
