package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKNavigationResponse
import platform.WebKit.WKNavigationResponsePolicy
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKWebpagePreferences
import platform.WebKit.WKWebsiteDataStore
import platform.darwin.NSObject

private const val KAKAO_POSTCODE_LOG_PREFIX = "[ClassitdaKakaoPostcode]"

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun KakaoPostcodeWebContent(
    onLoadingChanged: (Boolean) -> Unit,
    onResult: (KakaoPostcodeResult) -> Unit,
    onCancelled: () -> Unit,
    onError: (KakaoPostcodeError) -> Unit,
    modifier: Modifier,
) {
    val currentOnLoadingChanged by rememberUpdatedState(onLoadingChanged)
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnError by rememberUpdatedState(onError)
    val webViewState = remember { IosKakaoPostcodeWebViewState() }
    val messageHandler =
        remember {
            KakaoPostcodeScriptMessageHandler { payload ->
                NSLog("$KAKAO_POSTCODE_LOG_PREFIX bridge message received: length=${payload.length}")
                when (val message = parseKakaoPostcodeBridgeMessage(payload).getOrNull()) {
                    is KakaoPostcodeBridgeMessage.Result -> {
                        NSLog(
                            "$KAKAO_POSTCODE_LOG_PREFIX address selected: " +
                                "zoneCodePresent=${message.value.zoneCode.isNotBlank()}, " +
                                "roadAddressPresent=${message.value.roadAddress.isNotBlank()}, " +
                                "jibunAddressPresent=${message.value.jibunAddress.isNotBlank()}, " +
                                "buildingNamePresent=${message.value.buildingName.isNotBlank()}, " +
                                "detailAddress=<app input>",
                        )
                        currentOnResult(message.value)
                    }

                    is KakaoPostcodeBridgeMessage.Error -> {
                        NSLog("$KAKAO_POSTCODE_LOG_PREFIX bridge error: ${message.reason}")
                        currentOnError(message.reason)
                    }

                    null -> {
                        NSLog("$KAKAO_POSTCODE_LOG_PREFIX bridge payload parse failed")
                        currentOnError(KakaoPostcodeError.INVALID_PAYLOAD)
                    }
                }
            }
        }
    val navigationDelegate =
        remember {
            KakaoPostcodeNavigationDelegate(
                onLoadingChanged = { currentOnLoadingChanged(it) },
                onNavigationBlocked = { currentOnError(KakaoPostcodeError.NAVIGATION_BLOCKED) },
                onNetworkError = { currentOnError(KakaoPostcodeError.NETWORK) },
            )
        }
    val configuration =
        remember {
            WKWebViewConfiguration().apply {
                websiteDataStore = WKWebsiteDataStore.nonPersistentDataStore()
                defaultWebpagePreferences =
                    WKWebpagePreferences().apply {
                        allowsContentJavaScript = true
                    }
                userContentController =
                    WKUserContentController().apply {
                        addScriptMessageHandler(messageHandler, name = KAKAO_POSTCODE_BRIDGE_NAME)
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
                this.navigationDelegate = navigationDelegate
                NSLog("$KAKAO_POSTCODE_LOG_PREFIX loading local wrapper with Kakao base URL")
                loadHTMLString(
                    string = kakaoPostcodeWrapperHtml(),
                    baseURL = NSURL(string = KAKAO_POSTCODE_BASE_URL),
                )
            }
        },
        update = { webView ->
            webViewState.webView = webView
        },
    )

    DisposableEffect(configuration, messageHandler, navigationDelegate, webViewState) {
        onDispose {
            webViewState.webView?.let { webView ->
                webView.stopLoading()
                webView.navigationDelegate = null
                webView.configuration.userContentController.removeScriptMessageHandlerForName(
                    KAKAO_POSTCODE_BRIDGE_NAME,
                )
            }
            webViewState.webView = null
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosKakaoPostcodeWebViewState {
    var webView: WKWebView? = null
}

@OptIn(ExperimentalForeignApi::class)
private class KakaoPostcodeScriptMessageHandler(
    private val onMessage: (String) -> Unit,
) : NSObject(),
    WKScriptMessageHandlerProtocol {
    @ObjCSignatureOverride
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val payload = didReceiveScriptMessage.body as? String
        if (payload == null) {
            onMessage("{}")
        } else {
            onMessage(payload)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class KakaoPostcodeNavigationDelegate(
    private val onLoadingChanged: (Boolean) -> Unit,
    private val onNavigationBlocked: () -> Unit,
    private val onNetworkError: () -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val url = decidePolicyForNavigationAction.request.URL
        val targetFrame = decidePolicyForNavigationAction.targetFrame
        if (url?.isAllowedKakaoPostcodeNavigation(targetFrame?.isMainFrame()) == true) {
            NSLog(
                "$KAKAO_POSTCODE_LOG_PREFIX navigation allowed: ${url.safeLogLocation()}, mainFrame=${targetFrame?.isMainFrame()}",
            )
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        } else {
            NSLog(
                "$KAKAO_POSTCODE_LOG_PREFIX navigation blocked: ${url?.safeLogLocation()}, mainFrame=${targetFrame?.isMainFrame()}",
            )
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
        if (decidePolicyForNavigationResponse.canShowMIMEType &&
            url?.isAllowedKakaoPostcodeNavigation(
                isMainFrame = decidePolicyForNavigationResponse.isForMainFrame(),
            ) == true
        ) {
            NSLog("$KAKAO_POSTCODE_LOG_PREFIX response allowed: ${url.safeLogLocation()}")
            decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyAllow)
        } else {
            NSLog("$KAKAO_POSTCODE_LOG_PREFIX response blocked: ${url?.safeLogLocation()}")
            decisionHandler(WKNavigationResponsePolicy.WKNavigationResponsePolicyCancel)
            onNavigationBlocked()
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        NSLog("$KAKAO_POSTCODE_LOG_PREFIX page started: ${webView.URL?.safeLogLocation()}")
        onLoadingChanged(true)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        if (webView.URL?.isAllowedKakaoPostcodeUrl() == true) {
            NSLog("$KAKAO_POSTCODE_LOG_PREFIX page finished: ${webView.URL?.safeLogLocation()}")
            onLoadingChanged(false)
        } else {
            NSLog("$KAKAO_POSTCODE_LOG_PREFIX page finish blocked: ${webView.URL?.safeLogLocation()}")
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
        NSLog(
            "$KAKAO_POSTCODE_LOG_PREFIX provisional navigation failed: domain=${withError.domain}, code=${withError.code}, description=${withError.localizedDescription}",
        )
        onNetworkError()
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        NSLog(
            "$KAKAO_POSTCODE_LOG_PREFIX navigation failed: domain=${withError.domain}, code=${withError.code}, description=${withError.localizedDescription}",
        )
        onNetworkError()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.isAllowedKakaoPostcodeUrl(): Boolean =
    scheme.equals("https", ignoreCase = true) &&
        host in KAKAO_POSTCODE_ALLOWED_HOSTS &&
        (port == null || port?.intValue == 443) &&
        user == null &&
        password == null

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.isAllowedKakaoPostcodeNavigation(isMainFrame: Boolean?): Boolean =
    isAllowedKakaoPostcodeUrl() ||
        (isMainFrame == false && absoluteString == "about:blank")

@OptIn(ExperimentalForeignApi::class)
private fun NSURL.safeLogLocation(): String =
    buildString {
        append(scheme.orEmpty())
        append("://")
        append(host.orEmpty())
        append(path.orEmpty())
    }
