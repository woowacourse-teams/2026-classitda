package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    onLoadingChanged: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val currentOnLoadingChanged = androidx.compose.runtime.rememberUpdatedState(onLoadingChanged)
    val navigationDelegate =
        androidx.compose.runtime.remember {
            TermsNavigationDelegate { isLoading -> currentOnLoadingChanged.value(isLoading) }
        }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)).apply {
                this.navigationDelegate = navigationDelegate
            }
        },
        update = { webView ->
            webView.navigationDelegate = navigationDelegate
            if (webView.URL?.absoluteString != url) {
                currentOnLoadingChanged.value(true)
                webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class TermsNavigationDelegate(
    private val onLoadingChanged: (Boolean) -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
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
        onLoadingChanged(false)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadingChanged(false)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadingChanged(false)
    }
}
