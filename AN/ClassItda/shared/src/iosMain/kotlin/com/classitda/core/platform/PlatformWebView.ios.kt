package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    UIKitView(
        modifier = modifier,
        factory = { WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) },
        update = { webView ->
            if (webView.URL?.absoluteString != url) {
                webView.loadRequest(NSURLRequest(uRL = NSURL(string = url)))
            }
        },
    )
}
