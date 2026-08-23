package com.classitda.core.platform

import androidx.compose.runtime.Composable
import com.classitda.core.network.ClassItdaApiConfig
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val GOOGLE_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
private const val GOOGLE_CALLBACK_SUFFIX = ":/oauthredirect"

@OptIn(ExperimentalForeignApi::class)
private class IosGoogleSignInProvider : GoogleSignInProvider {
    private var authenticationSession: ASWebAuthenticationSession? = null

    override suspend fun signIn(): String =
        suspendCancellableCoroutine { continuation ->
            val clientId = ClassItdaApiConfig.GOOGLE_IOS_CLIENT_ID
            val callbackScheme = "com.googleusercontent.apps.${clientId.substringBefore(".apps.googleusercontent.com")}"
            val callbackUrl = callbackScheme + GOOGLE_CALLBACK_SUFFIX
            val nonce = NSUUID().UUIDString
            val state = NSUUID().UUIDString
            val url =
                "$GOOGLE_AUTH_ENDPOINT?client_id=$clientId" +
                    "&redirect_uri=$callbackUrl" +
                    "&response_type=id_token" +
                    "&scope=openid%20email%20profile" +
                    "&nonce=$nonce" +
                    "&state=$state" +
                    "&prompt=select_account"

            val session =
                ASWebAuthenticationSession(
                    uRL = NSURL(string = url),
                    callbackURLScheme = callbackScheme,
                ) { callbackUrl, error ->
                    authenticationSession = null
                    if (error != null) {
                        continuation.resumeWithException(
                            IllegalStateException("Google 로그인에 실패했습니다: ${error.localizedDescription}"),
                        )
                        return@ASWebAuthenticationSession
                    }
                    val callbackParameters = callbackUrl?.absoluteString?.substringAfter('#')?.let(::parseFragment)
                    if (callbackParameters?.get("state") != state) {
                        continuation.resumeWithException(IllegalStateException("Google 로그인 응답이 유효하지 않습니다."))
                        return@ASWebAuthenticationSession
                    }
                    val idToken = callbackParameters["id_token"]
                    if (idToken.isNullOrBlank()) {
                        continuation.resumeWithException(IllegalStateException("Google ID Token을 받지 못했습니다."))
                    } else {
                        continuation.resume(idToken)
                    }
                }
            session.presentationContextProvider = IosPresentationContextProvider()
            authenticationSession = session
            continuation.invokeOnCancellation {
                authenticationSession?.cancel()
                authenticationSession = null
            }
            if (!session.start()) {
                authenticationSession = null
                continuation.resumeWithException(IllegalStateException("Google 로그인 화면을 열 수 없습니다."))
            }
        }
}

private fun parseFragment(fragment: String): Map<String, String> =
    fragment
        .split('&')
        .mapNotNull { entry ->
            val separator = entry.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            entry.substring(0, separator) to entry.substring(separator + 1)
        }.toMap()

@OptIn(ExperimentalForeignApi::class)
private class IosPresentationContextProvider :
    NSObject(),
    platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): UIWindow =
        (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow) ?: UIWindow()
}

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider =
    androidx.compose.runtime.remember { IosGoogleSignInProvider() }
