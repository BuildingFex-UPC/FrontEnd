package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
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
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.socialspaces.domain.model.Guest
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.Space
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer

/** Admin reservation oversight + guest check-in (port of AppGenerationView). */
@Composable
fun GenerationScreen(container: AppContainer) {
    val viewModel: GenerationViewModel = appViewModel {
        GenerationViewModel(container.reservations, container.spaces)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("gen.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("gen.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                if (state.reservations.isEmpty()) {
                    SectionCard(title = string("gen.reservationsTitle")) {
                        EmptyState(string("gen.empty"))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.reservations.forEach { reservation ->
                            ReservationCard(
                                reservation = reservation,
                                spacesById = state.spacesById,
                                savingKey = state.savingKey,
                                onToggle = viewModel::toggleCheckIn,
                            )
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }
}

@Composable
private fun ReservationCard(
    reservation: Reservation,
    spacesById: Map<String, Space>,
    savingKey: String?,
    onToggle: (Reservation, Guest, Boolean) -> Unit,
) {
    val spaceName = spacesById[reservation.spaceId]?.name ?: string("gen.spaceFallback")
    OutlinedCardBox {
        Column {
            Text(spaceName, fontWeight = FontWeight.SemiBold)
            Text(
                text = string("gen.resident")
                    .replace("{name}", reservation.residentName.ifBlank { "—" }) +
                    if (reservation.residentCode.isNotBlank()) " (${reservation.residentCode})" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${Dates.displayDate(reservation.date)} · ${reservation.startTime} – ${reservation.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(8)
            Text(
                text = string("gen.guests"),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (reservation.guests.isEmpty()) {
                Text(
                    text = string("gen.noGuests"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column {
                    reservation.guests.forEach { guest ->
                        val key = "${reservation.id}:${guest.id}"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = guest.checkedIn,
                                enabled = savingKey == null || savingKey == key,
                                onCheckedChange = { checked ->
                                    onToggle(reservation, guest, checked)
                                },
                            )
                            Text(
                                text = guest.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            StatusChip(
                                text = if (guest.checkedIn) string("gen.checkedIn") else string("gen.notCheckedIn"),
                                container = if (guest.checkedIn) BfSuccessContainer else BfWarningContainer,
                                content = if (guest.checkedIn) BfSuccess else BfWarning,
                            )
                        }
                    }
                }
            }
        }
    }
}
