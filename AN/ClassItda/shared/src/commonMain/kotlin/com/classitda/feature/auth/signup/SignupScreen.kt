package com.classitda.feature.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.StuColors
import com.classitda.core.platform.rememberGoogleSignInProvider
import com.classitda.feature.auth.signup.component.SignupTermsSheet
import com.classitda.feature.auth.signup.screen.SignupCompletedScreen
import com.classitda.feature.auth.signup.screen.SignupFormScreen
import com.classitda.feature.auth.signup.screen.SignupWelcomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SignupScreen(
    onSignupCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val googleSignInProvider = rememberGoogleSignInProvider()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.page) {
        if (state.page == SignupPage.Completed) onSignupCompleted()
    }

    SignupScreenStateless(
        state = state,
        onAction = { action ->
            if (action == SignupAction.LoginWithGoogle) {
                scope.launch {
                    runCatching { googleSignInProvider.signIn() }
                        .onSuccess(viewModel::loginWithGoogle)
                        .onFailure(viewModel::showError)
                }
            } else {
                viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun SignupScreenStateless(
    state: SignupUiState,
    onAction: (SignupAction) -> Unit,
    modifier: Modifier = Modifier,
    onTermsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Surface)
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        when (state.page) {
            SignupPage.Welcome -> SignupWelcomeScreen(onAction = onAction)
            SignupPage.Form -> SignupFormScreen(state = state, onAction = onAction)
            SignupPage.Completed -> SignupCompletedScreen(onAction = onAction)
        }

        if (state.isTermsVisible) {
            SignupTermsSheet(
                allTermsAgreed = state.allTermsAgreed,
                termsAgreed = state.termsAgreed,
                privacyPolicyAgreed = state.privacyPolicyAgreed,
                onToggleAllTerms = { onAction(SignupAction.ToggleAllTerms) },
                onToggleTerms = { onAction(SignupAction.ToggleTermsAgreement) },
                onTogglePrivacyPolicy = { onAction(SignupAction.TogglePrivacyPolicyAgreement) },
                onComplete = { onAction(SignupAction.CompleteSignup) },
                onDismiss = { onAction(SignupAction.DismissTerms) },
                onTermsClick = onTermsClick,
                onPrivacyPolicyClick = onPrivacyPolicyClick,
            )
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                color = StuColors.Red,
            )
        }
    }
}
