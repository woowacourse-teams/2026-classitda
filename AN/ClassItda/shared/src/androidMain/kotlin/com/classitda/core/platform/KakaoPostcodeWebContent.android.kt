package com.classitda.core.platform

import android.net.Uri
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

private const val KAKAO_POSTCODE_LOG_TAG = "ClassitdaKakaoPostcode"

@Composable
internal actual fun KakaoPostcodeWebContent(
    onLoadingChanged: (Boolean) -> Unit,
    onResult: (KakaoPostcodeResult) -> Unit,
    onCancelled: () -> Unit,
    onError: (KakaoPostcodeError) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnCancelled by rememberUpdatedState(onCancelled)
    val currentOnError by rememberUpdatedState(onError)
    val webViewState = remember { AndroidKakaoPostcodeWebViewState() }
    val webViewClient =
        remember {
            KakaoPostcodeWebViewClient(
                onLoadingChanged = { currentOnLoadingChanged(it) },
                onNavigationBlocked = { currentOnError(KakaoPostcodeError.NAVIGATION_BLOCKED) },
                onNetworkError = { currentOnError(KakaoPostcodeError.NETWORK) },
            )
        }

    BackHandler(onBack = currentOnCancelled)

    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).also { webView ->
                val bridge =
                    KakaoPostcodeJavascriptBridge(
                        onMessage = { payload ->
                            Log.d(KAKAO_POSTCODE_LOG_TAG, "bridge message received: length=${payload.length}")
                            webView.post {
                                when (val message = parseKakaoPostcodeBridgeMessage(payload).getOrNull()) {
                                    is KakaoPostcodeBridgeMessage.Result -> {
                                        Log.d(
                                            KAKAO_POSTCODE_LOG_TAG,
                                            "address selected: " +
                                                "zoneCode=${message.value.zoneCode}, " +
                                                "roadAddress=${message.value.roadAddress}, " +
                                                "jibunAddress=${message.value.jibunAddress}, " +
                                                "buildingName=${message.value.buildingName}, " +
                                                "detailAddress=<app input>",
                                        )
                                        currentOnResult(message.value)
                                    }

                                    is KakaoPostcodeBridgeMessage.Error -> {
                                        Log.w(KAKAO_POSTCODE_LOG_TAG, "bridge error: ${message.reason}")
                                        currentOnError(message.reason)
                                    }

                                    null -> {
                                        Log.e(KAKAO_POSTCODE_LOG_TAG, "bridge payload parse failed")
                                        currentOnError(KakaoPostcodeError.INVALID_PAYLOAD)
                                    }
                                }
                            }
                        },
                        onError = { reason ->
                            Log.e(KAKAO_POSTCODE_LOG_TAG, "wrapper error: $reason")
                            webView.post {
                                currentOnError(reason.toKakaoPostcodeError())
                            }
                        },
                    )
                webViewState.webView = webView
                webViewState.bridge = bridge
                configureKakaoPostcodeWebView(webView)
                webView.webViewClient = webViewClient
                webView.addJavascriptInterface(bridge, KAKAO_POSTCODE_BRIDGE_NAME)
                Log.d(KAKAO_POSTCODE_LOG_TAG, "loading local wrapper with Kakao base URL")
                webView.loadDataWithBaseURL(
                    KAKAO_POSTCODE_BASE_URL,
                    kakaoPostcodeWrapperHtml(),
                    "text/html",
                    "UTF-8",
                    null,
                )
            }
        },
        update = { webView ->
            webViewState.webView = webView
        },
    )

    DisposableEffect(webViewState) {
        onDispose {
            webViewState.webView?.let { webView ->
                webView.stopLoading()
                webView.removeJavascriptInterface(KAKAO_POSTCODE_BRIDGE_NAME)
                webView.webViewClient = WebViewClient()
                webView.webChromeClient = null
                webView.destroy()
            }
            webViewState.bridge = null
            webViewState.webView = null
        }
    }
}

private class AndroidKakaoPostcodeWebViewState {
    var webView: WebView? = null
    var bridge: KakaoPostcodeJavascriptBridge? = null
}

private class KakaoPostcodeJavascriptBridge(
    private val onMessage: (String) -> Unit,
    private val onError: (String) -> Unit,
) {
    @JavascriptInterface
    fun postMessage(payload: String) {
        onMessage(payload)
    }

    @JavascriptInterface
    fun postError(reason: String) {
        onError(reason)
    }
}

