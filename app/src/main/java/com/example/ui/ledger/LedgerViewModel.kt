package com.example.ui.ledger

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.data.repository.LedgerRepository
import com.example.util.BengaliFormatter
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LedgerViewModel(
  private val repository: LedgerRepository,
  private val currentUserId: String,
) : ViewModel() {

  private val _selectedTab = MutableStateFlow(LedgerTab.ENTRY)
  val selectedTab: StateFlow<LedgerTab> = _selectedTab.asStateFlow()

  private val _formState = MutableStateFlow(EntryFormState())
  val formState: StateFlow<EntryFormState> = _formState.asStateFlow()

  private val _filterState = MutableStateFlow(StatementFilterState())
  val filterState: StateFlow<StatementFilterState> = _filterState.asStateFlow()

  private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
  val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

  val dataState: StateFlow<LedgerDataState> = combine<List<LedgerEntry>, StatementFilterState, LedgerDataState>(
    repository.observeEntries(currentUserId),
    _filterState,
  ) { entries, filters ->
    buildSuccessState(entries, filters)
  }
    .catch { error ->
      val firestoreError = generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
      if (firestoreError == null && error is CancellationException) throw error
      val rootError = firestoreError ?: error
      Log.w(TAG, "Error observing ledger entries", rootError)
      emit(LedgerDataState.Error(rootError.localizedMessage ?: "হিসাব লোড করতে সমস্যা হয়েছে"))
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = LedgerDataState.Loading,
    )

  private fun buildSuccessState(
    entries: List<LedgerEntry>,
    filters: StatementFilterState,
  ): LedgerDataState.Success {
    var totalIncome = 0.0
    var totalExpense = 0.0
    for (entry in entries) {
      if (entry.entryType == EntryType.INCOME) {
        totalIncome += entry.amount
      } else {
        totalExpense += entry.amount
      }
    }

    val sortedChronological = entries.sortedWith(
      compareBy<LedgerEntry> { it.dateStr }
        .thenBy { it.clientOrder }
        .thenBy { it.id }
    )

    var runningBalance = 0.0
    val allRows = sortedChronological.map { entry ->
      runningBalance += if (entry.entryType == EntryType.INCOME) entry.amount else -entry.amount
      StatementRow(entry = entry, runningBalance = runningBalance)
    }

    val searchLower = filters.searchQuery.trim().lowercase()
    val filteredRows = allRows.filter { row ->
      val e = row.entry
      val matchesStart = filters.startDate.isEmpty() || e.dateStr >= filters.startDate
      val matchesEnd = filters.endDate.isEmpty() || e.dateStr <= filters.endDate
      val matchesType = when (filters.typeFilter) {
        TypeFilter.ALL -> true
        TypeFilter.INCOME -> e.entryType == EntryType.INCOME
        TypeFilter.EXPENSE -> e.entryType == EntryType.EXPENSE
      }
      val matchesSearch = searchLower.isEmpty() ||
        e.description.lowercase().contains(searchLower)
      matchesStart && matchesEnd && matchesType && matchesSearch
    }

    var filteredIncome = 0.0
    var filteredExpense = 0.0
    for (row in filteredRows) {
      if (row.entry.entryType == EntryType.INCOME) {
        filteredIncome += row.entry.amount
      } else {
        filteredExpense += row.entry.amount
      }
    }

    val recentEntries = sortedChronological.asReversed().take(5)

    return LedgerDataState.Success(
      overallSummary = LedgerSummary(
        totalIncome = totalIncome,
        totalExpense = totalExpense,
        balance = totalIncome - totalExpense,
      ),
      recentEntries = recentEntries,
      statementRows = filteredRows,
      filteredSummary = LedgerSummary(
        totalIncome = filteredIncome,
        totalExpense = filteredExpense,
        balance = filteredIncome - filteredExpense,
      ),
      totalEntryCount = entries.size,
    )
  }

  fun selectTab(tab: LedgerTab) {
    _selectedTab.value = tab
  }

  fun updateFormType(type: EntryType) {
    _formState.update { it.copy(type = type, validationError = null) }
  }

  fun updateFormDate(isoDate: String) {
    _formState.update { it.copy(dateStr = isoDate, validationError = null) }
  }

  fun updateFormAmount(amountText: String) {
    _formState.update { it.copy(amountText = amountText, validationError = null) }
  }

  fun updateFormDescription(description: String) {
    val clipped = if (description.length > 300) description.take(300) else description
    _formState.update { it.copy(description = clipped, validationError = null) }
  }

  fun startEditing(entry: LedgerEntry) {
    val cleanAmount = if (entry.amount % 1.0 == 0.0) {
      entry.amount.toLong().toString()
    } else {
      entry.amount.toString()
    }
    _formState.value = EntryFormState(
      editingEntryId = entry.id,
      type = entry.entryType,
      dateStr = entry.dateStr,
      amountText = cleanAmount,
      description = entry.description,
      isSaving = false,
      validationError = null,
    )
    _selectedTab.value = LedgerTab.ENTRY
  }

  fun cancelEditing() {
    _formState.value = EntryFormState(
      dateStr = BengaliFormatter.todayIsoDate(),
    )
  }

  fun saveEntry(
    invalidInputMsg: String,
    addedMsg: String,
    updatedMsg: String,
    failedMsg: String,
  ) {
    val current = _formState.value
    val amount = BengaliFormatter.parseAmountInput(current.amountText)
    if (amount == null || current.dateStr.isBlank()) {
      _formState.update { it.copy(validationError = invalidInputMsg) }
      return
    }

    _formState.update { it.copy(isSaving = true, validationError = null) }
    viewModelScope.launch {
      val editingId = current.editingEntryId
      val result = if (editingId != null) {
        repository.updateEntry(
          entryId = editingId,
          type = current.type,
          amount = amount,
          dateStr = current.dateStr,
          description = current.description,
        )
      } else {
        repository.addEntry(
          type = current.type,
          amount = amount,
          dateStr = current.dateStr,
          description = current.description,
        ).map { Unit }
      }

      if (result.isSuccess) {
        _formState.value = EntryFormState(dateStr = BengaliFormatter.todayIsoDate())
        _snackbarEvents.tryEmit(if (editingId != null) updatedMsg else addedMsg)
      } else {
        _formState.update {
          it.copy(
            isSaving = false,
            validationError = result.exceptionOrNull()?.localizedMessage ?: failedMsg,
          )
        }
      }
    }
  }

  fun deleteEntry(entryId: String, deletedMsg: String, failedMsg: String) {
    viewModelScope.launch {
      val result = repository.deleteEntry(entryId)
      if (result.isSuccess) {
        if (_formState.value.editingEntryId == entryId) {
          cancelEditing()
        }
        _snackbarEvents.tryEmit(deletedMsg)
      } else {
        _snackbarEvents.tryEmit(result.exceptionOrNull()?.localizedMessage ?: failedMsg)
      }
    }
  }

  fun updateStartDate(isoDate: String) {
    _filterState.update { it.copy(startDate = isoDate) }
  }

  fun updateEndDate(isoDate: String) {
    _filterState.update { it.copy(endDate = isoDate) }
  }

  fun updateTypeFilter(filter: TypeFilter) {
    _filterState.update { it.copy(typeFilter = filter) }
  }

  fun updateSearchQuery(query: String) {
    _filterState.update { it.copy(searchQuery = query) }
  }

  fun clearFilters() {
    _filterState.value = StatementFilterState()
  }

  private companion object {
    const val TAG = "LedgerVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
