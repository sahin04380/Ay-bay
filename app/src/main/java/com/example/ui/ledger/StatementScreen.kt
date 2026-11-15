package com.example.ui.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.ui.ledger.components.LedgerDatePickerDialog
import com.example.ui.theme.LocalHisabColors
import com.example.util.BengaliFormatter

private enum class ActiveDateFilterField {
  START,
  END,
}

@Composable
fun StatementScreen(
  filterState: StatementFilterState,
  rows: List<StatementRow>,
  filteredSummary: LedgerSummary,
  totalEntryCount: Int,
  onStartDateChange: (String) -> Unit,
  onEndDateChange: (String) -> Unit,
  onTypeFilterChange: (TypeFilter) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onClearFilters: () -> Unit,
  onEditEntry: (LedgerEntry) -> Unit,
  onDeleteEntry: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hisabColors = LocalHisabColors.current
  var activeDateField by remember { mutableStateOf<ActiveDateFilterField?>(null) }
  var pendingDeleteEntry by remember { mutableStateOf<LedgerEntry?>(null) }

  activeDateField?.let { field ->
    val initialDate = when (field) {
      ActiveDateFilterField.START -> filterState.startDate.ifEmpty { BengaliFormatter.todayIsoDate() }
      ActiveDateFilterField.END -> filterState.endDate.ifEmpty { BengaliFormatter.todayIsoDate() }
    }
    LedgerDatePickerDialog(
      initialIsoDate = initialDate,
      onDateSelected = { selected ->
        when (field) {
          ActiveDateFilterField.START -> onStartDateChange(selected)
          ActiveDateFilterField.END -> onEndDateChange(selected)
        }
      },
      onDismiss = { activeDateField = null },
    )
  }

  pendingDeleteEntry?.let { entry ->
    AlertDialog(
      onDismissRequest = { pendingDeleteEntry = null },
      title = { Text(stringResource(R.string.delete_confirm_title)) },
      text = {
        val descText = entry.description.ifBlank { BengaliFormatter.formatDate(entry.dateStr) }
        Text("${stringResource(R.string.delete_confirm_message)}\n($descText — ${BengaliFormatter.formatTaka(entry.amount)})")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteEntry(entry.id)
            pendingDeleteEntry = null
          },
          modifier = Modifier.testTag("confirm_delete_button"),
        ) {
          Text(
            text = stringResource(R.string.action_delete),
            color = hisabColors.expense,
            fontWeight = FontWeight.Bold,
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingDeleteEntry = null }) {
          Text(stringResource(R.string.btn_cancel))
        }
      },
    )
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item(key = "filters") {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            DateFilterBox(
              label = stringResource(R.string.filter_start_date),
              isoDate = filterState.startDate,
              onClick = { activeDateField = ActiveDateFilterField.START },
              onClear = { onStartDateChange("") },
              modifier = Modifier
                .weight(1f)
                .testTag("filter_start_date"),
            )
            DateFilterBox(
              label = stringResource(R.string.filter_end_date),
              isoDate = filterState.endDate,
              onClick = { activeDateField = ActiveDateFilterField.END },
              onClear = { onEndDateChange("") },
              modifier = Modifier
                .weight(1f)
                .testTag("filter_end_date"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TypeFilterChip(
              label = stringResource(R.string.filter_all),
              selected = filterState.typeFilter == TypeFilter.ALL,
              onClick = { onTypeFilterChange(TypeFilter.ALL) },
              modifier = Modifier.testTag("filter_type_all"),
            )
            TypeFilterChip(
              label = stringResource(R.string.filter_income),
              selected = filterState.typeFilter == TypeFilter.INCOME,
              onClick = { onTypeFilterChange(TypeFilter.INCOME) },
              modifier = Modifier.testTag("filter_type_income"),
            )
            TypeFilterChip(
              label = stringResource(R.string.filter_expense),
              selected = filterState.typeFilter == TypeFilter.EXPENSE,
              onClick = { onTypeFilterChange(TypeFilter.EXPENSE) },
              modifier = Modifier.testTag("filter_type_expense"),
            )
            Spacer(modifier = Modifier.weight(1f))
            if (filterState.hasActiveFilter) {
              TextButton(
                onClick = onClearFilters,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.testTag("clear_filters_button"),
              ) {
                Text(
                  text = stringResource(R.string.filter_clear),
                  style = MaterialTheme.typography.labelMedium,
                )
              }
            }
          }

          OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(stringResource(R.string.filter_search)) },
            placeholder = { Text(stringResource(R.string.filter_search_placeholder)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            },
            trailingIcon = {
              if (filterState.searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                  Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = stringResource(R.string.filter_clear),
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("filter_search_input"),
          )
        }
      }
    }

    if (rows.isEmpty()) {
      item(key = "empty") {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 36.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(44.dp),
            )
            Text(
              text = stringResource(R.string.empty_entries),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = if (totalEntryCount == 0) {
                stringResource(R.string.empty_entries_hint)
              } else {
                stringResource(R.string.empty_filtered_hint)
              },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      items(items = rows, key = { it.entry.id }) { row ->
        StatementRowCard(
          row = row,
          onEdit = { onEditEntry(row.entry) },
          onDelete = { pendingDeleteEntry = row.entry },
        )
      }

      item(key = "totals_footer") {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = if (filterState.hasActiveFilter) {
                stringResource(R.string.label_filtered_total)
              } else {
                stringResource(R.string.label_total_income).let {
                  "${stringResource(R.string.tab_statement)} — মোট (${BengaliFormatter.toBengaliDigits(rows.size.toString())}টি)"
                }
              },
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              FooterStat(
                label = stringResource(R.string.filter_income),
                amount = BengaliFormatter.formatTaka(filteredSummary.totalIncome),
                color = hisabColors.income,
              )
              FooterStat(
                label = stringResource(R.string.filter_expense),
                amount = BengaliFormatter.formatTaka(filteredSummary.totalExpense),
                color = hisabColors.expense,
              )
              FooterStat(
                label = stringResource(R.string.label_balance),
                amount = BengaliFormatter.formatTaka(filteredSummary.balance),
                color = hisabColors.balance,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DateFilterBox(
  label: String,
  isoDate: String,
  onClick: () -> Unit,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.background,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
        Text(
          text = if (isoDate.isEmpty()) {
            stringResource(R.string.filter_select_date)
          } else {
            BengaliFormatter.formatDate(isoDate)
          },
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (isoDate.isEmpty()) FontWeight.Normal else FontWeight.SemiBold,
          color = if (isoDate.isEmpty()) {
            MaterialTheme.colorScheme.onSurfaceVariant
          } else {
            MaterialTheme.colorScheme.onSurface
          },
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      if (isoDate.isNotEmpty()) {
        Icon(
          imageVector = Icons.Rounded.Clear,
          contentDescription = stringResource(R.string.filter_clear),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier
            .size(18.dp)
            .clickable(onClick = onClear),
        )
      } else {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun TypeFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  FilterChip(
    selected = selected,
    onClick = onClick,
    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
    colors = FilterChipDefaults.filterChipColors(
      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    shape = RoundedCornerShape(10.dp),
    modifier = modifier,
  )
}

@Composable
private fun StatementRowCard(
  row: StatementRow,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val hisabColors = LocalHisabColors.current
  val entry = row.entry
  val isIncome = entry.entryType == EntryType.INCOME
  val accentColor = if (isIncome) hisabColors.income else hisabColors.expense
  val badgeBg = if (isIncome) hisabColors.incomeContainer else hisabColors.expenseContainer

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("statement_row_${entry.id}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              color = badgeBg,
              shape = RoundedCornerShape(6.dp),
            ) {
              Text(
                text = if (isIncome) stringResource(R.string.filter_income) else stringResource(R.string.filter_expense),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
              )
            }
            Text(
              text = BengaliFormatter.formatDate(entry.dateStr),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Text(
            text = entry.description.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = (if (isIncome) "+" else "−") + BengaliFormatter.formatTaka(entry.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor,
          )
          Box(
            modifier = Modifier
              .padding(top = 2.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(hisabColors.balanceContainer)
              .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${stringResource(R.string.label_running_balance)}: ${BengaliFormatter.formatTaka(row.runningBalance)}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = hisabColors.balance,
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(
          onClick = onEdit,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("edit_entry_${entry.id}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(stringResource(R.string.action_edit), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(
          onClick = onDelete,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("delete_entry_${entry.id}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteOutline,
            contentDescription = null,
            tint = hisabColors.expense,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.action_delete),
            color = hisabColors.expense,
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun FooterStat(
  label: String,
  amount: String,
  color: androidx.compose.ui.graphics.Color,
) {
  Column(horizontalAlignment = Alignment.Start) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = amount,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = color,
    )
  }
}
