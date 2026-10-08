package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.omr.OmrLayout
import java.io.File
import java.io.FileOutputStream

object PdfOmrExporter {

    private const val PT_PER_MM = 72.0f / 25.4f
    private const val A4_WIDTH_PT = (OmrLayout.PAGE_WIDTH_MM * PT_PER_MM).toInt()   // ~595 pt
    private const val A4_HEIGHT_PT = (OmrLayout.PAGE_HEIGHT_MM * PT_PER_MM).toInt() // ~842 pt

    private val COLOR_PRIMARY = Color.rgb(15, 23, 42)      // Deep Slate / Navy
    private val COLOR_SECONDARY = Color.rgb(51, 65, 85)   // Muted Slate
    private val COLOR_BORDER = Color.rgb(71, 85, 105)     // Border
    private val COLOR_HEADER_BG = Color.rgb(241, 245, 249)// Soft Slate Tint
    private val COLOR_ROW_ALT = Color.rgb(248, 250, 252)  // Very soft row stripe
    private val COLOR_SUCCESS = Color.rgb(16, 185, 129)   // Green
    private val COLOR_DANGER = Color.rgb(239, 68, 68)     // Red

    /**
     * Generates a high quality vector A4 PDF matching the official RIP Exam OMR Sheet
     */
    fun createPdfDocument(): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = COLOR_PRIMARY
            strokeWidth = 0.35f * PT_PER_MM
        }

        val thinStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = COLOR_BORDER
            strokeWidth = 0.20f * PT_PER_MM
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 1. 4 Corner Solid Black Alignment Markers (9mm x 7mm)
        fillPaint.color = Color.BLACK
        canvas.drawRect(8f * PT_PER_MM, 8f * PT_PER_MM, 17f * PT_PER_MM, 15f * PT_PER_MM, fillPaint)
        canvas.drawRect(193f * PT_PER_MM, 8f * PT_PER_MM, 202f * PT_PER_MM, 15f * PT_PER_MM, fillPaint)
        canvas.drawRect(8f * PT_PER_MM, 282f * PT_PER_MM, 17f * PT_PER_MM, 289f * PT_PER_MM, fillPaint)
        canvas.drawRect(193f * PT_PER_MM, 282f * PT_PER_MM, 202f * PT_PER_MM, 289f * PT_PER_MM, fillPaint)

        // 2. Header: Title, Subtitle, and Badges (Y: 8mm to 28mm)
        textPaint.textSize = 5.2f * PT_PER_MM
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("RIP EXAM OMR SHEET", (A4_WIDTH_PT / 2).toFloat(), 13.5f * PT_PER_MM, textPaint)

        textPaint.textSize = 2.4f * PT_PER_MM
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("ওএমআর উত্তরপত্র • OFFICIAL A4 MCQ EVALUATION SHEET", (A4_WIDTH_PT / 2).toFloat(), 17.5f * PT_PER_MM, textPaint)

        // Header Badges
        val leftBadgeRect = RectF(20f * PT_PER_MM, 19.5f * PT_PER_MM, 58f * PT_PER_MM, 26f * PT_PER_MM)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(leftBadgeRect, 2f * PT_PER_MM, 2f * PT_PER_MM, fillPaint)
        canvas.drawRoundRect(leftBadgeRect, 2f * PT_PER_MM, 2f * PT_PER_MM, thinStrokePaint)
        textPaint.textSize = 2.0f * PT_PER_MM
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("MODEL: RIP-100-4", leftBadgeRect.centerX(), leftBadgeRect.centerY() + 0.7f * PT_PER_MM, textPaint)

        val centerNote = "(অবশ্যই কালো বা নীল কালির বলপয়েন্ট কলম দ্বারা বৃত্ত ভরাট করতে হবে)"
        textPaint.textSize = 2.1f * PT_PER_MM
        textPaint.color = Color.rgb(180, 20, 20)
        canvas.drawText(centerNote, (A4_WIDTH_PT / 2).toFloat(), 24f * PT_PER_MM, textPaint)

        val rightBadgeRect = RectF(152f * PT_PER_MM, 19.5f * PT_PER_MM, 190f * PT_PER_MM, 26f * PT_PER_MM)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(rightBadgeRect, 2f * PT_PER_MM, 2f * PT_PER_MM, fillPaint)
        canvas.drawRoundRect(rightBadgeRect, 2f * PT_PER_MM, 2f * PT_PER_MM, thinStrokePaint)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("১০০ প্রশ্ন • 100 QUESTIONS", rightBadgeRect.centerX(), rightBadgeRect.centerY() + 0.7f * PT_PER_MM, textPaint)

        // 3. Left Box: Candidate Information & Instructions (10mm to 124mm, 32mm to 109mm)
        val candidateBox = RectF(10f * PT_PER_MM, 32f * PT_PER_MM, 124f * PT_PER_MM, 109f * PT_PER_MM)
        canvas.drawRoundRect(candidateBox, 2.5f * PT_PER_MM, 2.5f * PT_PER_MM, strokePaint)

        // Box Header Banner
        val candidateHeader = RectF(10f * PT_PER_MM, 32f * PT_PER_MM, 124f * PT_PER_MM, 38.5f * PT_PER_MM)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(candidateHeader, 2.5f * PT_PER_MM, 2.5f * PT_PER_MM, fillPaint)
        canvas.drawLine(10f * PT_PER_MM, 38.5f * PT_PER_MM, 124f * PT_PER_MM, 38.5f * PT_PER_MM, strokePaint)

        textPaint.textSize = 2.3f * PT_PER_MM
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("পরীক্ষার্থীর তথ্য ও নির্দেশাবলি (CANDIDATE INFO & RULES)", candidateBox.centerX(), 36.5f * PT_PER_MM, textPaint)

        // Candidate Fields
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 2.2f * PT_PER_MM

        // Name
        canvas.drawText("পরীক্ষার্থীর নাম (NAME):", 13f * PT_PER_MM, 44.5f * PT_PER_MM, textPaint)
        canvas.drawLine(49f * PT_PER_MM, 45f * PT_PER_MM, 121f * PT_PER_MM, 45f * PT_PER_MM, thinStrokePaint)

        // Subject
        canvas.drawText("বিষয় (SUBJECT):", 13f * PT_PER_MM, 52.5f * PT_PER_MM, textPaint)
        canvas.drawLine(38f * PT_PER_MM, 53f * PT_PER_MM, 121f * PT_PER_MM, 53f * PT_PER_MM, thinStrokePaint)

        // Date and Room/Batch
        canvas.drawText("তারিখ (DATE):", 13f * PT_PER_MM, 60.5f * PT_PER_MM, textPaint)
        canvas.drawLine(34f * PT_PER_MM, 61f * PT_PER_MM, 68f * PT_PER_MM, 61f * PT_PER_MM, thinStrokePaint)

        canvas.drawText("রুম/ব্যাচ:", 72f * PT_PER_MM, 60.5f * PT_PER_MM, textPaint)
        canvas.drawLine(86f * PT_PER_MM, 61f * PT_PER_MM, 121f * PT_PER_MM, 61f * PT_PER_MM, thinStrokePaint)

        // Signature
        canvas.drawText("পরীক্ষার্থীর স্বাক্ষর (SIGNATURE):", 13f * PT_PER_MM, 68.5f * PT_PER_MM, textPaint)
        canvas.drawLine(58f * PT_PER_MM, 69f * PT_PER_MM, 121f * PT_PER_MM, 69f * PT_PER_MM, thinStrokePaint)

        // Divider
        canvas.drawLine(10f * PT_PER_MM, 73f * PT_PER_MM, 124f * PT_PER_MM, 73f * PT_PER_MM, thinStrokePaint)

        // Instructions Section
        textPaint.textSize = 2.0f * PT_PER_MM
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = COLOR_PRIMARY
        canvas.drawText("নির্দেশাবলি (INSTRUCTIONS):", 13f * PT_PER_MM, 78f * PT_PER_MM, textPaint)

        textPaint.textSize = 1.8f * PT_PER_MM
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("১. প্রতিটি প্রশ্নের বিপরীতে কেবল একটি বৃত্ত সম্পূর্ণভাবে ভরাট করতে হবে।", 13f * PT_PER_MM, 82.5f * PT_PER_MM, textPaint)
        canvas.drawText("২. একাধিক বৃত্ত ভরাট বা কাটাকাটি করলে উত্তরটি বাতিল বলে গণ্য হবে।", 13f * PT_PER_MM, 86.5f * PT_PER_MM, textPaint)
        canvas.drawText("৩. শিটে কোনো অপ্রয়োজনীয় দাগ, ভাঁজ বা ময়লা ফেলা সম্পূর্ণ নিষেধ।", 13f * PT_PER_MM, 90.5f * PT_PER_MM, textPaint)

        // Visual Bubble Filling Guide (Icons)
        val guideY = 99f * PT_PER_MM
        val guideR = 2.0f * PT_PER_MM

        // Correct Example: Filled Bubble
        fillPaint.color = Color.BLACK
        canvas.drawCircle(22f * PT_PER_MM, guideY, guideR, fillPaint)
        textPaint.textSize = 1.9f * PT_PER_MM
        textPaint.color = COLOR_SUCCESS
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("সঠিক (●)", 22f * PT_PER_MM, guideY + 5f * PT_PER_MM, textPaint)

        // Wrong Example 1: Cross
        canvas.drawCircle(44f * PT_PER_MM, guideY, guideR, thinStrokePaint)
        canvas.drawLine(42f * PT_PER_MM, guideY - 2f * PT_PER_MM, 46f * PT_PER_MM, guideY + 2f * PT_PER_MM, thinStrokePaint)
        canvas.drawLine(46f * PT_PER_MM, guideY - 2f * PT_PER_MM, 42f * PT_PER_MM, guideY + 2f * PT_PER_MM, thinStrokePaint)
        textPaint.color = COLOR_DANGER
        canvas.drawText("ভুল (✖)", 44f * PT_PER_MM, guideY + 5f * PT_PER_MM, textPaint)

        // Wrong Example 2: Tick
        canvas.drawCircle(66f * PT_PER_MM, guideY, guideR, thinStrokePaint)
        canvas.drawLine(64.5f * PT_PER_MM, guideY, 66f * PT_PER_MM, guideY + 1.8f * PT_PER_MM, thinStrokePaint)
        canvas.drawLine(66f * PT_PER_MM, guideY + 1.8f * PT_PER_MM, 68f * PT_PER_MM, guideY - 2f * PT_PER_MM, thinStrokePaint)
        canvas.drawText("ভুল (✔)", 66f * PT_PER_MM, guideY + 5f * PT_PER_MM, textPaint)

        // Wrong Example 3: Half-filled
        canvas.drawCircle(88f * PT_PER_MM, guideY, guideR, thinStrokePaint)
        val halfRect = RectF(88f * PT_PER_MM - guideR, guideY - guideR, 88f * PT_PER_MM, guideY + guideR)
        canvas.drawArc(RectF(88f * PT_PER_MM - guideR, guideY - guideR, 88f * PT_PER_MM + guideR, guideY + guideR), 90f, 180f, true, fillPaint)
        canvas.drawText("ভুল (◐)", 88f * PT_PER_MM, guideY + 5f * PT_PER_MM, textPaint)

        // Wrong Example 4: Dot in center
        canvas.drawCircle(110f * PT_PER_MM, guideY, guideR, thinStrokePaint)
        canvas.drawCircle(110f * PT_PER_MM, guideY, 0.6f * PT_PER_MM, fillPaint)
        canvas.drawText("ভুল (·)", 110f * PT_PER_MM, guideY + 5f * PT_PER_MM, textPaint)

        // 4. Right Box: Roll Number Section (128mm to 200mm, 32mm to 109mm)
        val rollBox = RectF(128f * PT_PER_MM, 32f * PT_PER_MM, 200f * PT_PER_MM, 109f * PT_PER_MM)
        canvas.drawRoundRect(rollBox, 2.5f * PT_PER_MM, 2.5f * PT_PER_MM, strokePaint)

        val rollHeader = RectF(128f * PT_PER_MM, 32f * PT_PER_MM, 200f * PT_PER_MM, 38.5f * PT_PER_MM)
        fillPaint.color = COLOR_HEADER_BG
        canvas.drawRoundRect(rollHeader, 2.5f * PT_PER_MM, 2.5f * PT_PER_MM, fillPaint)
        canvas.drawLine(128f * PT_PER_MM, 38.5f * PT_PER_MM, 200f * PT_PER_MM, 38.5f * PT_PER_MM, strokePaint)

        textPaint.textSize = 2.3f * PT_PER_MM
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("রোল নম্বর (ROLL NUMBER)", rollBox.centerX(), 36.5f * PT_PER_MM, textPaint)

        // 4 Roll digit input boxes
        val rollBoxWidth = 10f * PT_PER_MM
        val rollBoxHeight = 7f * PT_PER_MM
        val rollBoxTop = 41.5f * PT_PER_MM
        val rollBoxBottom = rollBoxTop + rollBoxHeight

        for (col in 0 until OmrLayout.TOTAL_ROLL_DIGITS) {
            val cx = (OmrLayout.ROLL_START_X + OmrLayout.ROLL_STEP_X * col) * PT_PER_MM
            val boxLeft = cx - rollBoxWidth / 2
            val boxRight = cx + rollBoxWidth / 2
            canvas.drawRect(boxLeft, rollBoxTop, boxRight, rollBoxBottom, strokePaint)

            // Digit Bubbles 0..9 below
            for (digit in 0..9) {
                val centerPt = OmrLayout.getRollBubbleCenter(col, digit)
                val bx = centerPt.x * PT_PER_MM
                val by = centerPt.y * PT_PER_MM
                val bRadius = OmrLayout.BUBBLE_RADIUS_MM * PT_PER_MM

                canvas.drawCircle(bx, by, bRadius, strokePaint)
                textPaint.textSize = 1.9f * PT_PER_MM
                textPaint.color = COLOR_PRIMARY
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(digit.toString(), bx, by + 0.7f * PT_PER_MM, textPaint)
            }
        }

        // 5. Questions Section (4 Columns of 25 Questions, Y: 114mm to 269mm)
        val colWidth = OmrLayout.COL_WIDTH_MM * PT_PER_MM
        val tableTopY = OmrLayout.TABLE_TOP_Y * PT_PER_MM
        val headerH = OmrLayout.HEADER_HEIGHT_MM * PT_PER_MM
        val rowH = OmrLayout.ROW_HEIGHT_MM * PT_PER_MM
        val tableBottomY = tableTopY + headerH + rowH * 25
        val bubbleR = OmrLayout.BUBBLE_RADIUS_MM * PT_PER_MM

        val rowAltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = COLOR_ROW_ALT
        }

        for (col in 0..3) {
            val colLeft = OmrLayout.COL_X_OFFSETS[col] * PT_PER_MM
            val colRight = colLeft + colWidth
            val startQ = col * 25 + 1
            val endQ = (col + 1) * 25

            // Outer box
            canvas.drawRect(colLeft, tableTopY, colRight, tableBottomY, strokePaint)

            // Header Background & Text
            fillPaint.color = COLOR_HEADER_BG
            canvas.drawRect(colLeft, tableTopY, colRight, tableTopY + headerH, fillPaint)
            canvas.drawLine(colLeft, tableTopY + headerH, colRight, tableTopY + headerH, strokePaint)

            // Column Title (e.g., প্রশ্ন ১ - ২৫)
            textPaint.textSize = 2.1f * PT_PER_MM
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.color = COLOR_PRIMARY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val headerTitle = if (col == 0) "প্রশ্ন ১-২৫" else "প্রশ্ন $startQ-$endQ"
            canvas.drawText(headerTitle, colLeft + 1.8f * PT_PER_MM, tableTopY + 4.8f * PT_PER_MM, textPaint)

            // Header Option Labels (ক, খ, গ, ঘ)
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 2.1f * PT_PER_MM
            for (opt in 0..3) {
                val optX = colLeft + (OmrLayout.BUBBLE_START_OFFSET_X + OmrLayout.BUBBLE_STEP_X * opt) * PT_PER_MM
                canvas.drawText(OmrLayout.OPTION_LABELS[opt], optX, tableTopY + 4.8f * PT_PER_MM, textPaint)
            }

            // 25 Question Rows
            for (r in 0..24) {
                val qNum = startQ + r
                val yRowTop = tableTopY + headerH + rowH * r
                val yRowBottom = yRowTop + rowH
                val yRowCenter = yRowTop + rowH / 2

                // Alternating row background tint
                if (r % 2 == 1) {
                    canvas.drawRect(colLeft + 0.3f * PT_PER_MM, yRowTop, colRight - 0.3f * PT_PER_MM, yRowBottom, rowAltPaint)
                }

                // Row divider line
                canvas.drawLine(colLeft, yRowBottom, colRight, yRowBottom, thinStrokePaint)

                // Question number (০১., ০২., ... ২৫.)
                val qStr = if (qNum < 10) "০$qNum." else "$qNum."
                textPaint.textSize = 2.0f * PT_PER_MM
                textPaint.textAlign = Paint.Align.LEFT
                textPaint.color = COLOR_PRIMARY
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(qStr, colLeft + 2.0f * PT_PER_MM, yRowCenter + 0.7f * PT_PER_MM, textPaint)

                // 4 option bubbles: ক, খ, গ, ঘ
                for (opt in 0..3) {
                    val centerPt = OmrLayout.getAnswerBubbleCenter(qNum, opt)
                    val bx = centerPt.x * PT_PER_MM
                    val by = centerPt.y * PT_PER_MM

                    // Bubble circle outline
                    canvas.drawCircle(bx, by, bubbleR, strokePaint)

                    // Bubble label inside
                    textPaint.textSize = 1.9f * PT_PER_MM
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.color = COLOR_PRIMARY
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(OmrLayout.OPTION_LABELS[opt], bx, by + 0.65f * PT_PER_MM, textPaint)
                }
            }
        }

        // 6. Footer (Y: 272mm to 280mm)
        textPaint.textSize = 2.2f * PT_PER_MM
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = COLOR_PRIMARY
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("★ RIP OMR AUTOMATED EVALUATION SYSTEM ★", (A4_WIDTH_PT / 2).toFloat(), 274.5f * PT_PER_MM, textPaint)

        textPaint.textSize = 1.9f * PT_PER_MM
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = COLOR_SECONDARY
        canvas.drawText("প্রিন্ট নির্দেশিকা: ১০০% স্কেলে (Actual Size / No Scaling) A4 পেপারে প্রিন্ট করুন। • [FORM ID: RIP-OMR-A4-100Q-V2]", (A4_WIDTH_PT / 2).toFloat(), 278.5f * PT_PER_MM, textPaint)

        document.finishPage(page)
        return document
    }

    /**
     * Saves PDF to cache directory for immediate sharing
     */
    fun getShareablePdfFile(context: Context): File {
        val file = File(context.cacheDir, "rip_exam_omr_sheet_a4.pdf")
        val doc = createPdfDocument()
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }

    /**
     * Saves PDF directly to public Downloads folder so user can easily find it
     */
    fun savePdfToDownloads(context: Context): Boolean {
        return try {
            val doc = createPdfDocument()
            val fileName = "rip_exam_omr_sheet_a4.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        doc.writeTo(out)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetFile = File(downloadDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    doc.writeTo(out)
                }
            }
            doc.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Triggers Android system share sheet for the generated PDF
     */
    fun sharePdf(context: Context) {
        val file = getShareablePdfFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "RIP Exam OMR Sheet (A4)")
            putExtra(Intent.EXTRA_TEXT, "RIP Exam ১০০-প্রশ্নের অফিশিয়াল A4 ওএমআর শিট। ১০০% স্কেলে প্রিন্ট করুন।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "ওএমআর শিট PDF শেয়ার বা প্রিন্ট করুন"))
    }
}
