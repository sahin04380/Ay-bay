package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BengaliFormatterTest {

  @Test
  fun parseAmountInput_handlesAsciiAndBengaliDigits() {
    assertEquals(1500.5, BengaliFormatter.parseAmountInput("1500.5") ?: 0.0, 0.001)
    assertEquals(1500.5, BengaliFormatter.parseAmountInput("১৫০০.৫") ?: 0.0, 0.001)
    assertNull(BengaliFormatter.parseAmountInput("0"))
    assertNull(BengaliFormatter.parseAmountInput("-50"))
    assertNull(BengaliFormatter.parseAmountInput("abc"))
  }

  @Test
  fun formatTaka_formatsPositiveAndNegativeWithBengaliNumerals() {
    assertEquals("৳ ১,৫০০.৫", BengaliFormatter.formatTaka(1500.5))
    assertEquals("−৳ ২৫০", BengaliFormatter.formatTaka(-250.0))
  }

  @Test
  fun formatDate_convertsIsoDateToBengaliDdMmYyyy() {
    assertEquals("১৫/১১/২০২৬", BengaliFormatter.formatDate("2026-11-15"))
  }
}
