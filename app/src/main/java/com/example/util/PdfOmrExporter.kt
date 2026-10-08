package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
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

    // 1 mm in PostScript points (72 points / 25.4 mm)
    private const val PT_PER_MM = 72.0f / 25.4f

    // Standard A4 dimensions in points
    private const val A4_WIDTH_PT = (OmrLayout.PAGE_WIDTH_MM * PT_PER_MM).toInt()   // ~595 pt
    private const val A4_HEIGHT_PT = (OmrLayout.PAGE_HEIGHT_MM * PT_PER_MM).toInt() // ~842 pt

    /**
     * Generates a vector A4 PDF of the official RIP OMR sheet
     */
    fun createPdfDocument(): PdfDocument {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Clean white background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 1. Draw 4 solid black corner alignment markers (6mm x 6mm)
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        // Top-Left: (8, 8) to (14, 14) mm -> center (11, 11) mm
        canvas.drawRect(
            8f * PT_PER_MM, 8f * PT_PER_MM,
            14f * PT_PER_MM, 14f * PT_PER_MM,
            paint
        )
        // Top-Right: (196, 8) to (202, 8) mm -> center (199, 11) mm
        canvas.drawRect(
            196f * PT_PER_MM, 8f * PT_PER_MM,
            202f * PT_PER_MM, 14f * PT_PER_MM,
            paint
        )
        // Bottom-Left: (8, 283) to (14, 289) mm -> center (11, 286) mm
        canvas.drawRect(
            8f * PT_PER_MM, 283f * PT_PER_MM,
            14f * PT_PER_MM, 289f * PT_PER_MM,
            paint
        )
        // Bottom-Right: (196, 283) to (202, 289) mm -> center (199, 286) mm
        canvas.drawRect(
            196f * PT_PER_MM, 283f * PT_PER_MM,
            202f * PT_PER_MM, 289f * PT_PER_MM,
            paint
        )

        // 2. Header and Title
        textPaint.textSize = 6.0f * PT_PER_MM
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RIP ওএমআর মূল্যায়ন শিট (A4)", (A4_WIDTH_PT / 2).toFloat(), 20f * PT_PER_MM, textPaint)

        textPaint.textSize = 3.0f * PT_PER_MM
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = Color.rgb(40, 40, 40)
        canvas.drawText("কালো বা নীল বলপেন দিয়ে বৃত্ত সম্পূর্ণ ভরাট করুন • কোনো ভাঁজ বা কাটাকাটি করবেন না", (A4_WIDTH_PT / 2).toFloat(), 26f * PT_PER_MM, textPaint)

        // Candidate Info Line Boxes (Student Name, Subject, Date)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.3f * PT_PER_MM
        paint.color = Color.DKGRAY

        textPaint.textSize = 2.8f * PT_PER_MM
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // Candidate Name line
        canvas.drawText("শিক্ষার্থীর নাম (NAME):", 72f * PT_PER_MM, 46f * PT_PER_MM, textPaint)
        canvas.drawLine(108f * PT_PER_MM, 47f * PT_PER_MM, 192f * PT_PER_MM, 47f * PT_PER_MM, paint)

        // Date and Subject lines
        canvas.drawText("বিষয় (SUBJECT):", 72f * PT_PER_MM, 54f * PT_PER_MM, textPaint)
        canvas.drawLine(96f * PT_PER_MM, 55f * PT_PER_MM, 140f * PT_PER_MM, 55f * PT_PER_MM, paint)

        canvas.drawText("তারিখ (DATE):", 146f * PT_PER_MM, 54f * PT_PER_MM, textPaint)
        canvas.drawLine(168f * PT_PER_MM, 55f * PT_PER_MM, 192f * PT_PER_MM, 55f * PT_PER_MM, paint)

        // 3. Roll Number Section
        canvas.drawText("রোল নম্বর (ROLL NO):", 16f * PT_PER_MM, 44f * PT_PER_MM, textPaint)

        // 4 Roll digit input boxes
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.35f * PT_PER_MM
        paint.color = Color.BLACK

        for (col in 0..3) {
            val cx = (20f + 12f * col) * PT_PER_MM
            val boxLeft = cx - 4.5f * PT_PER_MM
            val boxRight = cx + 4.5f * PT_PER_MM
            val boxTop = 46f * PT_PER_MM
            val boxBottom = 52f * PT_PER_MM
            canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, paint)
        }

        // Draw Roll Bubbles (digits 0..9)
        val bubbleRadiusPt = OmrLayout.BUBBLE_RADIUS_MM * PT_PER_MM
        for (col in 0..3) {
            for (digit in 0..9) {
                val center = OmrLayout.getRollBubbleCenter(col, digit)
                val cx = center.x * PT_PER_MM
                val cy = center.y * PT_PER_MM

                // Bubble circle outline
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.28f * PT_PER_MM
                paint.color = Color.GRAY
                canvas.drawCircle(cx, cy, bubbleRadiusPt, paint)

                // Digit inside bubble
                textPaint.textSize = 2.4f * PT_PER_MM
                textPaint.color = Color.DKGRAY
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText(digit.toString(), cx, cy + 0.8f * PT_PER_MM, textPaint)
            }
        }

        // 4. Questions & Answer Bubbles (100 questions, 4 blocks of 25)
        val bandPaint = Paint().apply {
            color = Color.rgb(246, 247, 249) // light alternating row shading
            style = Paint.Style.FILL
        }

        for (b in 0..3) {
            val blockLeft = (12f + 47.5f * b) * PT_PER_MM
            val blockRight = blockLeft + 45f * PT_PER_MM

            // Block header
            textPaint.textSize = 3.0f * PT_PER_MM
            textPaint.color = Color.BLACK
            textPaint.textAlign = Paint.Align.LEFT
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val startQ = b * 25 + 1
            val endQ = (b + 1) * 25
            canvas.drawText("প্রশ্ন $startQ - $endQ", blockLeft, 126f * PT_PER_MM, textPaint)

            // Column labels ক, খ, গ, ঘ
            textPaint.textSize = 2.4f * PT_PER_MM
            textPaint.textAlign = Paint.Align.CENTER
            for (k in 0..3) {
                val optX = (12f + 47.5f * b + 14.5f + 7.5f * k) * PT_PER_MM
                canvas.drawText(OmrLayout.OPTION_LABELS[k], optX, 130f * PT_PER_MM, textPaint)
            }

            for (r in 0..24) {
                val qNum = b * 25 + r + 1
                val rowY = (133.75f + 5.5f * r) * PT_PER_MM

                // Draw subtle alternating row background band
                if (r % 2 == 1) {
                    canvas.drawRect(
                        blockLeft - 2f * PT_PER_MM,
                        rowY - 2.5f * PT_PER_MM,
                        blockRight + 2f * PT_PER_MM,
                        rowY + 2.5f * PT_PER_MM,
                        bandPaint
                    )
                }

                // Question number label
                textPaint.textSize = 2.4f * PT_PER_MM
                textPaint.color = Color.BLACK
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.typeface = Typeface.DEFAULT
                canvas.drawText("$qNum.", blockLeft + 8.5f * PT_PER_MM, rowY + 0.9f * PT_PER_MM, textPaint)

                // 4 option bubbles
                for (k in 0..3) {
                    val center = OmrLayout.getAnswerBubbleCenter(qNum, k)
                    val cx = center.x * PT_PER_MM
                    val cy = center.y * PT_PER_MM

                    // Bubble circle outline
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 0.28f * PT_PER_MM
                    paint.color = Color.GRAY
                    canvas.drawCircle(cx, cy, bubbleRadiusPt, paint)

                    // Option label inside
                    textPaint.textSize = 2.0f * PT_PER_MM
                    textPaint.color = Color.LTGRAY
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText(OmrLayout.OPTION_LABELS[k], cx, cy + 0.7f * PT_PER_MM, textPaint)
                }
            }
        }

        // Footer note
        textPaint.textSize = 2.2f * PT_PER_MM
        textPaint.color = Color.GRAY
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("প্রিন্ট নির্দেশিকা: ১০০% স্কেলে (Actual Size / No Scaling) A4 পেপারে প্রিন্ট করুন।", (A4_WIDTH_PT / 2).toFloat(), 276f * PT_PER_MM, textPaint)

        document.finishPage(page)
        return document
    }

    /**
     * Saves PDF to cache directory for immediate sharing
     */
    fun getShareablePdfFile(context: Context): File {
        val file = File(context.cacheDir, "rip_omr_sheet_a4.pdf")
        val doc = createPdfDocument()
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }

    /**
     * Saves PDF directly to public Downloads folder so user can easily find it in phone's file manager
     */
    fun savePdfToDownloads(context: Context): Boolean {
        return try {
            val doc = createPdfDocument()
            val fileName = "rip_omr_sheet_a4.pdf"

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
            putExtra(Intent.EXTRA_SUBJECT, "RIP OMR Sheet (A4)")
            putExtra(Intent.EXTRA_TEXT, "RIP OMR ১০০-প্রশ্নের অফিশিয়াল A4 ওএমআর শিট। ১০০% স্কেলে প্রিন্ট করুন।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "ওএমআর শিট PDF শেয়ার বা প্রিন্ট করুন"))
    }
}
