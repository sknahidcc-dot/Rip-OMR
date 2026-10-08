package com.example.util

import java.util.Locale

object BengaliUtils {

    private val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun formatNumber(text: String, useBengaliDigits: Boolean = false): String {
        if (!useBengaliDigits) return text
        val sb = StringBuilder()
        for (c in text) {
            if (c in '0'..'9') {
                sb.append(bnDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, useBengaliDigits: Boolean = false): String {
        return formatNumber(number.toString(), useBengaliDigits)
    }

    fun formatDecimal(value: Double, useBengaliDigits: Boolean = false, decimals: Int = 2): String {
        val s = String.format(Locale.US, "%.${decimals}f", value)
        return formatNumber(s, useBengaliDigits)
    }

    fun optionToBengali(optionIndex: Int): String {
        return when (optionIndex) {
            0 -> "ক"
            1 -> "খ"
            2 -> "গ"
            3 -> "ঘ"
            else -> "-"
        }
    }

    fun digitCharToBengaliOption(ch: Char): String {
        return when (ch) {
            '1', 'ক' -> "ক"
            '2', 'খ' -> "খ"
            '3', 'গ' -> "গ"
            '4', 'ঘ' -> "ঘ"
            else -> "-"
        }
    }
}
