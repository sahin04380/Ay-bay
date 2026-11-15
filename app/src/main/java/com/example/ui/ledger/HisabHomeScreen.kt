package com.example.ui.ledger

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.ledger.components.SummaryBanner
import com.example.ui.theme.LocalHisabColors
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisabHomeScreen(
  viewModel: LedgerViewModel,
  themeMode: ThemeMode,
  onToggleTheme: () -> Unit,
  onSignOutClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dataState by viewModel.dataState.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val filterState by viewModel.filterState.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var showSignOutDialog by remember { mutableStateOf(false) }

  val invalidMsg = stringResource(R.string.error_invalid_amount_or_date)
  val addedMsg = stringResource(R.string.msg_entry_added)
  val updatedMsg = stringResource(R.string.msg_entry_updated)
  val deletedMsg = stringResource(R.string.msg_entry_deleted)
  val failedMsg = stringResource(R.string.error_loading_entries)
  val isDark = LocalHisabColors.current.isDark

  LaunchedEffect(viewModel) {
    viewModel.snackbarEvents.collectLatest { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  if (formState.isEditing) {
    BackHandler {
      viewModel.cancelEditing()
    }
  } else if (selectedTab == LedgerTab.STATEMENT) {
    BackHandler {
      viewModel.selectTab(LedgerTab.ENTRY)
    }
  }

  if (showSignOutDialog) {
    AlertDialog(
      onDismissRequest = { showSignOutDialog = false },
      title = { Text(stringResource(R.string.sign_out_confirm_title)) },
      text = { Text(stringResource(R.string.sign_out_confirm_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            showSignOutDialog = false
            onSignOutClick()
          },
          modifier = Modifier.testTag("confirm_sign_out_button"),
        ) {
          Text(stringResource(R.string.sign_out))
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutDialog = false }) {
          Text(stringResource(R.string.btn_cancel))
        }
      },
    )
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isCompact = maxWidth < 600.dp
    val isExpandedDualPane = maxWidth >= 880.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.titleLarge,
            )
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
          ),
          actions = {
            IconButton(
              onClick = onToggleTheme,
              modifier = Modifier.testTag("theme_toggle_button"),
            ) {
              Icon(
                imageVector = if (isDark || themeMode == ThemeMode.DARK) {
                  Icons.Rounded.LightMode
                } else {
                  Icons.Rounded.DarkMode
                },
                contentDescription = stringResource(R.string.theme_toggle_desc),
              )
            }
            IconButton(
              onClick = { showSignOutDialog = true },
              modifier = Modifier.testTag("sign_out_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.Logout,
                contentDescription = stringResource(R.string.sign_out),
              )
            }
          },
        )
      },
      bottomBar = {
        if (isCompact) {
          NavigationBar(windowInsets = WindowInsets.navigationBars) {
            NavigationBarItem(
              selected = selectedTab == LedgerTab.ENTRY,
              onClick = { viewModel.selectTab(LedgerTab.ENTRY) },
              icon = {
                Icon(
                  imageVector = if (selectedTab == LedgerTab.ENTRY) {
                    Icons.Rounded.AddCircle
                  } else {
                    Icons.Outlined.AddCircleOutline
                  },
                  contentDescription = null,
                )
              },
              label = {
                Text(
                  text = if (formState.isEditing) {
                    stringResource(R.string.tab_edit_entry)
                  } else {
                    stringResource(R.string.tab_new_entry)
                  }
                )
              },
              modifier = Modifier.testTag("nav_tab_entry"),
            )
            NavigationBarItem(
              selected = selectedTab == LedgerTab.STATEMENT,
              onClick = { viewModel.selectTab(LedgerTab.STATEMENT) },
              icon = {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                  contentDescription = null,
                )
              },
              label = { Text(stringResource(R.string.tab_statement)) },
              modifier = Modifier.testTag("nav_tab_statement"),
            )
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding),
      ) {
        if (!isCompact && !isExpandedDualPane) {
          NavigationRail {
            NavigationRailItem(
              selected = selectedTab == LedgerTab.ENTRY,
              onClick = { viewModel.selectTab(LedgerTab.ENTRY) },
              icon = {
                Icon(
                  imageVector = if (selectedTab == LedgerTab.ENTRY) {
                    Icons.Rounded.AddCircle
                  } else {
                    Icons.Outlined.AddCircleOutline
                  },
                  contentDescription = null,
                )
              },
              label = { Text(stringResource(R.string.tab_new_entry)) },
            )
            NavigationRailItem(
              selected = selectedTab == LedgerTab.STATEMENT,
              onClick = { viewModel.selectTab(LedgerTab.STATEMENT) },
              icon = {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                  contentDescription = null,
                )
              },
              label = { Text(stringResource(R.string.tab_statement)) },
            )
          }
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.TopCenter,
        ) {
          when (val state = dataState) {
            is LedgerDataState.Loading -> {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
              ) {
                CircularProgressIndicator()
              }
            }

            is LedgerDataState.Error -> {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(24.dp),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = state.message,
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodyLarge,
                )
              }
            }

            is LedgerDataState.Success -> {
              Column(
                modifier = Modifier
                  .widthIn(max = if (isExpandedDualPane) 1120.dp else 680.dp)
                  .fillMaxSize()
                  .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                SummaryBanner(summary = state.overallSummary)

                if (isExpandedDualPane) {
                  Row(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                  ) {
                    EntryFormScreen(
                      formState = formState,
                      recentEntries = emptyList(),
                      onTypeChange = viewModel::updateFormType,
                      onDateChange = viewModel::updateFormDate,
                      onAmountChange = viewModel::updateFormAmount,
                      onDescriptionChange = viewModel::updateFormDescription,
                      onSaveClick = {
                        viewModel.saveEntry(invalidMsg, addedMsg, updatedMsg, failedMsg)
                      },
                      onCancelEdit = viewModel::cancelEditing,
                      onEditRecent = viewModel::startEditing,
                      onViewAllStatement = null,
                      modifier = Modifier.weight(0.9f),
                    )
                    StatementScreen(
                      filterState = filterState,
                      rows = state.statementRows,
                      filteredSummary = state.filteredSummary,
                      totalEntryCount = state.totalEntryCount,
                      onStartDateChange = viewModel::updateStartDate,
                      onEndDateChange = viewModel::updateEndDate,
                      onTypeFilterChange = viewModel::updateTypeFilter,
                      onSearchQueryChange = viewModel::updateSearchQuery,
                      onClearFilters = viewModel::clearFilters,
                      onEditEntry = viewModel::startEditing,
                      onDeleteEntry = { id ->
                        viewModel.deleteEntry(id, deletedMsg, failedMsg)
                      },
                      modifier = Modifier.weight(1.1f),
                    )
                  }
                } else {
                  Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                      LedgerTab.ENTRY -> {
                        EntryFormScreen(
                          formState = formState,
                          recentEntries = state.recentEntries,
                          onTypeChange = viewModel::updateFormType,
                          onDateChange = viewModel::updateFormDate,
                          onAmountChange = viewModel::updateFormAmount,
                          onDescriptionChange = viewModel::updateFormDescription,
                          onSaveClick = {
                            viewModel.saveEntry(invalidMsg, addedMsg, updatedMsg, failedMsg)
                          },
                          onCancelEdit = viewModel::cancelEditing,
                          onEditRecent = viewModel::startEditing,
                          onViewAllStatement = { viewModel.selectTab(LedgerTab.STATEMENT) },
                        )
                      }

                      LedgerTab.STATEMENT -> {
                        StatementScreen(
                          filterState = filterState,
                          rows = state.statementRows,
                          filteredSummary = state.filteredSummary,
                          totalEntryCount = state.totalEntryCount,
                          onStartDateChange = viewModel::updateStartDate,
                          onEndDateChange = viewModel::updateEndDate,
                          onTypeFilterChange = viewModel::updateTypeFilter,
                          onSearchQueryChange = viewModel::updateSearchQuery,
                          onClearFilters = viewModel::clearFilters,
                          onEditEntry = viewModel::startEditing,
                          onDeleteEntry = { id ->
                            viewModel.deleteEntry(id, deletedMsg, failedMsg)
                          },
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
