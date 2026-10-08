package com.example

import com.example.data.model.ExamEntity
import com.example.omr.OmrLayout
import com.example.omr.OmrScannerEngine
import com.example.omr.OmrSheetGenerator
import com.example.util.BengaliUtils
import com.example.util.QrUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testOmrLayoutFormulas() {
        val q1Opt0 = OmrLayout.getAnswerBubbleCenter(1, 0)
        assertEquals(10.0f + 15.0f, q1Opt0.x, 0.01f)
        assertEquals(114.0f + 7.5f + 5.9f * 0 + 2.95f, q1Opt0.y, 0.01f)

        val q100Opt3 = OmrLayout.getAnswerBubbleCenter(100, 3)
        assertEquals(154.0f + 15.0f + 8.0f * 3, q100Opt3.x, 0.01f)
        assertEquals(114.0f + 7.5f + 5.9f * 24 + 2.95f, q100Opt3.y, 0.01f)

        val roll0Digit0 = OmrLayout.getRollBubbleCenter(0, 0)
        assertEquals(138.0f, roll0Digit0.x, 0.01f)
        assertEquals(54.0f, roll0Digit0.y, 0.01f)

        val roll3Digit9 = OmrLayout.getRollBubbleCenter(3, 9)
        assertEquals(138.0f + 16.0f * 3, roll3Digit9.x, 0.01f)
        assertEquals(54.0f + 5.5f * 9, roll3Digit9.y, 0.01f)
    }

    @Test
    fun testBitmapGeneration() {
        // Test Bitmap generation (Blank)
        val blankSheet = OmrSheetGenerator.generateSheet(scaleDpi = 100, fillSampleAnswers = false)
        assertNotNull(blankSheet)
        assertTrue(blankSheet.width > 0)
        assertTrue(blankSheet.height > 0)

        // Test Bitmap generation (Sample filled)
        val sampleSheet = OmrSheetGenerator.generateSheet(
            scaleDpi = 100,
            fillSampleAnswers = true,
            sampleRoll = "1248"
        )
        assertNotNull(sampleSheet)
        assertTrue(sampleSheet.width > 0)
        assertTrue(sampleSheet.height > 0)
    }

    @Test
    fun testQrEncodingAndDecoding() {
        val exam = ExamEntity(
            id = 1,
            name = "টেস্ট পরীক্ষা",
            date = "2026-10-08",
            totalQuestions = 100,
            passPercent = 80.0,
            negativeMark = 0.25,
            answers = "12341234"
        )
        val json = QrUtils.encodeExamToJson(exam)
        val decoded = QrUtils.decodeExamFromJson(json)

        assertNotNull(decoded)
        assertEquals("টেস্ট পরীক্ষা", decoded?.name)
        assertEquals(100, decoded?.totalQuestions)
        assertEquals(80.0, decoded?.passPercent ?: 0.0, 0.01)
        assertEquals(0.25, decoded?.negativeMark ?: 0.0, 0.01)
        assertEquals("12341234", decoded?.answers)
    }

    @Test
    fun testBengaliNumberFormatting() {
        assertEquals("১২৩৪", BengaliUtils.formatNumber(1234, useBengaliDigits = true))
        assertEquals("1234", BengaliUtils.formatNumber(1234, useBengaliDigits = false))
        assertEquals("৭৫.৫", BengaliUtils.formatDecimal(75.5, useBengaliDigits = true, decimals = 1))
    }
}
