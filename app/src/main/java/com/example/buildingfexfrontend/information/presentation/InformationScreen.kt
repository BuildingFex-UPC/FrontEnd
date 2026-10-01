package com.example.buildingfexfrontend.information.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.SectionCard
import com.example.buildingfexfrontend.core.ui.components.StatusChip
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.information.domain.model.Announcement
import com.example.buildingfexfrontend.ui.theme.BfError
import com.example.buildingfexfrontend.ui.theme.BfErrorContainer
import com.example.buildingfexfrontend.ui.theme.BfInfo
import com.example.buildingfexfrontend.ui.theme.BfInfoContainer

@Composable
fun AnnouncementCard(announcement: Announcement, onDelete: (() -> Unit)? = null) {
    val isHigh = announcement.priority == "high"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(
                    text = if (isHigh) string("info.chip.high") else string("info.chip.normal"),
                    container = if (isHigh) BfErrorContainer else BfInfoContainer,
                    content = if (isHigh) BfError else BfInfo,
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.weight(1f),
                )
                if (announcement.authorName.isNotBlank()) {
                    Text(
                        text = announcement.authorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = string("info.delete.cd"),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            VerticalGap(8)
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            VerticalGap(4)
            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(8)
            Text(
                text = string("info.card.dates")
                    .replace("{published}", Dates.displayDate(announcement.createdAt))
                    .replace("{expires}", Dates.displayDate(announcement.expiresAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Reused by the resident dashboard. */
@Composable
fun AnnouncementList(items: List<Announcement>) {
    if (items.isEmpty()) {
        EmptyState(string("info.list.empty"))
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { announcement -> AnnouncementCard(announcement) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationScreen(container: AppContainer) {
    val viewModel: InformationViewModel = appViewModel { InformationViewModel(container.announcements) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openEditor) {
                Icon(Icons.Filled.Add, contentDescription = string("info.fab.add"))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            VerticalGap(12)
            Text(
                text = string("info.title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = string("info.subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(12)
            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.announcements.isEmpty() -> EmptyState(string("info.empty"))
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(state.announcements, key = { it.id }) { announcement ->
                        AnnouncementCard(announcement) { viewModel.openDelete(announcement) }
                    }
                }
            }
        }
    }

    if (state.editorVisible) {
        AlertDialog(
            onDismissRequest = viewModel::closeEditor,
            title = { Text(string("info.editor.title")) },
            text = {
                Column {
                    AppTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = string("info.field.title"),
                    )
                    VerticalGap(12)
                    AppTextField(
                        value = state.body,
                        onValueChange = viewModel::onBodyChange,
                        label = string("info.field.body"),
                        singleLine = false,
                    )
                    VerticalGap(12)
                    Text(string("info.field.priority"), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.FilterChip(
                            selected = state.priority == "normal",
                            onClick = { viewModel.onPriorityChange("normal") },
                            label = { Text(string("info.priority.normal")) },
                        )
                        androidx.compose.material3.FilterChip(
                            selected = state.priority == "high",
                            onClick = { viewModel.onPriorityChange("high") },
                            label = { Text(string("info.priority.high")) },
                        )
                    }
                    VerticalGap(12)
                    Text(string("info.field.duration"), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 15, 30).forEach { days ->
                            androidx.compose.material3.FilterChip(
                                selected = state.duration == days,
                                onClick = { viewModel.onDurationChange(days) },
                                label = { Text("$days") },
                            )
                        }
                    }
                    state.formError?.let {
                        VerticalGap(8)
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::publish, enabled = !state.saving) {
                    Text(if (state.saving) string("info.editor.saving") else string("info.editor.save"))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeEditor) { Text(string("info.editor.cancel")) }
            },
        )
    }

    state.deleteTarget?.let {
        ConfirmDialog(
            title = string("info.delete.title"),
            message = string("info.delete.message"),
            confirmText = string("info.delete.confirm"),
            destructive = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::closeDelete,
        )
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("info.message.title"),
            message = message,
            confirmText = string("info.message.confirm"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}
