package com.classitda.core.platform

import androidx.compose.runtime.Composable

private object UnsupportedGoogleSignInProvider : GoogleSignInProvider {
    override suspend fun signIn(): String = error("iOS Google 로그인은 아직 연결되지 않았습니다.")
}

@Composable
actual fun rememberGoogleSignInProvider(): GoogleSignInProvider = UnsupportedGoogleSignInProvider
