package com.classitda.feature.auth.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.classitda.feature.auth.signup.component.SignupTermsSheet

private const val TERMS_URL = "https://classitda.com/terms"
private const val PRIVACY_POLICY_URL = "https://classitda.com/privacy-policy"

@Composable
internal fun SignupTermsPreviewScreen(onComplete: () -> Unit) {
    var termsAgreed by remember { mutableStateOf(false) }
    var privacyPolicyAgreed by remember { mutableStateOf(false) }
    var selectedTerm by remember { mutableStateOf<SignupTermLink?>(null) }

    if (selectedTerm == null) {
        SignupTermsSheet(
            allTermsAgreed = termsAgreed && privacyPolicyAgreed,
            termsAgreed = termsAgreed,
            privacyPolicyAgreed = privacyPolicyAgreed,
            onToggleAllTerms = {
                val next = !(termsAgreed && privacyPolicyAgreed)
                termsAgreed = next
                privacyPolicyAgreed = next
            },
            onToggleTerms = { termsAgreed = !termsAgreed },
            onTogglePrivacyPolicy = { privacyPolicyAgreed = !privacyPolicyAgreed },
            onComplete = onComplete,
            onDismiss = onComplete,
            onTermsClick = {
                selectedTerm = SignupTermLink("이용약관", TERMS_URL)
            },
            onPrivacyPolicyClick = {
                selectedTerm = SignupTermLink("개인정보처리방침", PRIVACY_POLICY_URL)
            },
        )
    } else {
        TermsWebViewScreen(
            title = selectedTerm!!.title,
            url = selectedTerm!!.url,
            onNavigateBack = { selectedTerm = null },
        )
    }
}
