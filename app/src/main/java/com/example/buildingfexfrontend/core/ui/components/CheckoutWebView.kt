package com.example.buildingfexfrontend.core.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.buildingfexfrontend.core.i18n.string

/**
 * Mercado Pago Checkout Pro in an in-app WebView (full-screen dialog).
 *
 * When Mercado Pago redirects back to the configured frontend URL (back_urls),
 * the redirect is intercepted before any page loads and its query parameters
 * (`payment_id` / `collection_id`, `status`, ...) are delivered to [onRedirect],
 * so the ViewModel can confirm the payment without leaving the app.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CheckoutWebView(
    url: String,
    onRedirect: (Uri) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentOnRedirect by rememberUpdatedState(onRedirect)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    loading.value = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    loading.value = false
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme?.lowercase()
                    if (scheme != "http" && scheme != "https") {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        return true
                    }
                    val path = uri.path.orEmpty()
                    if (path.contains("/app/resident/finance") || path.contains("/app/settings")) {
                        currentOnRedirect(uri)
                        return true
                    }
                    return false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = currentOnDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = string("ui.checkout.title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                    IconButton(onClick = currentOnDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = string("ui.close"),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    AndroidView(
                        factory = { webView },
                        update = { if (it.url == null) it.loadUrl(url) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (loading.value) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
