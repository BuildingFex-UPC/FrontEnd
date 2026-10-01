package com.example.buildingfexfrontend.imports.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buildingfexfrontend.core.di.AppContainer
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.ui.appViewModel
import com.example.buildingfexfrontend.core.ui.components.AppOutlinedButton
import com.example.buildingfexfrontend.core.ui.components.ConfirmDialog
import com.example.buildingfexfrontend.core.ui.components.EmptyState
import com.example.buildingfexfrontend.core.ui.components.ErrorState
import com.example.buildingfexfrontend.core.ui.components.FullScreenLoading
import com.example.buildingfexfrontend.core.ui.components.VerticalGap
import com.example.buildingfexfrontend.core.util.Dates
import com.example.buildingfexfrontend.imports.domain.model.ImportUpload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportsScreen(container: AppContainer) {
    val viewModel: ImportsViewModel = appViewModel { ImportsViewModel(container.imports) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingDownload by remember { mutableStateOf<ImportUpload?>(null) }

    val openDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            } ?: "archivo"
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: ByteArray(0)
            viewModel.upload(name, mime, bytes)
        }.onFailure {
            viewModel.upload("archivo", "application/octet-stream", ByteArray(0))
        }
    }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*"),
    ) { uri ->
        val target = pendingDownload
        pendingDownload = null
        if (uri == null || target == null) return@rememberLauncherForActivityResult
        runCatching {
            val bytes = viewModel.decode(target.dataUrl) ?: ByteArray(0)
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            VerticalGap(12)
            Text(
                text = string("imports.title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = string("imports.subtitle"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalGap(12)
            AppOutlinedButton(
                text = if (state.uploading) string("imports.upload.uploading") else string("imports.upload.select"),
                enabled = !state.uploading,
                onClick = { openDocument.launch(arrayOf("*/*")) },
            )
            VerticalGap(12)
            when {
                state.loading -> FullScreenLoading()
                state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
                state.uploads.isEmpty() -> EmptyState(string("imports.empty"))
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(state.uploads, key = { it.id }) { upload ->
                        UploadRow(
                            upload = upload,
                            onDelete = { viewModel.remove(upload.id) },
                            onSaveCopy = {
                                pendingDownload = upload
                                createDocument.launch(upload.fileName)
                            },
                        )
                    }
                }
            }
        }
    }

    state.message?.let { message ->
        ConfirmDialog(
            title = string("imports.title"),
            message = message,
            confirmText = string("imports.message.confirm"),
            onConfirm = viewModel::dismissMessage,
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
private fun UploadRow(upload: ImportUpload, onDelete: () -> Unit, onSaveCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.UploadFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(upload.fileName, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(
                    text = "${formatSize(upload.size)} · ${Dates.displayDate(upload.uploadedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onSaveCopy) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = string("imports.row.saveCopyCd"),
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text(string("imports.row.save"))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = string("imports.row.deleteCd"),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
