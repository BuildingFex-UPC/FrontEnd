package com.example.buildingfexfrontend.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.buildingfexfrontend.core.util.Images
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a stored image source: `data:image/...;base64` (uploaded photo) or a
 * legacy `http(s)` URL (downloaded here, Coil is not in the project). Shows a
 * placeholder while loading or when the source is empty/broken.
 */
@Composable
fun AppImage(
    source: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector = Icons.Outlined.BrokenImage,
    placeholderSize: Dp = 24.dp,
) {
    val src = source.orEmpty().trim()
    val bitmap by produceState<ImageBitmap?>(initialValue = null, src) {
        value = if (src.isEmpty()) {
            null
        } else {
            withContext(Dispatchers.Default) { Images.bitmapFromSource(src)?.asImageBitmap() }
        }
    }
    val image = bitmap
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        } else {
            Icon(
                imageVector = placeholderIcon,
                contentDescription = null,
                modifier = Modifier.size(placeholderSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