private class KakaoPostcodeWebViewClient(
    private val onLoadingChanged: (Boolean) -> Unit,
    private val onNavigationBlocked: () -> Unit,
    private val onNetworkError: () -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        if (!request.isAllowedKakaoPostcodeNavigation()) {
            Log.w(
                KAKAO_POSTCODE_LOG_TAG,
                "navigation blocked: ${request.url.safeLogLocation()}, mainFrame=${request.isForMainFrame}",
            )
            onNavigationBlocked()
            return true
        }
        Log.d(
            KAKAO_POSTCODE_LOG_TAG,
            "navigation allowed: ${request.url.safeLogLocation()}, mainFrame=${request.isForMainFrame}",
        )
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        if (
            request.isForMainFrame &&
            !request.url.isAllowedKakaoPostcodeUrl() &&
            !request.url.isKakaoPostcodeWrapperDataUrl()
        ) {
            Log.w(
                KAKAO_POSTCODE_LOG_TAG,
                "resource navigation blocked: ${request.url.safeLogLocation()}",
            )
            view.post(onNavigationBlocked)
            return blockedResponse()
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: android.graphics.Bitmap?,
    ) {
        if (url?.isAllowedKakaoPostcodeUrl() == true) {
            Log.d(KAKAO_POSTCODE_LOG_TAG, "page started: ${url.safeLogLocation()}")
            onLoadingChanged(true)
        } else {
            Log.w(KAKAO_POSTCODE_LOG_TAG, "page start blocked: ${url.orEmpty()}")
            view.stopLoading()
            onNavigationBlocked()
        }
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        if (url?.isAllowedKakaoPostcodeUrl() == true) {
            Log.d(KAKAO_POSTCODE_LOG_TAG, "page finished: ${url.safeLogLocation()}")
            onLoadingChanged(false)
        } else {
            Log.w(KAKAO_POSTCODE_LOG_TAG, "page finish blocked: ${url.orEmpty()}")
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
            Log.e(
                KAKAO_POSTCODE_LOG_TAG,
                "main frame network error: code=${error.errorCode}, description=${error.description}, url=${request.url.safeLogLocation()}",
            )
            onNetworkError()
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse,
    ) {
        if (request.isForMainFrame && errorResponse.statusCode >= 400) {
            Log.e(
                KAKAO_POSTCODE_LOG_TAG,
                "main frame HTTP error: status=${errorResponse.statusCode}, url=${request.url.safeLogLocation()}",
            )
            onNetworkError()
        }
    }

    override fun onReceivedSslError(
        view: WebView,
        handler: SslErrorHandler,
        error: android.net.http.SslError,
    ) {
        Log.e(KAKAO_POSTCODE_LOG_TAG, "SSL error: $error")
        handler.cancel()
        onNetworkError()
    }

    private fun blockedResponse() =
        WebResourceResponse(
            "text/plain",
            "UTF-8",
            403,
            "Forbidden",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0)),
        )
}

private fun WebResourceRequest.isAllowedKakaoPostcodeNavigation(): Boolean =
    url.isAllowedKakaoPostcodeUrl() ||
        (!isForMainFrame && url.toString() == "about:blank")

private fun configureKakaoPostcodeWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        allowFileAccess = false
        allowContentAccess = false
        mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        setSupportMultipleWindows(false)
    }
}

private fun String.isAllowedKakaoPostcodeUrl(): Boolean =
    runCatching { Uri.parse(this).isAllowedKakaoPostcodeUrl() }.getOrDefault(false)

private fun Uri.isAllowedKakaoPostcodeUrl(): Boolean =
    scheme.equals("https", ignoreCase = true) &&
        host in KAKAO_POSTCODE_ALLOWED_HOSTS &&
        (port == -1 || port == 443) &&
        userInfo == null

private fun Uri.isKakaoPostcodeWrapperDataUrl(): Boolean =
    scheme.equals("data", ignoreCase = true) && host.isNullOrBlank()

private fun Uri.safeLogLocation(): String =
    buildString {
        append(scheme.orEmpty())
        append("://")
        append(host.orEmpty())
        append(path.orEmpty())
    }

private fun String.safeLogLocation(): String =
    runCatching { Uri.parse(this).safeLogLocation() }.getOrDefault(this.substringBefore('?'))

private fun String.toKakaoPostcodeError(): KakaoPostcodeError =
    when (this) {
        "NETWORK" -> KakaoPostcodeError.NETWORK
        else -> KakaoPostcodeError.UNKNOWN
    }
