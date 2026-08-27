package com.classitda.core.platform

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.classitda.core.network.ClassItdaApiConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

private class AndroidGoogleSignInProvider(
    private val context: Context,
) : GoogleSignInProvider {
    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(): String {
        val option =
            GetGoogleIdOption
                .Builder()
                .setServerClientId(ClassItdaApiConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) { "지원하지 않는 Google Credential 응답입니다." }
        return try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (error: GoogleIdTokenParsingException) {
            throw IllegalStateException("Google ID Token을 읽을 수 없습니다.", error)
        }
    }
}

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider {
    val context = LocalContext.current
    return remember(context) { AndroidGoogleSignInProvider(context) }
}
