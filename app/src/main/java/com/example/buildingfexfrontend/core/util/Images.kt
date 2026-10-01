package com.example.buildingfexfrontend.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Technical image helpers: scale + JPEG-compress a picked/captured photo into
 * a `data:image/jpeg;base64,...` URL (same shape the web stores in `photoUrl`),
 * plus decode helpers for `data:` and legacy `http(s)` sources.
 */
object Images {

    private const val MARKER = "base64,"
    private const val CONNECT_TIMEOUT_MS = 15_000
    private const val READ_TIMEOUT_MS = 20_000
    private const val MAX_REMOTE_BYTES = 4 * 1024 * 1024
    private const val MAX_CACHE_ENTRIES = 16

    /** Small LRU cache of downloaded bytes so scrolling never re-fetches. */
    private val remoteCache = object : LinkedHashMap<String, ByteArray>(12, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean =
            size > MAX_CACHE_ENTRIES
    }

    fun toDataUrl(
        context: Context,
        uri: Uri,
        maxSize: Int = 1024,
        quality: Int = 72,
    ): String? = try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val bitmap = decodeScaled(bytes, maxSize) ?: return null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }

    fun decodeDataUrl(dataUrl: String?): ByteArray? {
        if (dataUrl.isNullOrBlank()) return null
        val index = dataUrl.indexOf(MARKER)
        val payload = if (index >= 0) dataUrl.substring(index + MARKER.length) else dataUrl
        return try {
            Base64.decode(payload, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    fun bitmap(dataUrl: String?): Bitmap? {
        val bytes = decodeDataUrl(dataUrl) ?: return null
        return try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /** Decodes any stored source (`data:` URL or `http(s)` URL) into a display bitmap. */
    fun bitmapFromSource(source: String?, maxSize: Int = 1024): Bitmap? {
        val src = source?.trim().orEmpty()
        if (src.isEmpty()) return null
        return when {
            src.startsWith("data:", ignoreCase = true) -> bitmap(src)
            src.startsWith("http://", ignoreCase = true) || src.startsWith("https://", ignoreCase = true) -> {
                val bytes = loadRemote(src) ?: return null
                try {
                    decodeScaled(bytes, maxSize)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }

    /** Downloads an image with a size cap and a small in-memory LRU cache. */
    fun loadRemote(url: String): ByteArray? {
        synchronized(remoteCache) { remoteCache[url] }?.let { return it }
        val bytes = try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.connect()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            connection.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_REMOTE_BYTES) return null
                    out.write(buffer, 0, read)
                }
                out.toByteArray()
            }
        } catch (e: Exception) {
            return null
        }
        synchronized(remoteCache) { remoteCache[url] = bytes }
        return bytes
    }

    private fun decodeScaled(bytes: ByteArray, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxSize || bounds.outHeight / sample > maxSize) {
            sample *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sample },
        ) ?: return null
        val scale = maxSize.toFloat() / maxOf(decoded.width, decoded.height)
        if (scale >= 1f) return decoded
        val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
        val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
    }
}
