package com.example.buildingfexfrontend.finances.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppDialog
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.DateField
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfSuccess

@Composable
fun CollectionsScreen(container: AppContainer) {
    val viewModel: CollectionsViewModel = appViewModel { CollectionsViewModel(container.collections) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("coll.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("coll.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                SectionCard(title = string("coll.summary.title")) {
                    OutlinedCardBox {
                        Column {
                            Text(
                                text = string("coll.summary.balance"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = Dates.currency(state.balanceAfterAll),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (state.balanceAfterAll > 0) BfSuccess else BfError,
                            )
                            VerticalGap(10)
                            SummaryRow(string("coll.summary.gross"), state.grossCollected, BfSuccess)
                            SummaryRow(string("coll.summary.expenses"), state.totalCosts, BfError)
                            SummaryRow(string("coll.summary.shared"), state.totalSharedServices, BfError)
                            SummaryRow(string("coll.summary.fixed"), state.totalFixedPaid, BfError)
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(
                    title = string("coll.expenses.title"),
                    actions = { TextButton(onClick = { viewModel.openDialog(CollectionsDialog.EXPENSE) }) { Text(string("coll.new")) } },
                ) {
                    if (state.expenses.isEmpty()) {
                        EmptyState(string("coll.expenses.empty"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.expenses.forEach { expense ->
                                OutlinedCardBox {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(expense.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = Dates.displayDate(expense.purchaseDate),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = Dates.currency(expense.amount),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(
                    title = string("coll.shared.title"),
                    actions = { TextButton(onClick = { viewModel.openDialog(CollectionsDialog.SHARED_SERVICE) }) { Text(string("coll.new")) } },
                ) {
                    if (state.sharedServices.isEmpty()) {
                        EmptyState(string("coll.shared.empty"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.sharedServices.forEach { service ->
                                OutlinedCardBox {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                string(if (service.type == "water") "coll.type.water" else "coll.type.luz"),
                                                fontWeight = FontWeight.Medium,
                                            )
                                            val split = if (service.residentCount > 0) {
                                                string("coll.shared.split")
                                                    .replace("{n}", service.residentCount.toString())
                                                    .replace("{share}", Dates.currency(service.perResidentShare))
                                            } else {
                                                string("coll.shared.noResidents")
                                            }
                                            Text(
                                                text = string("coll.shared.row")
                                                    .replace("{month}", service.month)
                                                    .replace("{split}", split),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = Dates.currency(service.amount),
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(
                    title = string("coll.payouts.title"),
                    actions = { TextButton(onClick = { viewModel.openDialog(CollectionsDialog.PAYOUT) }) { Text(string("coll.new")) } },
                ) {
                    if (state.payouts.isEmpty()) {
                        EmptyState(string("coll.payouts.empty"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.payouts.forEach { payout ->
                                OutlinedCardBox {
                                    Column {
                                        Text(payout.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = string("coll.payouts.dni")
                                                .replace("{dni}", payout.dni)
                                                .replace("{phone}", payout.phone),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = string("coll.payouts.salary")
                                                .replace("{salary}", Dates.currency(payout.salary))
                                                .replace("{days}", payout.intervalDays.toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }

    when (state.dialog) {
        CollectionsDialog.EXPENSE -> ExpenseDialog(state, viewModel)
        CollectionsDialog.SHARED_SERVICE -> SharedServiceDialog(state, viewModel)
        CollectionsDialog.PAYOUT -> PayoutDialog(state, viewModel)
        CollectionsDialog.NONE -> Unit
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("coll.dialog.title"),
            message = message,
            confirmText = string("coll.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun ExpenseDialog(state: CollectionsUiState, viewModel: CollectionsViewModel) {
    AppDialog(
        title = string("coll.expenseDialog.title"),
        onDismiss = viewModel::closeDialog,
        onConfirm = viewModel::submit,
        busy = state.busy,
    ) {
        AppTextField(
            value = state.expenseName,
            onValueChange = { viewModel.onFieldChange("expenseName", it) },
            label = string("coll.expenseDialog.name"),
        )
        VerticalGap(12)
        AppTextField(
            value = state.expenseAmount,
            onValueChange = { viewModel.onFieldChange("expenseAmount", it) },
            label = string("coll.field.amount"),
        )
        VerticalGap(12)
        DateField(
            value = state.expenseDate,
            onValueChange = { viewModel.onFieldChange("expenseDate", it) },
            label = string("coll.expenseDialog.date"),
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = Dates.currency(value),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
private fun SharedServiceDialog(state: CollectionsUiState, viewModel: CollectionsViewModel) {
    AppDialog(
        title = string("coll.sharedDialog.title"),
        onDismiss = viewModel::closeDialog,
        onConfirm = viewModel::submit,
        busy = state.busy,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.sharedType == "water",
                onClick = { viewModel.onFieldChange("sharedType", "water") },
                label = { Text(string("coll.type.water")) },
            )
            FilterChip(
                selected = state.sharedType == "electricity",
                onClick = { viewModel.onFieldChange("sharedType", "electricity") },
                label = { Text(string("coll.type.luz")) },
            )
        }
        VerticalGap(12)
        AppTextField(
            value = state.sharedAmount,
            onValueChange = { viewModel.onFieldChange("sharedAmount", it) },
            label = string("coll.field.amount"),
        )
    }
}

@Composable
private fun PayoutDialog(state: CollectionsUiState, viewModel: CollectionsViewModel) {
    AppDialog(
        title = string("coll.payoutDialog.title"),
        onDismiss = viewModel::closeDialog,
        onConfirm = viewModel::submit,
        busy = state.busy,
    ) {
        AppTextField(
            value = state.payoutName,
            onValueChange = { viewModel.onFieldChange("payoutName", it) },
            label = string("coll.payoutDialog.name"),
        )
        VerticalGap(12)
        AppTextField(
            value = state.payoutDni,
            onValueChange = { viewModel.onFieldChange("payoutDni", it) },
            label = string("coll.payoutDialog.id"),
        )
        VerticalGap(12)
        AppTextField(
            value = state.payoutPhone,
            onValueChange = { viewModel.onFieldChange("payoutPhone", it) },
            label = string("coll.payoutDialog.phone"),
        )
        VerticalGap(12)
        AppTextField(
            value = state.payoutSalary,
            onValueChange = { viewModel.onFieldChange("payoutSalary", it) },
            label = string("coll.payoutDialog.salary"),
        )
        VerticalGap(12)
        AppTextField(
            value = state.payoutInterval,
            onValueChange = { viewModel.onFieldChange("payoutInterval", it) },
            label = string("coll.payoutDialog.interval"),
        )
    }
}
