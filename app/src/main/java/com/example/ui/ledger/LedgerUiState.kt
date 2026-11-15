package com.example.ui.ledger

import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.util.BengaliFormatter

enum class LedgerTab {
  ENTRY,
  STATEMENT,
}

enum class TypeFilter {
  ALL,
  INCOME,
  EXPENSE,
}

data class LedgerSummary(
  val totalIncome: Double = 0.0,
  val totalExpense: Double = 0.0,
  val balance: Double = 0.0,
)

data class StatementRow(
  val entry: LedgerEntry,
  val runningBalance: Double,
)

sealed interface LedgerDataState {
  data object Loading : LedgerDataState
  data class Success(
    val overallSummary: LedgerSummary,
    val recentEntries: List<LedgerEntry>,
    val statementRows: List<StatementRow>,
    val filteredSummary: LedgerSummary,
    val totalEntryCount: Int,
  ) : LedgerDataState

  data class Error(val message: String) : LedgerDataState
}

data class EntryFormState(
  val editingEntryId: String? = null,
  val type: EntryType = EntryType.INCOME,
  val dateStr: String = BengaliFormatter.todayIsoDate(),
  val amountText: String = "",
  val description: String = "",
  val isSaving: Boolean = false,
  val validationError: String? = null,
) {
  val isEditing: Boolean
    get() = editingEntryId != null
}

data class StatementFilterState(
  val startDate: String = "",
  val endDate: String = "",
  val typeFilter: TypeFilter = TypeFilter.ALL,
  val searchQuery: String = "",
) {
  val hasActiveFilter: Boolean
    get() = startDate.isNotEmpty() ||
      endDate.isNotEmpty() ||
      typeFilter != TypeFilter.ALL ||
      searchQuery.isNotBlank()
}
