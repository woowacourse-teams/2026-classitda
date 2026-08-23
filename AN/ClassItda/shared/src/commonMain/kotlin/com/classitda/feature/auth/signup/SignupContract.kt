package com.classitda.feature.auth.signup

import com.classitda.domain.model.auth.signup.PhoneVerificationId
import com.classitda.domain.model.auth.signup.SignupTerm
import com.classitda.domain.model.auth.signup.SignupToken

enum class SignupPage {
    Welcome,
    Form,
    Completed,
}

data class SignupUiState(
    val page: SignupPage = SignupPage.Welcome,
    val name: String = "",
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isVerificationSent: Boolean = false,
    val allTermsAgreed: Boolean = false,
    val termsAgreed: Boolean = false,
    val privacyPolicyAgreed: Boolean = false,
    val isTermsVisible: Boolean = false,
    val signupToken: SignupToken? = null,
    val verificationId: PhoneVerificationId? = null,
    val terms: List<SignupTerm> = emptyList(),
    val isPhoneVerified: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface SignupAction {
    data object LoginWithGoogle : SignupAction

    data object LoginWithApple : SignupAction

    data object Back : SignupAction

    data object Close : SignupAction

    data class ChangeName(
        val value: String,
    ) : SignupAction

    data class ChangePhoneNumber(
        val value: String,
    ) : SignupAction

    data class ChangeVerificationCode(
        val value: String,
    ) : SignupAction

    data object SendVerificationCode : SignupAction

    data object ConfirmForm : SignupAction

    data object DismissTerms : SignupAction

    data object ToggleAllTerms : SignupAction

    data object ToggleTermsAgreement : SignupAction

    data object TogglePrivacyPolicyAgreement : SignupAction

    data object CompleteSignup : SignupAction

    data object OpenProfile : SignupAction
}
