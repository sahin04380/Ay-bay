package com.example.ui.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.ledger.LedgerSummary
import com.example.ui.theme.LocalHisabColors
import com.example.util.BengaliFormatter

@Composable
fun SummaryBanner(
  summary: LedgerSummary,
  modifier: Modifier = Modifier,
) {
  val hisabColors = LocalHisabColors.current
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
        .padding(vertical = 12.dp, horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      SummaryColumn(
        label = stringResource(R.string.label_total_income),
        value = BengaliFormatter.formatTaka(summary.totalIncome),
        valueColor = hisabColors.income,
        modifier = Modifier.weight(1f),
      )
      SummaryDivider()
      SummaryColumn(
        label = stringResource(R.string.label_total_expense),
        value = BengaliFormatter.formatTaka(summary.totalExpense),
        valueColor = hisabColors.expense,
        modifier = Modifier.weight(1f),
      )
      SummaryDivider()
      SummaryColumn(
        label = stringResource(R.string.label_balance),
        value = BengaliFormatter.formatTaka(summary.balance),
        valueColor = hisabColors.balance,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun SummaryDivider() {
  Box(
    modifier = Modifier
      .fillMaxHeight(0.7f)
      .width(1.dp)
      .background(MaterialTheme.colorScheme.outlineVariant)
  )
}

@Composable
private fun SummaryColumn(
  label: String,
  value: String,
  valueColor: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(horizontal = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = valueColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
