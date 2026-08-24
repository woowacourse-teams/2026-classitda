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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.StuColors
import com.classitda.core.platform.rememberGoogleSignInProvider
import com.classitda.domain.model.auth.signup.SignupTerm
import com.classitda.domain.model.auth.signup.SignupTermCode
import com.classitda.feature.auth.signup.component.SignupTermsSheet
import com.classitda.feature.auth.signup.screen.SignupCompletedScreen
import com.classitda.feature.auth.signup.screen.SignupFormScreen
import com.classitda.feature.auth.signup.screen.SignupWelcomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val SERVICE_TERMS_URL = "https://classitda.com/terms"
private const val PRIVACY_POLICY_URL = "https://classitda.com/privacy-policy"

@Composable
internal fun SignupScreen(
    onSignupCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignupViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val googleSignInProvider = rememberGoogleSignInProvider()
    val scope = rememberCoroutineScope()
    var selectedTerm by remember { mutableStateOf<SignupTermLink?>(null) }

    LaunchedEffect(state.page) {
        if (state.page == SignupPage.Completed) onSignupCompleted()
    }

    if (selectedTerm == null) {
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
            onTermClick = { selectedTerm = it },
        )
    } else {
        TermsWebViewScreen(
            title = selectedTerm!!.title,
            url = selectedTerm!!.url,
            onNavigateBack = { selectedTerm = null },
        )
    }
}

internal data class SignupTermLink(
    val title: String,
    val url: String,
)

@Composable
internal fun SignupScreenStateless(
    state: SignupUiState,
    onAction: (SignupAction) -> Unit,
    modifier: Modifier = Modifier,
    onTermClick: (SignupTermLink) -> Unit = {},
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
                onTermsClick = {
                    state.terms
                        .firstOrNull { it.code == SignupTermCode.SERVICE_TERMS }
                        ?.let { onTermClick(SignupTermLink(it.title, it.webUrl())) }
                },
                onPrivacyPolicyClick = {
                    state.terms
                        .firstOrNull { it.code == SignupTermCode.PRIVACY_POLICY }
                        ?.let { onTermClick(SignupTermLink(it.title, it.webUrl())) }
                },
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

private fun SignupTerm.webUrl(): String =
    when (code) {
        SignupTermCode.SERVICE_TERMS -> SERVICE_TERMS_URL
        SignupTermCode.PRIVACY_POLICY -> PRIVACY_POLICY_URL
        SignupTermCode.MARKETING_CONSENT -> url
    }
