package com.example.buildingfexfrontend.core.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.i18n.stringOf
import com.example.buildingfexfrontend.core.util.Images
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.content.FileProvider

/**
 * Shared photo picker (gallery + camera) that compresses the picked image into
 * a `data:image/jpeg;base64` URL. Used by Team and Social Spaces forms.
 */
@Composable
fun PhotoPickerField(
    dataUrl: String,
    onPhotoChange: (String) -> Unit,
    onClear: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
    onProcessingChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
    label: String = string("ui.photoLabel"),
    previewSize: Dp = 56.dp,
    placeholderIcon: ImageVector = Icons.Outlined.Badge,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    var working by remember { mutableStateOf(false) }

    fun handlePhoto(uri: Uri) {
        if (working) return
        working = true
        onProcessingChange(true)
        scope.launch {
            val result = withContext(Dispatchers.IO) { Images.toDataUrl(context, uri) }
            working = false
            onProcessingChange(false)
            if (result == null) onError(stringOf("ui.photoError"))
            else onPhotoChange(result)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (working) {
                working = false
                onProcessingChange(false)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) handlePhoto(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { ok ->
        val uri = cameraUri.value
        cameraUri.value = null
        if (ok && uri != null) handlePhoto(uri)
    }

    val active = enabled && !working

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppImage(
                source = dataUrl,
                modifier = Modifier
                    .size(previewSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                placeholderIcon = placeholderIcon,
                placeholderSize = (previewSize / 2.5f),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = active,
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(string("ui.gallery"), style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        enabled = active,
                        onClick = {
                            val uri = newCameraUri(context, prefix = "photo")
                            if (uri != null) {
                                cameraUri.value = uri
                                cameraLauncher.launch(uri)
                            }
                        },
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(string("ui.camera"), style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (dataUrl.isNotBlank()) {
                    TextButton(enabled = active, onClick = onClear) {
                        Text(string("ui.removePhoto"), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        if (working) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    text = string("ui.processingPhoto"),
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

/** Scratch file exposed to the system camera app through the FileProvider. */
private fun newCameraUri(context: android.content.Context, prefix: String): Uri? = runCatching {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "$prefix-${System.currentTimeMillis()}.jpg")
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()
