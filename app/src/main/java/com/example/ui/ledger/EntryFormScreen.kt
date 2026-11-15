package com.example.ui.ledger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.example.ui.ledger.components.LedgerDatePickerDialog
import com.example.ui.theme.LocalHisabColors
import com.example.util.BengaliFormatter

private val quickDescriptionSuggestions = listOf(
  "পণ্য বিক্রি",
  "দোকান ভাড়া",
  "বেতন",
  "বাজার খরচ",
  "বিদ্যুৎ বিল",
  "মালামাল ক্রয়",
)

@Composable
fun EntryFormScreen(
  formState: EntryFormState,
  recentEntries: List<LedgerEntry>,
  onTypeChange: (EntryType) -> Unit,
  onDateChange: (String) -> Unit,
  onAmountChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onSaveClick: () -> Unit,
  onCancelEdit: () -> Unit,
  onEditRecent: (LedgerEntry) -> Unit,
  onViewAllStatement: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val hisabColors = LocalHisabColors.current
  val focusManager = LocalFocusManager.current
  var showDatePicker by remember { mutableStateOf(false) }

  if (showDatePicker) {
    LedgerDatePickerDialog(
      initialIsoDate = formState.dateStr,
      onDateSelected = onDateChange,
      onDismiss = { showDatePicker = false },
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          TypeToggleButton(
            label = stringResource(R.string.type_income),
            selected = formState.type == EntryType.INCOME,
            activeBg = hisabColors.income,
            activeFg = Color.White,
            icon = Icons.Rounded.Add,
            onClick = { onTypeChange(EntryType.INCOME) },
            modifier = Modifier
              .weight(1f)
              .testTag("type_income_toggle"),
          )
          TypeToggleButton(
            label = stringResource(R.string.type_expense),
            selected = formState.type == EntryType.EXPENSE,
            activeBg = hisabColors.expense,
            activeFg = Color.White,
            icon = Icons.Rounded.Remove,
            onClick = { onTypeChange(EntryType.EXPENSE) },
            modifier = Modifier
              .weight(1f)
              .testTag("type_expense_toggle"),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
              value = BengaliFormatter.formatDate(formState.dateStr),
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.label_date)) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.CalendarToday,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth(),
            )
            Box(
              modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable { showDatePicker = true }
                .testTag("date_picker_field")
            )
          }

          OutlinedTextField(
            value = formState.amountText,
            onValueChange = onAmountChange,
            label = { Text(stringResource(R.string.label_amount)) },
            placeholder = { Text(stringResource(R.string.placeholder_amount)) },
            prefix = {
              Text(
                text = "${stringResource(R.string.currency_symbol)} ",
                fontWeight = FontWeight.Bold,
                color = if (formState.type == EntryType.INCOME) hisabColors.income else hisabColors.expense,
              )
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Decimal,
              imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("amount_input"),
          )
        }

        OutlinedTextField(
          value = formState.description,
          onValueChange = onDescriptionChange,
          label = { Text(stringResource(R.string.label_description)) },
          placeholder = { Text(stringResource(R.string.placeholder_description)) },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              focusManager.clearFocus()
              onSaveClick()
            }
          ),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("description_input"),
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = stringResource(R.string.quick_suggestions),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            quickDescriptionSuggestions.forEach { suggestion ->
              SuggestionChip(
                onClick = { onDescriptionChange(suggestion) },
                label = { Text(suggestion, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(10.dp),
              )
            }
          }
        }

        AnimatedVisibility(visible = formState.validationError != null) {
          Surface(
            color = hisabColors.expenseContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = formState.validationError.orEmpty(),
              color = hisabColors.expense,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
          }
        }

        Button(
          onClick = {
            focusManager.clearFocus()
            onSaveClick()
          },
          enabled = !formState.isSaving,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
          contentPadding = PaddingValues(vertical = 14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("save_entry_button"),
        ) {
          if (formState.isSaving) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.dp,
            )
          } else {
            Icon(
              imageVector = if (formState.isEditing) Icons.Rounded.Check else Icons.Rounded.Add,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (formState.isEditing) {
                stringResource(R.string.btn_update_entry)
              } else {
                stringResource(R.string.btn_add_entry)
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        if (formState.isEditing) {
          OutlinedButton(
            onClick = {
              focusManager.clearFocus()
              onCancelEdit()
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize()
              .testTag("cancel_edit_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.btn_cancel),
              style = MaterialTheme.typography.labelLarge,
            )
          }
        }
      }
    }

    if (recentEntries.isNotEmpty()) {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.recent_entries_title),
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            if (onViewAllStatement != null) {
              TextButton(
                onClick = onViewAllStatement,
                modifier = Modifier.testTag("view_all_statement_button"),
              ) {
                Text(stringResource(R.string.view_all_statement))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }

          recentEntries.forEachIndexed { index, entry ->
            if (index > 0) {
              HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 6.dp),
              )
            }
            val isIncome = entry.entryType == EntryType.INCOME
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onEditRecent(entry) }
                .padding(vertical = 6.dp, horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = entry.description.ifBlank { "—" },
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = BengaliFormatter.formatDate(entry.dateStr),
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = (if (isIncome) "+" else "−") + BengaliFormatter.formatTaka(entry.amount),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isIncome) hisabColors.income else hisabColors.expense,
                )
                Icon(
                  imageVector = Icons.Rounded.Edit,
                  contentDescription = stringResource(R.string.action_edit),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}

@Composable
private fun TypeToggleButton(
  label: String,
  selected: Boolean,
  activeBg: Color,
  activeFg: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bgColor by animateColorAsState(
    targetValue = if (selected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    label = "toggleBg",
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) activeFg else MaterialTheme.colorScheme.onSurface,
    label = "toggleFg",
  )

  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    color = bgColor,
    border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier
      .height(50.dp)
      .minimumInteractiveComponentSize(),
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = contentColor,
      )
    }
  }
}
