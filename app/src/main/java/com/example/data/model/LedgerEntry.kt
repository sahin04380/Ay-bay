package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class EntryType(val wireValue: String) {
  INCOME("in"),
  EXPENSE("out");

  companion object {
    fun fromWire(value: String): EntryType =
      if (value == EXPENSE.wireValue) EXPENSE else INCOME
  }
}

@IgnoreExtraProperties
data class LedgerEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val type: String = EntryType.INCOME.wireValue,
  val amount: Double = 0.0,
  val dateStr: String = "",
  val description: String = "",
  val clientOrder: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val entryType: EntryType
    get() = EntryType.fromWire(type)
}
