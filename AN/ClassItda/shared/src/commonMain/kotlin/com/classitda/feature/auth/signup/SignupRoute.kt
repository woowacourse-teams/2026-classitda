package com.classitda.feature.auth.signup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun SignupRoute(
    onSignupCompleted: () -> Unit,
    onLoginCompleted: () -> Unit = {},
    onWithdrawalPending: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    SignupScreen(
        onSignupCompleted = onSignupCompleted,
        onLoginCompleted = onLoginCompleted,
        onWithdrawalPending = onWithdrawalPending,
        modifier = modifier,
    )
}
