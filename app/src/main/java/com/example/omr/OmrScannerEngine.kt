package com.example.omr

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import com.example.data.model.ExamEntity
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object OmrScannerEngine {

    const val DEFAULT_SENSITIVITY = 0.68f

    /**
     * Automatic corner marker detection
     * Searches for the 4 alignment black squares in the 4 quadrants of the bitmap.
     * Returns list of 4 points: [TL, TR, BL, BR] in pixel coordinates.
     */
    fun detectCornerMarkers(bitmap: Bitmap): List<PointF> {
        val width = bitmap.width
        val height = bitmap.height

        // Downscale for fast blob search
        val scale = if (max(width, height) > 1200) 1200f / max(width, height) else 1.0f
        val smallW = (width * scale).toInt()
        val smallH = (height * scale).toInt()
        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)
        } else {
            bitmap
        }

        // Grayscale luminance array
        val lum = IntArray(smallW * smallH)
        val pixels = IntArray(smallW * smallH)
        scaledBitmap.getPixels(pixels, 0, smallW, 0, 0, smallW, smallH)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            lum[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        // Quadrant search bounding boxes (relative)
        val tl = findCornerMarkerInRegion(lum, smallW, smallH, 0.01f, 0.35f, 0.01f, 0.28f, isTop = true, isLeft = true)
        val tr = findCornerMarkerInRegion(lum, smallW, smallH, 0.65f, 0.99f, 0.01f, 0.28f, isTop = true, isLeft = false)
        val bl = findCornerMarkerInRegion(lum, smallW, smallH, 0.01f, 0.35f, 0.72f, 0.99f, isTop = false, isLeft = true)
        val br = findCornerMarkerInRegion(lum, smallW, smallH, 0.65f, 0.99f, 0.72f, 0.99f, isTop = false, isLeft = false)

        val invScale = 1.0f / scale

        // Default fallbacks based on A4 aspect ratio within the image
        val defTL = PointF(width * (11f / 210f), height * (11f / 297f))
        val defTR = PointF(width * (199f / 210f), height * (11f / 297f))
        val defBL = PointF(width * (11f / 210f), height * (286f / 297f))
        val defBR = PointF(width * (199f / 210f), height * (286f / 297f))

        val finalTL = tl?.let { PointF(it.x * invScale, it.y * invScale) } ?: defTL
        val finalTR = tr?.let { PointF(it.x * invScale, it.y * invScale) } ?: defTR
        val finalBL = bl?.let { PointF(it.x * invScale, it.y * invScale) } ?: defBL
        val finalBR = br?.let { PointF(it.x * invScale, it.y * invScale) } ?: defBR

        return listOf(finalTL, finalTR, finalBL, finalBR)
    }

    private fun findCornerMarkerInRegion(
        lum: IntArray,
        w: Int,
        h: Int,
        minXFrac: Float,
        maxXFrac: Float,
        minYFrac: Float,
        maxYFrac: Float,
        isTop: Boolean,
        isLeft: Boolean
    ): PointF? {
        val startX = (w * minXFrac).toInt().coerceIn(0, w - 1)
        val endX = (w * maxXFrac).toInt().coerceIn(0, w - 1)
        val startY = (h * minYFrac).toInt().coerceIn(0, h - 1)
        val endY = (h * maxYFrac).toInt().coerceIn(0, h - 1)

        // Find minimum luminance in region
        var minLum = 255
        var avgLum = 0L
        var count = 0
        for (y in startY..endY) {
            for (x in startX..endX) {
                val l = lum[y * w + x]
                if (l < minLum) minLum = l
                avgLum += l
                count++
            }
        }
        if (count == 0) return null
        val mean = (avgLum / count).toInt()
        val threshold = (minLum * 0.65f + mean * 0.35f).toInt()

        // Find dark connected components or dark cluster centroid
        var sumX = 0L
        var sumY = 0L
        var darkCount = 0
        for (y in startY..endY) {
            for (x in startX..endX) {
                if (lum[y * w + x] <= threshold) {
                    sumX += x
                    sumY += y
                    darkCount++
                }
            }
        }

        return if (darkCount > 15) {
            PointF(sumX.toFloat() / darkCount, sumY.toFloat() / darkCount)
        } else {
            null
        }
    }

    /**
     * Compute projective transformation matrix from OMR sheet physical mm to image pixels
     */
    fun createHomographyMatrix(corners: List<PointF>): Matrix {
        require(corners.size == 4) { "Must provide exactly 4 corner points" }
        val tl = corners[0]
        val tr = corners[1]
        val bl = corners[2]
        val br = corners[3]

        // Source points in mm
        val src = floatArrayOf(
            OmrLayout.MARKER_TL.x, OmrLayout.MARKER_TL.y,
            OmrLayout.MARKER_TR.x, OmrLayout.MARKER_TR.y,
            OmrLayout.MARKER_BL.x, OmrLayout.MARKER_BL.y,
            OmrLayout.MARKER_BR.x, OmrLayout.MARKER_BR.y
        )

        // Destination points in pixels
        val dst = floatArrayOf(
            tl.x, tl.y,
            tr.x, tr.y,
            bl.x, bl.y,
            br.x, br.y
        )

        val matrix = Matrix()
        matrix.setPolyToPoly(src, 0, dst, 0, 4)
        return matrix
    }

    /**
     * Samples darkness ratio of a single bubble at (centerMm.x, centerMm.y)
     */
    fun sampleBubble(
        bitmap: Bitmap,
        matrix: Matrix,
        centerMm: PointF,
        sensitivity: Float
    ): BubbleSample {
        val pts = floatArrayOf(
            centerMm.x, centerMm.y,
            centerMm.x + OmrLayout.BUBBLE_RADIUS_MM, centerMm.y
        )
        matrix.mapPoints(pts)
        val cx = pts[0]
        val cy = pts[1]
        val rPx = max(1.0f, hypot(pts[2] - cx, pts[3] - cy))

        val innerRadiusPx = rPx * (OmrLayout.INNER_SAMPLE_RADIUS_MM / OmrLayout.BUBBLE_RADIUS_MM)
        val ringInnerPx = rPx * (OmrLayout.RING_INNER_RADIUS_MM / OmrLayout.BUBBLE_RADIUS_MM)
        val ringOuterPx = rPx * (OmrLayout.RING_OUTER_RADIUS_MM / OmrLayout.BUBBLE_RADIUS_MM)

        val innerRadiusSq = innerRadiusPx * innerRadiusPx
        val ringInnerSq = ringInnerPx * ringInnerPx
        val ringOuterSq = ringOuterPx * ringOuterPx

        val minX = (cx - ringOuterPx).toInt().coerceIn(0, bitmap.width - 1)
        val maxX = (cx + ringOuterPx).toInt().coerceIn(0, bitmap.width - 1)
        val minY = (cy - ringOuterPx).toInt().coerceIn(0, bitmap.height - 1)
        val maxY = (cy + ringOuterPx).toInt().coerceIn(0, bitmap.height - 1)

        var innerLumSum = 0L
        var innerCount = 0
        var ringLumSum = 0L
        var ringCount = 0

        for (y in minY..maxY) {
            val dy = y - cy
            val dySq = dy * dy
            for (x in minX..maxX) {
                val dx = x - cx
                val dSq = dx * dx + dySq

                if (dSq <= innerRadiusSq) {
                    val p = bitmap.getPixel(x, y)
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val l = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    innerLumSum += l
                    innerCount++
                } else if (dSq in ringInnerSq..ringOuterSq) {
                    val p = bitmap.getPixel(x, y)
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    val l = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                    ringLumSum += l
                    ringCount++
                }
            }
        }

        val meanInner = if (innerCount > 0) innerLumSum.toFloat() / innerCount else 255f
        val meanRing = if (ringCount > 0) ringLumSum.toFloat() / ringCount else 255f
        val fillRatio = meanInner / max(meanRing, 1.0f)
        val isFilled = fillRatio < sensitivity

        return BubbleSample(
            optionIndex = -1,
            centerPixels = PointF(cx, cy),
            fillRatio = fillRatio,
            isFilled = isFilled
        )
    }

    /**
     * Evaluates full OMR sheet against the exam answer keys
     */
    fun evaluateSheet(
        bitmap: Bitmap,
        exam: ExamEntity,
        corners: List<PointF>? = null,
        sensitivity: Float = DEFAULT_SENSITIVITY,
        allowNegativeTotal: Boolean = false
    ): OmrScanEvaluation {
        val detectedCorners = corners ?: detectCornerMarkers(bitmap)
        val matrix = createHomographyMatrix(detectedCorners)

        // 1. Evaluate Roll number (4 digits: col 0..3)
        val rollDetections = mutableListOf<RollDetection>()
        val rollSb = StringBuilder()

        for (col in 0 until OmrLayout.TOTAL_ROLL_DIGITS) {
            val filledDigits = mutableListOf<Int>()
            for (digit in 0..9) {
                val center = OmrLayout.getRollBubbleCenter(col, digit)
                val sample = sampleBubble(bitmap, matrix, center, sensitivity)
                if (sample.isFilled) {
                    filledDigits.add(digit)
                }
            }

            val finalDigit = if (filledDigits.size == 1) filledDigits[0] else null
            rollDetections.add(
                RollDetection(
                    column = col,
                    detectedDigits = filledDigits,
                    finalDigit = finalDigit
                )
            )

            if (finalDigit != null) {
                rollSb.append(finalDigit)
            } else {
                rollSb.append("?")
            }
        }

        val roll = rollSb.toString()
        val isRollComplete = !roll.contains("?")

        // 2. Parse answer key from exam string (1=ক/0, 2=খ/1, 3=গ/2, 4=ঘ/3, 0=not set)
        val keyList = List(exam.totalQuestions) { qIdx ->
            if (qIdx < exam.answers.length) {
                val ch = exam.answers[qIdx]
                when (ch) {
                    '1', 'ক' -> 0
                    '2', 'খ' -> 1
                    '3', 'গ' -> 2
                    '4', 'ঘ' -> 3
                    else -> -1
                }
            } else -1
        }

        // 3. Evaluate Questions (1..exam.totalQuestions)
        val questions = mutableListOf<QuestionDetection>()
        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        for (q in 1..exam.totalQuestions) {
            val filledOptions = mutableListOf<Int>()
            val samples = mutableListOf<BubbleSample>()

            for (opt in 0..3) {
                val center = OmrLayout.getAnswerBubbleCenter(q, opt)
                val sample = sampleBubble(bitmap, matrix, center, sensitivity)
                val updatedSample = sample.copy(optionIndex = opt)
                samples.add(updatedSample)
                if (updatedSample.isFilled) {
                    filledOptions.add(opt)
                }
            }

            val keyOpt = keyList[q - 1]
            val status: AnswerStatus = when {
                filledOptions.isEmpty() -> {
                    unansweredCount++
                    AnswerStatus.UNANSWERED
                }
                filledOptions.size == 1 -> {
                    val singleOpt = filledOptions[0]
                    if (keyOpt != -1 && singleOpt == keyOpt) {
                        correctCount++
                        AnswerStatus.CORRECT
                    } else {
                        wrongCount++
                        AnswerStatus.WRONG
                    }
                }
                else -> {
                    // Multiple filled counts as wrong and gets negative marking!
                    wrongCount++
                    AnswerStatus.MULTIPLE
                }
            }

            questions.add(
                QuestionDetection(
                    question = q,
                    detectedOptions = filledOptions,
                    keyOption = keyOpt,
                    status = status,
                    bubbleSamples = samples
                )
            )
        }

        val totalMarks = correctCount.toDouble()
        val negativeMarks = wrongCount * exam.negativeMark
        var netMarks = totalMarks - negativeMarks
        if (!allowNegativeTotal && netMarks < 0.0) {
            netMarks = 0.0
        }

        val percentage = if (exam.totalQuestions > 0) {
            (netMarks / exam.totalQuestions.toDouble()) * 100.0
        } else 0.0

        val isPassed = percentage >= exam.passPercent

        return OmrScanEvaluation(
            roll = roll,
            isRollComplete = isRollComplete,
            examId = exam.id,
            examTitle = exam.name,
            totalQuestions = exam.totalQuestions,
            passPercent = exam.passPercent,
            negativeMark = exam.negativeMark,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unansweredCount = unansweredCount,
            totalMarks = totalMarks,
            negativeMarks = negativeMarks,
            netMarks = netMarks,
            percentage = percentage,
            isPassed = isPassed,
            questions = questions,
            rollDetections = rollDetections,
            cornerMarkers = detectedCorners,
            sheetBitmap = bitmap
        )
    }
}
