package com.classitda.core.platform

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient =
                    object : WebViewClient() {
                        override fun onPageFinished(
                            view: WebView?,
                            url: String?,
                        ) {
                            canGoBack = view?.canGoBack() == true
                        }
                    }
                loadUrl(url)
                webView = this
            }
        },
        update = { view ->
            canGoBack = view.canGoBack()
            if (view.url != url) view.loadUrl(url)
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }
}
