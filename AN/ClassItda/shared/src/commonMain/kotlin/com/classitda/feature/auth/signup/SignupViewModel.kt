package com.classitda.feature.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupToken
import com.classitda.domain.repository.auth.signup.SignupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SignupViewModel(
    private val repository: SignupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onAction(action: SignupAction) {
        when (action) {
            SignupAction.LoginWithGoogle,
            SignupAction.LoginWithApple,
            SignupAction.Back,
            SignupAction.Close,
            SignupAction.DismissTerms,
            SignupAction.ToggleAllTerms,
            SignupAction.ToggleTermsAgreement,
            SignupAction.TogglePrivacyPolicyAgreement,
            SignupAction.OpenProfile,
            -> {
                reduce(action)
            }

            is SignupAction.ChangeName -> {
                update { copy(name = action.value, errorMessage = null) }
            }

            is SignupAction.ChangePhoneNumber -> {
                update { copy(phoneNumber = action.value, errorMessage = null) }
            }

            is SignupAction.ChangeVerificationCode -> {
                update {
                    copy(
                        verificationCode = action.value,
                        errorMessage = null,
                    )
                }
            }

            SignupAction.SendVerificationCode -> {
                requestVerification()
            }

            SignupAction.ConfirmForm -> {
                confirmVerification()
            }

            SignupAction.CompleteSignup -> {
                completeSignup()
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            runCatching { repository.loginWithGoogle(GoogleIdToken(idToken)) }
                .onSuccess { result ->
                    when (result) {
                        is com.classitda.domain.model.auth.signup.GoogleLoginResult.Registered -> {
                            update { copy(isLoading = false, page = SignupPage.Completed) }
                        }

                        is com.classitda.domain.model.auth.signup.GoogleLoginResult.RegistrationRequired -> {
                            val terms = repository.getTerms(result.signupToken)
                            update {
                                copy(
                                    isLoading = false,
                                    page = SignupPage.Form,
                                    signupToken = result.signupToken,
                                    terms = terms,
                                    errorMessage = null,
                                )
                            }
                        }
                    }
                }.onFailure { error -> showError(error) }
        }
    }

    fun showError(error: Throwable) {
        update { copy(isLoading = false, errorMessage = error.message ?: "요청에 실패했습니다.") }
    }

    private fun requestVerification() {
        val state = _uiState.value
        val token = state.signupToken ?: return showError(IllegalStateException("Google 로그인이 필요합니다."))
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.requestPhoneVerification(token, SignupPhoneNumber(state.phoneNumber))
            }.onSuccess { challenge ->
                update {
                    copy(
                        isLoading = false,
                        isVerificationSent = true,
                        verificationId = challenge.id,
                    )
                }
            }.onFailure { error -> showError(error) }
        }
    }

    private fun confirmVerification() {
        val state = _uiState.value
        val token = state.signupToken ?: return showError(IllegalStateException("Google 로그인이 필요합니다."))
        val verificationId = state.verificationId ?: return showError(IllegalStateException("인증번호를 먼저 요청해 주세요."))
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            runCatching {
                repository.confirmPhoneVerification(
                    token,
                    verificationId,
                    PhoneVerificationCode(state.verificationCode),
                )
            }.onSuccess {
                update { copy(isLoading = false, isPhoneVerified = true, isTermsVisible = true) }
            }.onFailure { error -> showError(error) }
        }
    }

    private fun completeSignup() {
        val state = _uiState.value
        val token = state.signupToken ?: return showError(IllegalStateException("Google 로그인이 필요합니다."))
        if (!state.isPhoneVerified) return showError(IllegalStateException("휴대전화 인증을 완료해 주세요."))
        val requiredTermIds = state.terms.filter { it.required }.map { it.id }
        viewModelScope.launch {
            update { copy(isLoading = true, errorMessage = null) }
            runCatching { repository.completeSignup(token, SignupName(state.name), requiredTermIds) }
                .onSuccess { update { copy(isLoading = false, page = SignupPage.Completed, isTermsVisible = false) } }
                .onFailure { error -> showError(error) }
        }
    }

    private fun reduce(action: SignupAction) {
        update {
            when (action) {
                SignupAction.LoginWithGoogle, SignupAction.LoginWithApple -> {
                    copy(errorMessage = null)
                }

                SignupAction.Back, SignupAction.Close, SignupAction.OpenProfile -> {
                    copy(page = SignupPage.Welcome, isTermsVisible = false)
                }

                SignupAction.DismissTerms -> {
                    copy(isTermsVisible = false)
                }

                SignupAction.ToggleAllTerms -> {
                    val next = !allTermsAgreed
                    copy(allTermsAgreed = next, termsAgreed = next, privacyPolicyAgreed = next)
                }

                SignupAction.ToggleTermsAgreement -> {
                    val next = !termsAgreed
                    copy(termsAgreed = next, allTermsAgreed = next && privacyPolicyAgreed)
                }

                SignupAction.TogglePrivacyPolicyAgreement -> {
                    val next = !privacyPolicyAgreed
                    copy(privacyPolicyAgreed = next, allTermsAgreed = termsAgreed && next)
                }

                else -> {
                    this
                }
            }
        }
    }

    private fun update(transform: SignupUiState.() -> SignupUiState) {
        _uiState.value = _uiState.value.transform()
    }
}
