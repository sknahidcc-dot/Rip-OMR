package com.example

import com.example.data.model.ExamEntity
import com.example.omr.OmrLayout
import com.example.util.BengaliUtils
import com.example.util.QrUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testOmrLayoutFormulas() {
        val roll00 = OmrLayout.getRollBubbleCenter(0, 0)
        assertEquals(20.0f, roll00.x, 0.001f)
        assertEquals(56.0f, roll00.y, 0.001f)

        val roll39 = OmrLayout.getRollBubbleCenter(3, 9)
        assertEquals(56.0f, roll39.x, 0.001f)
        assertEquals(102.8f, roll39.y, 0.001f)

        val q1Opt0 = OmrLayout.getAnswerBubbleCenter(1, 0)
        assertEquals(26.5f, q1Opt0.x, 0.001f)
        assertEquals(133.75f, q1Opt0.y, 0.001f)

        val q100Opt3 = OmrLayout.getAnswerBubbleCenter(100, 3)
        assertEquals(191.5f, q100Opt3.x, 0.001f)
        assertEquals(265.75f, q100Opt3.y, 0.001f)
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
