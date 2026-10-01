package com.example.buildingfexfrontend.socialspaces.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppDialog
import com.example.buildingfexfrontend.core.ui.components.AppOutlinedButton
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FormError
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.OutlinedCardBox
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.core.util.Qr
import com.example.buildingfexfrontend.socialspaces.domain.model.Reservation
import com.example.buildingfexfrontend.socialspaces.domain.model.ReservationRules
import com.example.buildingfexfrontend.ui.theme.BfSuccess
import com.example.buildingfexfrontend.ui.theme.BfSuccessContainer
import com.example.buildingfexfrontend.ui.theme.BfWarning
import com.example.buildingfexfrontend.ui.theme.BfWarningContainer
import java.io.File

/** Resident "Mi generación": reservations, guests and invite link. */
@Composable
fun MyReservationsScreen(container: AppContainer) {
    val viewModel: MyReservationsViewModel = appViewModel {
        MyReservationsViewModel(container.reservations, container.spaces)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            viewModel.refreshNow()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(
            text = string("resv.title"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = string("resv.subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VerticalGap(16)

        when {
            state.loading -> FullScreenLoading()
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> {
                if (state.reservations.isEmpty()) {
                    EmptyState(string("resv.empty"))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.reservations.forEach { reservation ->
                            val expanded = state.expandedId == reservation.id
                            OutlinedCardBox {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = state.spacesById[reservation.spaceId]?.name
                                                    ?: string("resv.spaceFallback"),
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                text = "${Dates.displayDate(reservation.date)} · " +
                                                    "${reservation.startTime} – ${reservation.endTime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        TextButton(
                                            onClick = { reservation.id?.let(viewModel::toggleExpand) },
                                        ) {
                                            Text(if (expanded) string("resv.hide") else string("resv.guests"))
                                            Icon(
                                                imageVector = if (expanded) {
                                                    Icons.Filled.KeyboardArrowUp
                                                } else {
                                                    Icons.Filled.KeyboardArrowDown
                                                },
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                    VerticalGap(4)
                                    AppOutlinedButton(
                                        text = string("resv.editGuests"),
                                        onClick = { viewModel.openGuests(reservation) },
                                    )
                                    AnimatedVisibility(visible = expanded) {
                                        Column {
                                            VerticalGap(8)
                                            if (reservation.guests.isEmpty()) {
                                                Text(
                                                    text = string("resv.noGuests"),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    reservation.guests.forEach { guest ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Text(
                                                                text = guest.name,
                                                                modifier = Modifier.weight(1f),
                                                                style = MaterialTheme.typography.bodyMedium,
                                                            )
                                                            StatusChip(
                                                                text = if (guest.checkedIn) string("resv.checkedIn") else string("resv.notCheckedIn"),
                                                                container = if (guest.checkedIn) {
                                                                    BfSuccessContainer
                                                                } else {
                                                                    BfWarningContainer
                                                                },
                                                                content = if (guest.checkedIn) BfSuccess else BfWarning,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            val token = reservation.guestInviteToken
                                            if (!token.isNullOrBlank() && reservation.guests.isNotEmpty()) {
                                                VerticalGap(10)
                                                Text(
                                                    text = string("resv.inviteTitle"),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                                val expired = viewModel.inviteExpired(reservation)
                                                if (expired) {
                                                    Text(
                                                        text = string("resv.inviteExpired"),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error,
                                                    )
                                                } else {
                                                    val inviteUrl = remember(token) { viewModel.shareUrl(reservation) }
                                                    val qrBitmap = remember(inviteUrl) { Qr.bitmap(inviteUrl) }
                                                    if (qrBitmap != null) {
                                                        VerticalGap(8)
                                                        Image(
                                                            bitmap = qrBitmap.asImageBitmap(),
                                                            contentDescription = string("resv.inviteQrDesc"),
                                                            modifier = Modifier
                                                                .align(Alignment.CenterHorizontally)
                                                                .size(200.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.surface),
                                                        )
                                                    }
                                                    VerticalGap(10)
                                                    AppOutlinedButton(
                                                        text = string("resv.shareQr"),
                                                        onClick = {
                                                            if (qrBitmap != null) {
                                                                shareQrImage(
                                                                    context,
                                                                    qrBitmap,
                                                                    viewModel.shareText(reservation),
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
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
        VerticalGap(24)
    }

    if (state.guestsDialogId != null) {
        AppDialog(
            title = string("resv.guests"),
            onDismiss = viewModel::closeGuests,
            onConfirm = viewModel::saveGuests,
            confirmText = string("resv.save"),
            busy = state.savingGuests,
        ) {
            Text(
                text = string("resv.guestsHint")
                    .replace("{n}", ReservationRules.MAX_GUESTS.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(12)
            state.guestsDraft.forEachIndexed { index, guest ->
                AppTextField(
                    value = guest.name,
                    onValueChange = { viewModel.onGuestChange(index, it) },
                    label = string("resv.guestLabel")
                        .replace("{n}", (index + 1).toString()),
                )
                VerticalGap(8)
            }
            state.guestsError?.let { FormError(it) }
        }
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("resv.title"),
            message = message,
            confirmText = string("resv.understood"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

/** Writes the QR to the share cache and opens the system sheet with the image. */
private fun shareQrImage(context: Context, bitmap: Bitmap, text: String) {
    runCatching {
        val dir = File(context.cacheDir, "invite").apply { mkdirs() }
        val file = File(dir, "buildingfex-invite-qr.png")
        file.outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, stringOf("resv.shareChooser")))
    }
}
