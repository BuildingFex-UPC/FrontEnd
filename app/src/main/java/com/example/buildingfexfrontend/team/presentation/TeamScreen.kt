package com.example.buildingfexfrontend.team.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.buildingfexfrontend.core.ui.components.AppImage
import com.example.buildingfexfrontend.core.ui.components.AppTextField
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.PhotoPickerField
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.team.domain.model.TeamWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(container: AppContainer) {
    val viewModel: TeamViewModel = appViewModel { TeamViewModel(container.team) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openEditor) {
                Icon(Icons.Outlined.Add, contentDescription = string("team.fab.add"))
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
                text = string("team.title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = string("team.subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(12)
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(string("team.search.placeholder")) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(Icons.Outlined.Close, contentDescription = string("team.search.clear"))
                        }
                    }
                },
                singleLine = true,
            )
            VerticalGap(12)

            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.workers.isEmpty() -> EmptyState(string("team.empty.none"))
                state.filteredWorkers.isEmpty() -> EmptyState(
                    string("team.empty.noMatch").replace("{query}", state.query.trim()),
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(state.filteredWorkers, key = { it.id.ifBlank { it.name } }) { worker ->
                        WorkerCard(
                            worker = worker,
                            onEdit = { viewModel.openEditorFor(worker) },
                            onDelete = { viewModel.requestDelete(worker) },
                        )
                    }
                }
            }
        }
    }

    if (state.editorVisible) {
        AlertDialog(
            onDismissRequest = viewModel::closeEditor,
            title = {
                Text(
                    if (state.editingOriginal == null) string("team.editor.titleNew")
                    else string("team.editor.titleEdit"),
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AppTextField(
                        value = state.name,
                        onValueChange = { viewModel.onFieldChange("name", it) },
                        label = string("team.field.name"),
                    )
                    VerticalGap(12)
                    AppTextField(
                        value = state.phone,
                        onValueChange = { viewModel.onFieldChange("phone", it) },
                        label = string("team.field.phone"),
                    )
                    VerticalGap(12)
                    AppTextField(
                        value = state.dni,
                        onValueChange = { viewModel.onFieldChange("dni", it) },
                        label = string("team.field.dni"),
                    )
                    VerticalGap(12)
                    AppTextField(
                        value = state.salary,
                        onValueChange = { viewModel.onFieldChange("salary", it) },
                        label = string("team.field.salary"),
                    )
                    VerticalGap(12)

                    PhotoPickerField(
                        dataUrl = state.photoUrl,
                        onPhotoChange = viewModel::setPhoto,
                        onClear = viewModel::clearPhoto,
                        onError = { viewModel.photoProcessingFailed() },
                        onProcessingChange = viewModel::onPhotoProcessing,
                        enabled = !state.saving,
                        label = string("team.field.photo"),
                        placeholderIcon = Icons.Outlined.Badge,
                    )

                    state.formError?.let {
                        VerticalGap(8)
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::save,
                    enabled = !state.saving && !state.processingPhoto,
                ) {
                    Text(if (state.saving) string("team.editor.saving") else string("team.editor.save"))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeEditor) { Text(string("team.editor.cancel")) }
            },
        )
    }

    state.deleteTarget?.let { target ->
        ConfirmDialog(
            title = string("team.delete.title"),
            message = string("team.delete.message").replace("{name}", target.name),
            confirmText = string("team.delete.confirm"),
            destructive = true,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("team.message.title"),
            message = message,
            confirmText = string("team.message.confirm"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun WorkerCard(
    worker: TeamWorker,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppImage(
                source = worker.photoUrl,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                placeholderIcon = Icons.Outlined.Badge,
                placeholderSize = 20.dp,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(worker.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = string("team.card.dniPhone")
                        .replace("{dni}", worker.dni)
                        .replace("{phone}", worker.phone),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = string("team.card.salary")
                        .replace("{salary}", Dates.currency(worker.salary)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = string("team.card.editCd").replace("{name}", worker.name),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = string("team.card.deleteCd").replace("{name}", worker.name),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
