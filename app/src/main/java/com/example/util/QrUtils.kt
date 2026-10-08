package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.example.data.model.ExamEntity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.json.JSONObject

object QrUtils {

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 2)
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
        }
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun encodeExamToJson(exam: ExamEntity): String {
        val json = JSONObject().apply {
            put("v", 1)
            put("n", exam.name)
            put("d", exam.date)
            put("q", exam.totalQuestions)
            put("p", exam.passPercent)
            put("m", exam.negativeMark)
            put("a", exam.answers)
        }
        return json.toString()
    }

    fun decodeExamFromJson(jsonStr: String): ExamEntity? {
        return try {
            val json = JSONObject(jsonStr)
            val name = json.optString("n", "আমদানিকৃত পরীক্ষা")
            val date = json.optString("d", "")
            val q = json.optInt("q", 100)
            val p = json.optDouble("p", 80.0)
            val m = json.optDouble("m", 0.25)
            val a = json.optString("a", "")
            ExamEntity(
                name = name,
                date = date,
                totalQuestions = q,
                passPercent = p,
                negativeMark = m,
                answers = a
            )
        } catch (e: Exception) {
            null
        }
    }
}
