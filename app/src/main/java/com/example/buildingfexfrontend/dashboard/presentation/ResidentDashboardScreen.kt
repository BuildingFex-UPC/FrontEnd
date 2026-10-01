package com.example.buildingfexfrontend.dashboard.presentation

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
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.information.presentation.AnnouncementList
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfErrorContainer
import com.example.buildingfexfrontend.ui.theme.BfPrimary
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

@Composable
fun ResidentDashboardScreen(container: AppContainer) {
    val viewModel: ResidentDashboardViewModel = appViewModel {
        ResidentDashboardViewModel(
            finance = container.residentFinance,
            announcements = container.announcements,
            incidents = container.incidents,
            reservations = container.reservations,
            spaces = container.spaces,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profileName = container.session.current()?.profile?.name.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("dash.greeting")
                .replace("{name}", profileName.ifBlank { string("dash.residentFallback") }),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("dash.residentSubtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                val summary = state.financeSummary

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = string("dash.pendingBalance"),
                        value = Dates.currency(summary.pending),
                        modifier = Modifier.weight(1f),
                        accent = if (summary.overdue > 0) MaterialTheme.colorScheme.error else BfPrimary,
                    )
                    StatCard(
                        label = string("dash.openIncidentsLabel"),
                        value = state.openIncidents.toString(),
                        modifier = Modifier.weight(1f),
                        accent = BfWarning,
                    )
                }
                VerticalGap(8)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        label = string("dash.todayReservations"),
                        value = state.todayReservations.size.toString(),
                        modifier = Modifier.weight(1f),
                        accent = BfPrimary,
                    )
                    StatCard(
                        label = string("dash.activeAnnouncements"),
                        value = state.activeAnnouncements.size.toString(),
                        modifier = Modifier.weight(1f),
                        accent = BfSuccess,
                    )
                }

                VerticalGap(16)
                SectionCard(title = string("dash.paymentsTitle")) {
                    if (summary.total <= 0.0) {
                        EmptyState(string("dash.noFees"))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            DonutChart(
                                slices = listOf(
                                    string("dash.slicePaid") to summary.paid,
                                    string("dash.slicePending") to summary.pending - summary.overdue,
                                    string("dash.sliceOverdue") to summary.overdue,
                                ),
                            )
                            if (summary.overdue > 0) {
                                Text(
                                    text = string("dash.overdueNotice")
                                        .replace("{amount}", Dates.currency(summary.overdue)),
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                VerticalGap(16)
                SectionCard(title = string("dash.incidentsTitle")) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(
                            text = string("dash.chipOpen").replace("{n}", state.openIncidents.toString()),
                            container = BfErrorContainer,
                            content = BfError,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip(
                            text = string("dash.chipInProgress").replace("{n}", state.inProgressIncidents.toString()),
                            container = BfWarningContainer,
                            content = BfWarning,
                            modifier = Modifier.weight(1f),
                        )
                        StatusChip(
                            text = string("dash.chipResolved").replace("{n}", state.resolvedIncidents.toString()),
                            container = BfSuccessContainer,
                            content = BfSuccess,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                VerticalGap(16)
                SectionCard(title = string("dash.upcomingReservations")) {
                    val upcoming = state.todayReservations + state.upcomingReservations
                    if (upcoming.isEmpty()) {
                        EmptyState(string("dash.noUpcomingReservations"))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcoming.take(3).forEach { reservation ->
                                OutlinedCardBox {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = state.spacesById[reservation.spaceId]?.name
                                                    ?: string("dash.defaultSpace"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            Text(
                                                text = "${Dates.displayDate(reservation.date)} · " +
                                                    "${reservation.startTime}–${reservation.endTime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (reservation.date == Dates.todayYmd()) {
                                            StatusChip(
                                                text = string("dash.today"),
                                                container = BfPrimary.copy(alpha = 0.12f),
                                                content = BfPrimary,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.activeAnnouncements.isNotEmpty()) {
                    VerticalGap(16)
                    SectionCard(title = string("dash.announcementsTitle")) {
                        AnnouncementList(items = state.activeAnnouncements)
                    }
                }
            }
        }
        VerticalGap(24)
    }
}
