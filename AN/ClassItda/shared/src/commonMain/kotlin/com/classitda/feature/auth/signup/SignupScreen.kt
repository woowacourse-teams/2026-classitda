package com.classitda.feature.auth.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.auth.signup.component.SignupTermsSheet
import com.classitda.feature.auth.signup.screen.SignupCompletedScreen
import com.classitda.feature.auth.signup.screen.SignupFormScreen
import com.classitda.feature.auth.signup.screen.SignupWelcomeScreen

@Composable
internal fun SignupScreen(
    onSignupCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(SignupUiState()) }

    SignupScreenStateless(
        state = state,
        onAction = { action ->
            val wasCompleted = state.page == SignupPage.Completed
            state = state.reduce(action)
            if (wasCompleted && action == SignupAction.Close) {
                onSignupCompleted()
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
    }
}

private fun SignupUiState.reduce(action: SignupAction): SignupUiState =
    when (action) {
        SignupAction.LoginWithGoogle -> {
            copy(page = SignupPage.Form)
        }

        SignupAction.LoginWithApple -> {
            copy(page = SignupPage.Form)
        }

        SignupAction.Back -> {
            copy(page = SignupPage.Welcome, isTermsVisible = false)
        }

        SignupAction.Close -> {
            copy(page = SignupPage.Welcome, isTermsVisible = false)
        }

        is SignupAction.ChangeName -> {
            copy(name = action.value)
        }

        is SignupAction.ChangePhoneNumber -> {
            copy(phoneNumber = action.value)
        }

        is SignupAction.ChangeVerificationCode -> {
            copy(verificationCode = action.value)
        }

        SignupAction.SendVerificationCode -> {
            copy(isVerificationSent = true)
        }

        SignupAction.ConfirmForm -> {
            copy(isTermsVisible = true)
        }

        SignupAction.DismissTerms -> {
            copy(isTermsVisible = false)
        }

        SignupAction.ToggleAllTerms -> {
            val nextValue = !allTermsAgreed
            copy(
                allTermsAgreed = nextValue,
                termsAgreed = nextValue,
                privacyPolicyAgreed = nextValue,
            )
        }

        SignupAction.ToggleTermsAgreement -> {
            val nextValue = !termsAgreed
            copy(
                termsAgreed = nextValue,
                allTermsAgreed = nextValue && privacyPolicyAgreed,
            )
        }

        SignupAction.TogglePrivacyPolicyAgreement -> {
            val nextValue = !privacyPolicyAgreed
            copy(
                privacyPolicyAgreed = nextValue,
                allTermsAgreed = termsAgreed && nextValue,
            )
        }

        SignupAction.CompleteSignup -> {
            copy(page = SignupPage.Completed, isTermsVisible = false)
        }

        SignupAction.OpenProfile -> {
            copy(page = SignupPage.Welcome)
        }
    }
