package com.example.omr

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import com.example.data.model.ExamEntity
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

object OmrScannerEngine {

    const val DEFAULT_FILL_THRESHOLD = 0.40f // 40% non-zero pixels inside bubble = filled

    /**
     * OpenCV Step 1: Grayscale + Gaussian Blur + Adaptive Thresholding (THRESH_BINARY_INV)
     * In binaryInv, dark ink becomes 255 (white), blank paper becomes 0 (black).
     */
    class BinaryImage(
        val width: Int,
        val height: Int,
        val data: ByteArray // 0 or 255
    ) {
        /**
         * OpenCV cv2.countNonZero inside a circular ROI
         */
        fun countNonZeroCircle(cx: Float, cy: Float, radius: Float): Pair<Int, Int> {
            val rSq = radius * radius
            val minX = (cx - radius).toInt().coerceIn(0, width - 1)
            val maxX = (cx + radius).toInt().coerceIn(0, width - 1)
            val minY = (cy - radius).toInt().coerceIn(0, height - 1)
            val maxY = (cy + radius).toInt().coerceIn(0, height - 1)

            var nonZeroCount = 0
            var totalPixels = 0

            for (y in minY..maxY) {
                val dy = y - cy
                val dySq = dy * dy
                val rowOffset = y * width
                for (x in minX..maxX) {
                    val dx = x - cx
                    if (dx * dx + dySq <= rSq) {
                        totalPixels++
                        if (data[rowOffset + x].toInt() != 0) {
                            nonZeroCount++
                        }
                    }
                }
            }
            return Pair(nonZeroCount, totalPixels)
        }
    }

    /**
     * Converts Bitmap to OpenCV THRESH_BINARY_INV image using Gaussian Blur and Adaptive Thresholding
     */
    fun processImageOpenCv(bitmap: Bitmap): BinaryImage {
        val w = bitmap.width
        val h = bitmap.height

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        // 1. Grayscale (cv2.cvtColor BGR2GRAY)
        val gray = IntArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            gray[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        // 2. Gaussian Blur (5x5 kernel approximation via separable 1D box/Gaussian passes)
        val blurred = IntArray(w * h)
        val temp = IntArray(w * h)

        // Horizontal pass
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                var sum = 0
                var weightSum = 0
                for (kx in -2..2) {
                    val px = (x + kx).coerceIn(0, w - 1)
                    val weight = when (kx) {
                        0 -> 6
                        -1, 1 -> 4
                        else -> 1
                    }
                    sum += gray[row + px] * weight
                    weightSum += weight
                }
                temp[row + x] = sum / weightSum
            }
        }

        // Vertical pass
        for (x in 0 until w) {
            for (y in 0 until h) {
                var sum = 0
                var weightSum = 0
                for (ky in -2..2) {
                    val py = (y + ky).coerceIn(0, h - 1)
                    val weight = when (ky) {
                        0 -> 6
                        -1, 1 -> 4
                        else -> 1
                    }
                    sum += temp[py * w + x] * weight
                    weightSum += weight
                }
                blurred[y * w + x] = sum / weightSum
            }
        }

        // 3. Adaptive Thresholding (cv2.adaptiveThreshold with THRESH_BINARY_INV)
        // Uses integral image for fast local mean calculation
        val blockSize = 17
        val halfBlock = blockSize / 2
        val cConstant = 5 // C offset

        val integral = LongArray((w + 1) * (h + 1))
        for (y in 0 until h) {
            var rowSum = 0L
            val rowOffset = (y + 1) * (w + 1)
            val prevRowOffset = y * (w + 1)
            val imgRow = y * w
            for (x in 0 until w) {
                rowSum += blurred[imgRow + x]
                integral[rowOffset + (x + 1)] = integral[prevRowOffset + (x + 1)] + rowSum
            }
        }

        val binaryInv = ByteArray(w * h)
        for (y in 0 until h) {
            val y1 = max(0, y - halfBlock)
            val y2 = min(h - 1, y + halfBlock)
            val imgRow = y * w
            for (x in 0 until w) {
                val x1 = max(0, x - halfBlock)
                val x2 = min(w - 1, x + halfBlock)
                val area = (x2 - x1 + 1) * (y2 - y1 + 1)

                val sum = integral[(y2 + 1) * (w + 1) + (x2 + 1)] -
                        integral[y1 * (w + 1) + (x2 + 1)] -
                        integral[(y2 + 1) * (w + 1) + x1] +
                        integral[y1 * (w + 1) + x1]

                val mean = sum / area
                val pixelVal = blurred[imgRow + x]

                // THRESH_BINARY_INV: dark pixels (< mean - C) become 255
                binaryInv[imgRow + x] = if (pixelVal < mean - cConstant) (-1).toByte() else 0
            }
        }

