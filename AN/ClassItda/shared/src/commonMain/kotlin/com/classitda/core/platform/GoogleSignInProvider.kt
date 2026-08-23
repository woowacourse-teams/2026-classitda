package com.classitda.core.platform

import androidx.compose.runtime.Composable

interface GoogleSignInProvider {
    suspend fun signIn(): String
}

@Composable
expect fun rememberGoogleSignInProvider(): GoogleSignInProvider
