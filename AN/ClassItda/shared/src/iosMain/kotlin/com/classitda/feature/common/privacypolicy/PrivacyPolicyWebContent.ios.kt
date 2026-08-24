package com.classitda.feature.common.privacypolicy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorDomain
import platform.Foundation.NSURLErrorSecureConnectionFailed
import platform.Foundation.NSURLErrorServerCertificateHasBadDate
import platform.Foundation.NSURLErrorServerCertificateHasUnknownRoot
import platform.Foundation.NSURLErrorServerCertificateNotYetValid
import platform.Foundation.NSURLErrorServerCertificateUntrusted
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationResponse
import platform.WebKit.WKNavigationResponsePolicy
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebpagePreferences
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun PrivacyPolicyWebContent(
    reloadToken: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onLoadFailed: (PrivacyPolicyError) -> Unit,
    onNavigationBlocked: () -> Unit,
    modifier: Modifier,
) {
    val initialUrl = remember { trustedInitialUrl() }
    if (initialUrl == null) {
        LaunchedEffect(Unit) {
            onLoadFailed(PrivacyPolicyError.INVALID_INITIAL_URL)
        }
        return
    }

    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnLoadFailed by rememberUpdatedState(onLoadFailed)
    val currentOnNavigationBlocked by rememberUpdatedState(onNavigationBlocked)
    val webViewState = remember { IosPrivacyPolicyWebViewState() }
    val navigationDelegate =
        remember {
            PrivacyPolicyNavigationDelegate(
                onLoadingChanged = { currentOnLoadingChanged(it) },
                onLoadFailed = { currentOnLoadFailed(it) },
                onNavigationBlocked = { currentOnNavigationBlocked() },
            )
        }
    val configuration =
        remember {
            WKWebViewConfiguration().apply {
                websiteDataStore = WKWebsiteDataStore.nonPersistentDataStore()
                defaultWebpagePreferences =
                    WKWebpagePreferences().apply {
                        allowsContentJavaScript = false
                    }
            }
        }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = configuration,
            ).apply {
                webViewState.webView = this
                webViewState.lastReloadToken = reloadToken
                this.navigationDelegate = navigationDelegate
                loadRequest(NSURLRequest(uRL = initialUrl))
            }
        },
        update = { webView ->
            if (webViewState.lastReloadToken != reloadToken) {
                webViewState.lastReloadToken = reloadToken
                webView.loadRequest(NSURLRequest(uRL = initialUrl))
            }
        },
    )

    DisposableEffect(navigationDelegate, webViewState) {
        onDispose {
            webViewState.webView?.let { webView ->
                webView.stopLoading()
                webView.navigationDelegate = null
            }
            webViewState.webView = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosPrivacyPolicyWebViewState {
    var webView: WKWebView? = null
    var lastReloadToken: Int? = null
}

@OptIn(ExperimentalForeignApi::class)
private class PrivacyPolicyNavigationDelegate(
    private val onLoadingChanged: (Boolean) -> Unit,
    private val onLoadFailed: (PrivacyPolicyError) -> Unit,
    private val onNavigationBlocked: () -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val targetFrame = decidePolicyForNavigationAction.targetFrame
        val url = decidePolicyForNavigationAction.request.URL
        val isAllowed =
            targetFrame != null &&
                url?.isAllowedPrivacyPolicyUrl() == true

        if (isAllowed) {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        } else {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            onNavigationBlocked()
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationResponse: WKNavigationResponse,
        decisionHandler: (WKNavigationResponsePolicy) -> Unit,
    ) {
        val url = decidePolicyForNavigationResponse.response.URL
        val isAllowed =
            decidePolicyForNavigationResponse.canShowMIMEType &&
                url?.isAllowedPrivacyPolicyUrl() == true

        if (isAllowed) {
            decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyAllow)
        } else {
            decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyCancel)
            onNavigationBlocked()
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        onLoadingChanged(true)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        if (webView.URL?.isAllowedPrivacyPolicyUrl() == true) {
            onLoadingChanged(false)
        } else {
            webView.stopLoading()
            onNavigationBlocked()
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadFailed(withError.toPrivacyPolicyError())
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadFailed(withError.toPrivacyPolicyError())
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun trustedInitialUrl(): NSURL? =
    NSURL(string = PrivacyPolicyConfig.INITIAL_URL)
        .takeIf { it.isAllowedPrivacyPolicyUrl() }

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.isAllowedPrivacyPolicyUrl(): Boolean {
    val target =
        PrivacyPolicyNavigationTarget(
            scheme = scheme,
            host = host,
            port = port?.intValue,
            hasUserInfo = user != null || password != null,
            path = path.orEmpty(),
            query = query,
        )
    return PrivacyPolicyConfig.navigationPolicy.allows(target)
}

private fun NSError.toPrivacyPolicyError(): PrivacyPolicyError =
    if (domain == NSURLErrorDomain && code in tlsErrorCodes) {
        PrivacyPolicyError.TLS
    } else {
        PrivacyPolicyError.NETWORK
    }

private val tlsErrorCodes =
    setOf(
        NSURLErrorSecureConnectionFailed,
        NSURLErrorServerCertificateUntrusted,
        NSURLErrorServerCertificateHasBadDate,
        NSURLErrorServerCertificateHasUnknownRoot,
        NSURLErrorServerCertificateNotYetValid,
    )
