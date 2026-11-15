package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object BengaliFormatter {
  private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

  fun toBengaliDigits(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      if (ch in '0'..'9') {
        sb.append(bengaliDigits[ch - '0'])
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun toAsciiDigits(input: String): String {
    val sb = StringBuilder(input.length)
    for (ch in input) {
      val idx = bengaliDigits.indexOf(ch)
      if (idx >= 0) {
        sb.append(('0'.code + idx).toChar())
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun parseAmountInput(raw: String): Double? {
    val normalized = toAsciiDigits(raw.trim()).replace(",", "")
    if (normalized.isEmpty()) return null
    val value = normalized.toDoubleOrNull() ?: return null
    return if (value.isFinite() && value > 0.0) value else null
  }

  fun formatAmount(amount: Double): String {
    val negative = amount < -1e-9
    val absVal = kotlin.math.abs(amount)
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = DecimalFormat("#,##0.##", symbols)
    val formatted = formatter.format(absVal)
    val bn = toBengaliDigits(formatted)
    return if (negative) "−$bn" else bn
  }

  fun formatTaka(amount: Double): String {
    val negative = amount < -1e-9
    val absVal = kotlin.math.abs(amount)
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = DecimalFormat("#,##0.##", symbols)
    val bn = toBengaliDigits(formatter.format(absVal))
    return if (negative) "−৳ $bn" else "৳ $bn"
  }

  fun formatDate(isoDate: String): String {
    val parts = isoDate.split("-")
    if (parts.size != 3) return toBengaliDigits(isoDate)
    return toBengaliDigits("${parts[2]}/${parts[1]}/${parts[0]}")
  }

  fun todayIsoDate(): String {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
  }

  fun isoDateFromUtcMillis(utcMillis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = utcMillis
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
  }

  fun utcMillisFromIsoDate(isoDate: String): Long? {
    val parts = isoDate.split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(y, m - 1, d, 0, 0, 0)
    return cal.timeInMillis
  }
}
