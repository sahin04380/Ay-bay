package com.example.ui.ledger.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDatePickerDialog(
  initialIsoDate: String,
  onDateSelected: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val initialMillis = BengaliFormatter.utcMillisFromIsoDate(initialIsoDate)
    ?: BengaliFormatter.utcMillisFromIsoDate(BengaliFormatter.todayIsoDate())
  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          val selected = datePickerState.selectedDateMillis
          if (selected != null) {
            onDateSelected(BengaliFormatter.isoDateFromUtcMillis(selected))
          }
          onDismiss()
        }
      ) {
        Text(stringResource(R.string.btn_ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.btn_cancel))
      }
    },
  ) {
    DatePicker(state = datePickerState)
  }
}
