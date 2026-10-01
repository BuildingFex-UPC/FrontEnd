package com.example.buildingfexfrontend.finances.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.buildingfexfrontend.core.ui.components.AppButton
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.finances.domain.model.FeeItem
import com.example.buildingfexfrontend.finances.domain.model.Payment
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

@Composable
fun ResidentFinanceScreen(container: AppContainer) {
    val viewModel: ResidentFinanceViewModel =
        appViewModel { ResidentFinanceViewModel(container.residentFinance) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.openCheckoutUrl?.let { checkoutUrl ->
        com.example.buildingfexfrontend.core.ui.components.CheckoutWebView(
            url = checkoutUrl,
            onRedirect = viewModel::onCheckoutRedirect,
            onDismiss = viewModel::consumeCheckoutUrl,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("rfin.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("rfin.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                val account = state.account
                if (account != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.buildingfexfrontend.core.ui.components.StatCard(
                            label = string("rfin.pending"),
                            value = Dates.currency(account.pendingAmount),
                            modifier = Modifier.weight(1f),
                            accent = MaterialTheme.colorScheme.error,
                        )
                        com.example.buildingfexfrontend.core.ui.components.StatCard(
                            label = string("rfin.paidTotal"),
                            value = Dates.currency(account.paidTotal),
                            modifier = Modifier.weight(1f),
                            accent = MaterialTheme.colorScheme.primary,
                        )
                    }
                    VerticalGap(16)

                    SectionCard(title = string("rfin.checkout.title")) {
                        Text(
                            text = string("rfin.checkout.subtitle"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        VerticalGap(12)
                        AppButton(
                            text = if (state.startingCheckout) string("rfin.checkout.opening")
                            else string("rfin.checkout.pay"),
                            onClick = viewModel::startCheckout,
                            loading = state.startingCheckout,
                            enabled = account.hasPending && !state.confirming,
                        )
                    }

                    VerticalGap(16)
                    SectionCard(title = string("rfin.fees.title")) {
                        if (account.fees.isEmpty()) {
                            EmptyState(string("rfin.fees.empty"))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                account.fees.forEach { fee -> FeeRow(fee) }
                            }
                        }
                    }

                    VerticalGap(16)
                    SectionCard(title = string("rfin.history.title")) {
                        if (account.payments.isEmpty()) {
                            EmptyState(string("rfin.history.empty"))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                account.payments.forEach { payment -> PaymentRow(payment) }
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("rfin.title"),
            message = message,
            confirmText = string("rfin.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun FeeRow(fee: FeeItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = if (fee.concept == "Mantenimiento") string("rfin.concept.maintenance") else fee.concept,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = string("rfin.fees.row")
                    .replace(
                        "{month}",
                        if (fee.month == "Mes actual") string("rfin.month.current") else fee.month,
                    )
                    .replace("{date}", Dates.displayDate(fee.dueDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = Dates.currency(fee.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        StatusChip(
            text = if (fee.isPaid) string("rfin.status.paid") else string("rfin.status.pending"),
            container = if (fee.isPaid) BfSuccessContainer else BfWarningContainer,
            content = if (fee.isPaid) BfSuccess else BfWarning,
        )
    }
}

@Composable
private fun PaymentRow(payment: Payment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = payment.concept.let {
                    when {
                        it == null -> string("rfin.payment.fallbackConcept")
                        it == "Cuota mantenimiento" -> string("rfin.concept.fee")
                        else -> it
                    }
                },
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${payment.feeMonth ?: ""} · ${payment.method} · ${Dates.displayDateTime(payment.paidAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = Dates.currency(payment.amount),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
