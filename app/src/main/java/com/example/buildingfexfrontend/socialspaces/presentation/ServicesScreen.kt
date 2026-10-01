package com.example.buildingfexfrontend.socialspaces.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppDialog
import com.example.buildingfexfrontend.core.ui.components.AppImage
import com.example.buildingfexfrontend.core.ui.components.AppOutlinedButton
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.DateField
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FormError
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.TimeField
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.socialspaces.domain.model.Space

/** Resident services catalog: browse spaces and reserve a slot. */
@Composable
fun ServicesScreen(container: AppContainer) {
    val viewModel: ServicesViewModel = appViewModel {
        ServicesViewModel(container.spaces, container.reservations)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("services.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("services.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                if (state.spaces.isEmpty()) {
                    SectionCard(title = string("services.availableTitle")) {
                        EmptyState(string("services.empty"))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.spaces.forEach { space ->
                            OutlinedCardBox {
                                Column {
                                    if (space.imageUrl.isNotBlank()) {
                                        AppImage(
                                            source = space.imageUrl,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                        )
                                        VerticalGap(10)
                                    }
                                    Text(space.name, fontWeight = FontWeight.SemiBold)
                                    if (space.description.isNotBlank()) {
                                        Text(
                                            text = space.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    if (space.capacity != null) {
                                        Text(
                                            text = string("services.capacity")
                                                .replace("{n}", "${space.capacity}"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    VerticalGap(8)
                                    AppOutlinedButton(
                                        text = string("services.reserve"),
                                        onClick = { viewModel.selectSpace(space) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        VerticalGap(24)
    }

    state.selectedSpace?.let { space ->
        AppDialog(
            title = string("services.reserveTitle").replace("{name}", space.name),
            onDismiss = viewModel::closeReserve,
            onConfirm = viewModel::submit,
            confirmText = string("services.confirmReserve"),
            busy = state.submitting,
        ) {
            DateField(
                value = state.date,
                onValueChange = { viewModel.onFieldChange("date", it) },
                label = string("services.fieldDate"),
            )
            VerticalGap(12)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField(
                    value = state.startTime,
                    onValueChange = { viewModel.onFieldChange("startTime", it) },
                    label = string("services.fieldStart"),
                    modifier = Modifier.weight(1f),
                )
                TimeField(
                    value = state.endTime,
                    onValueChange = { viewModel.onFieldChange("endTime", it) },
                    label = string("services.fieldEnd"),
                    modifier = Modifier.weight(1f),
                )
            }
            state.formError?.let {
                VerticalGap(8)
                FormError(it)
            }
            if (state.spaceReservations.isNotEmpty()) {
                VerticalGap(12)
                Text(
                    text = string("services.reservedSlots"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.spaceReservations
                    .sortedBy { it.date }
                    .forEach { reservation ->
                        Text(
                            text = "${Dates.displayDate(reservation.date)} · " +
                                "${reservation.startTime} – ${reservation.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
        }
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("services.title"),
            message = message,
            confirmText = string("services.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}