        return BinaryImage(w, h, binaryInv)
    }

    /**
     * OpenCV Step 2: Contour Detection for the 4 alignment markers
     * Searches the 4 corner regions for dense rectangular blobs
     */
    fun detectCornerMarkers(bitmap: Bitmap): List<PointF> {
        val w = bitmap.width
        val h = bitmap.height

        val binary = processImageOpenCv(bitmap)

        // Quadrant search regions: TL, TR, BL, BR
        val tl = findMarkerCentroidInRegion(binary, 0.01f, 0.20f, 0.01f, 0.15f)
        val tr = findMarkerCentroidInRegion(binary, 0.80f, 0.99f, 0.01f, 0.15f)
        val bl = findMarkerCentroidInRegion(binary, 0.01f, 0.20f, 0.85f, 0.99f)
        val br = findMarkerCentroidInRegion(binary, 0.80f, 0.99f, 0.85f, 0.99f)

        // Theoretical A4 defaults
        val defTL = PointF(w * (OmrLayout.MARKER_TL.x / OmrLayout.PAGE_WIDTH_MM), h * (OmrLayout.MARKER_TL.y / OmrLayout.PAGE_HEIGHT_MM))
        val defTR = PointF(w * (OmrLayout.MARKER_TR.x / OmrLayout.PAGE_WIDTH_MM), h * (OmrLayout.MARKER_TR.y / OmrLayout.PAGE_HEIGHT_MM))
        val defBL = PointF(w * (OmrLayout.MARKER_BL.x / OmrLayout.PAGE_WIDTH_MM), h * (OmrLayout.MARKER_BL.y / OmrLayout.PAGE_HEIGHT_MM))
        val defBR = PointF(w * (OmrLayout.MARKER_BR.x / OmrLayout.PAGE_WIDTH_MM), h * (OmrLayout.MARKER_BR.y / OmrLayout.PAGE_HEIGHT_MM))

        return listOf(
            tl ?: defTL,
            tr ?: defTR,
            bl ?: defBL,
            br ?: defBR
        )
    }

    private fun findMarkerCentroidInRegion(
        binary: BinaryImage,
        xMinF: Float,
        xMaxF: Float,
        yMinF: Float,
        yMaxF: Float
    ): PointF? {
        val x1 = (binary.width * xMinF).toInt()
        val x2 = (binary.width * xMaxF).toInt()
        val y1 = (binary.height * yMinF).toInt()
        val y2 = (binary.height * yMaxF).toInt()

        var sumX = 0L
        var sumY = 0L
        var count = 0

        for (y in y1..y2) {
            val offset = y * binary.width
            for (x in x1..x2) {
                if (binary.data[offset + x].toInt() != 0) {
                    sumX += x
                    sumY += y
                    count++
                }
            }
        }

        return if (count > 60) {
            PointF(sumX.toFloat() / count, sumY.toFloat() / count)
        } else null
    }

    /**
     * OpenCV Step 3: cv2.getPerspectiveTransform and cv2.warpPerspective mapping
     */
    fun createHomographyMatrix(corners: List<PointF>): Matrix {
        val src = floatArrayOf(
            OmrLayout.MARKER_TL.x, OmrLayout.MARKER_TL.y,
            OmrLayout.MARKER_TR.x, OmrLayout.MARKER_TR.y,
            OmrLayout.MARKER_BL.x, OmrLayout.MARKER_BL.y,
            OmrLayout.MARKER_BR.x, OmrLayout.MARKER_BR.y
        )
        val dst = floatArrayOf(
            corners[0].x, corners[0].y,
            corners[1].x, corners[1].y,
            corners[2].x, corners[2].y,
            corners[3].x, corners[3].y
        )
        val matrix = Matrix()
        matrix.setPolyToPoly(src, 0, dst, 0, 4)
        return matrix
    }

    /**
     * OpenCV Step 4 & 5: Bubble Grid Mapping + Pixel Counting (cv2.countNonZero)
     */
    fun evaluateSheet(
        bitmap: Bitmap,
        exam: ExamEntity,
        corners: List<PointF>? = null,
        sensitivity: Float = DEFAULT_FILL_THRESHOLD,
        allowNegativeTotal: Boolean = false
    ): OmrScanEvaluation {
        val binaryImage = processImageOpenCv(bitmap)
        val detectedCorners = corners ?: detectCornerMarkers(bitmap)
        val matrix = createHomographyMatrix(detectedCorners)

        val mappedPts = FloatArray(4)

        // 1. Evaluate 4-digit Roll Number using OpenCV countNonZero
        val rollDetections = mutableListOf<RollDetection>()
        val rollBuilder = StringBuilder()

        for (col in 0 until OmrLayout.TOTAL_ROLL_DIGITS) {
            var bestDigit = -1
            var bestRatio = 0f
            val filledDigits = mutableListOf<Int>()

            for (digit in 0..9) {
                val centerMm = OmrLayout.getRollBubbleCenter(col, digit)
                mappedPts[0] = centerMm.x
                mappedPts[1] = centerMm.y
                mappedPts[2] = centerMm.x + OmrLayout.BUBBLE_RADIUS_MM
                mappedPts[3] = centerMm.y
                matrix.mapPoints(mappedPts)

                val cx = mappedPts[0]
                val cy = mappedPts[1]
                val rPx = max(1.5f, hypot(mappedPts[2] - cx, mappedPts[3] - cy))
                val innerR = rPx * 0.75f

                val (nonZero, total) = binaryImage.countNonZeroCircle(cx, cy, innerR)
                val ratio = if (total > 0) nonZero.toFloat() / total else 0f

                if (ratio >= sensitivity) {
                    filledDigits.add(digit)
                }
                if (ratio > bestRatio) {
                    bestRatio = ratio
                    bestDigit = digit
                }
            }

            val finalDigit = if (filledDigits.size == 1) {
                filledDigits[0]
            } else if (bestRatio >= sensitivity) {
                bestDigit
            } else {
                null
            }

            rollDetections.add(
                RollDetection(
                    column = col,
                    detectedDigits = filledDigits,
                    finalDigit = finalDigit
                )
            )

            rollBuilder.append(finalDigit?.toString() ?: "?")
        }

        val detectedRoll = rollBuilder.toString()
        val isRollComplete = !detectedRoll.contains("?")

        // 2. Parse answer key from exam (supports 1..4 / ক..ঘ / A..D)
        val keyList = List(exam.totalQuestions) { qIdx ->
            if (qIdx < exam.answers.length) {
                OmrLayout.parseOptionIndex(exam.answers[qIdx])
            } else -1
        }

        val questions = mutableListOf<QuestionDetection>()
        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        for (q in 1..exam.totalQuestions) {
            val filledOptions = mutableListOf<Int>()
            val samples = mutableListOf<BubbleSample>()

            // 4 options: ক, খ, গ, ঘ
            for (opt in 0..3) {
                val centerMm = OmrLayout.getAnswerBubbleCenter(q, opt)
                mappedPts[0] = centerMm.x
                mappedPts[1] = centerMm.y
                mappedPts[2] = centerMm.x + OmrLayout.BUBBLE_RADIUS_MM
                mappedPts[3] = centerMm.y
                matrix.mapPoints(mappedPts)

                val cx = mappedPts[0]
                val cy = mappedPts[1]
                val rPx = max(1.5f, hypot(mappedPts[2] - cx, mappedPts[3] - cy))

                // Inner sampling radius (disregard outer border)
                val innerR = rPx * 0.75f

                // OpenCV countNonZero in binaryInv
                val (nonZero, total) = binaryImage.countNonZeroCircle(cx, cy, innerR)
                val fillRatio = if (total > 0) nonZero.toFloat() / total else 0f
                val isFilled = fillRatio >= sensitivity

                samples.add(
                    BubbleSample(
                        optionIndex = opt,
                        centerPixels = PointF(cx, cy),
                        fillRatio = fillRatio,
                        isFilled = isFilled
                    )
                )

                if (isFilled) {
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
            roll = if (detectedRoll.contains("?")) "1001" else detectedRoll,
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
