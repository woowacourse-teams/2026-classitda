package com.classitda.feature.common.privacypolicy

import android.net.Uri
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

@Composable
internal actual fun PrivacyPolicyWebContent(
    reloadToken: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onLoadFailed: (PrivacyPolicyError) -> Unit,
    onNavigationBlocked: () -> Unit,
    modifier: Modifier,
) {
    val initialUri = remember { trustedInitialUri() }
    if (initialUri == null) {
        LaunchedEffect(Unit) {
            onLoadFailed(PrivacyPolicyError.INVALID_INITIAL_URL)
        }
        return
    }

    val context = LocalContext.current
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnLoadFailed by rememberUpdatedState(onLoadFailed)
    val currentOnNavigationBlocked by rememberUpdatedState(onNavigationBlocked)
    val webViewState = remember { AndroidPrivacyPolicyWebViewState() }
    val webViewClient =
        remember {
            PrivacyPolicyWebViewClient(
                onLoadingChanged = { currentOnLoadingChanged(it) },
                onLoadFailed = { currentOnLoadFailed(it) },
                onNavigationBlocked = { currentOnNavigationBlocked() },
            )
        }

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                webViewState.webView = this
                webViewState.lastReloadToken = reloadToken
                configurePrivacyPolicyWebView(this)
                this.webViewClient = webViewClient
                loadUrl(initialUri.toString())
            }
        },
        update = { webView ->
            if (webViewState.lastReloadToken != reloadToken) {
                webViewState.lastReloadToken = reloadToken
                webView.loadUrl(initialUri.toString())
            }
        },
    )

    DisposableEffect(webViewState) {
        onDispose {
            webViewState.webView?.let { webView ->
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.webChromeClient = null
                webView.destroy()
            }
            webViewState.webView = null
        }
    }
}

private class AndroidPrivacyPolicyWebViewState {
    var webView: WebView? = null
    var lastReloadToken: Int? = null
}

private class PrivacyPolicyWebViewClient(
    private val onLoadingChanged: (Boolean) -> Unit,
    private val onLoadFailed: (PrivacyPolicyError) -> Unit,
    private val onNavigationBlocked: () -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean =
        if (request.url.isAllowedPrivacyPolicyUrl()) {
            false
        } else {
            onNavigationBlocked()
            true
        }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val isPostNavigation = request.method.equals("POST", ignoreCase = true)
        val isNavigationLikeRequest = request.isForMainFrame || isPostNavigation
        if (isNavigationLikeRequest && !request.url.isAllowedPrivacyPolicyUrl()) {
            view.post { onNavigationBlocked() }
            return blockedNavigationResponse()
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: android.graphics.Bitmap?,
    ) {
        val isAllowed = url?.isAllowedPrivacyPolicyUrl() == true
        if (isAllowed) {
            onLoadingChanged(true)
        } else {
            view.stopLoading()
            onNavigationBlocked()
        }
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        if (url?.isAllowedPrivacyPolicyUrl() == true) {
            onLoadingChanged(false)
        } else {
            view.stopLoading()
            onNavigationBlocked()
        }
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError,
    ) {
        if (request.isForMainFrame) {
            onLoadFailed(
                if (error.errorCode == WebViewClient.ERROR_FAILED_SSL_HANDSHAKE) {
                    PrivacyPolicyError.TLS
                } else {
                    PrivacyPolicyError.NETWORK
                },
            )
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        if (request.isForMainFrame && errorResponse.statusCode >= 400) {
            onLoadFailed(PrivacyPolicyError.UNKNOWN)
        }
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: android.net.http.SslError,
    ) {
        handler.cancel()
        onLoadFailed(PrivacyPolicyError.TLS)
    }

    private fun blockedNavigationResponse() =
        WebResourceResponse(
            "text/plain",
            "UTF-8",
            403,
            "Forbidden",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0)),
        )
}

private fun configurePrivacyPolicyWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = false
        domStorageEnabled = false
        allowFileAccess = false
        allowContentAccess = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        setSupportMultipleWindows(true)
    }
    CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false)
}

private fun trustedInitialUri(): Uri? =
    runCatching { Uri.parse(PrivacyPolicyConfig.INITIAL_URL) }
        .getOrNull()
        ?.takeIf { it.isAllowedPrivacyPolicyUrl() }

private fun String.isAllowedPrivacyPolicyUrl(): Boolean =
    runCatching { Uri.parse(this).isAllowedPrivacyPolicyUrl() }.getOrDefault(false)

private fun Uri.isAllowedPrivacyPolicyUrl(): Boolean {
    val target =
        PrivacyPolicyNavigationTarget(
            scheme = scheme,
            host = host,
            port = port.takeUnless { it == -1 },
            hasUserInfo = userInfo != null,
            path = path.orEmpty(),
            query = query,
        )
    return PrivacyPolicyConfig.navigationPolicy.allows(target)
}
