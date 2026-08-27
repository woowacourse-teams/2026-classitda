package com.classitda.feature.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupToken
import com.classitda.domain.repository.auth.signup.SignupRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class SignupViewModel(
    private val repository: SignupRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<SignupEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SignupEvent> = _events.asSharedFlow()
    private var verificationTimerJob: Job? = null

    fun reset() {
        verificationTimerJob?.cancel()
        verificationTimerJob = null
        _uiState.value = SignupUiState()
    }

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
                update { copy(name = action.value, nameError = null, errorMessage = null) }
            }

            is SignupAction.ChangePhoneNumber -> {
                val isRequestedPhone = action.value == _uiState.value.verificationPhoneNumber
                update {
                    copy(
                        phoneNumber = action.value,
                        verificationCode = "",
                        isPhoneVerified = isRequestedPhone && isPhoneVerified,
                        phoneNumberError = null,
                        verificationCodeError = null,
                        errorMessage = null,
                    )
                }
            }

            is SignupAction.ChangeVerificationCode -> {
                update {
                    copy(
                        verificationCode = action.value,
                        verificationCodeError = null,
                        errorMessage = null,
                    )
                }
            }

            SignupAction.SendVerificationCode -> {
                if (_uiState.value.isPhoneVerified) {
                    showError(IllegalStateException("이미 휴대전화 인증이 완료되었습니다."))
                } else {
                    requestVerification()
                }
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
                            update { copy(isLoading = false) }
                            Logger.d("SignupFlow: existing member Google login completed")
                            _events.tryEmit(SignupEvent.LoginCompleted)
                        }

                        is com.classitda.domain.model.auth.signup.GoogleLoginResult.RegistrationRequired -> {
                            Logger.d("SignupFlow: registration required after Google login")
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
        val message = error.message ?: "요청에 실패했습니다."
        val normalizedMessage = message.lowercase()
        when {
            message.contains("410") ||
                message.contains("PHONE-003") ||
                message.contains("인증번호가 만료") -> {
                update {
                    copy(
                        isLoading = false,
                        verificationId = null,
                        verificationCode = "",
                        isPhoneVerified = false,
                        verificationCodeError = "인증번호가 만료되었거나 이미 처리되었습니다. 재요청해 주세요.",
                        errorMessage = null,
                    )
                }
            }

            message.contains("이미 가입") || normalizedMessage.contains("already registered") -> {
                update {
                    copy(
                        isLoading = false,
                        phoneNumberError = "이미 가입된 전화번호입니다.",
                        errorMessage = null,
                    )
                }
            }

            message.contains("6자리") ||
                normalizedMessage.contains("verification code") -> {
                update {
                    copy(
                        isLoading = false,
                        verificationCodeError = "인증번호는 6자리 숫자로 입력해 주세요.",
                        errorMessage = null,
                    )
                }
            }

            message.contains("인증번호") || normalizedMessage.contains("otp") -> {
                update {
                    copy(
                        isLoading = false,
                        verificationCodeError = "인증번호가 올바르지 않습니다.",
                        errorMessage = null,
                    )
                }
            }

            message.contains("이름") -> {
                update {
                    copy(
                        isLoading = false,
                        nameError = "이름을 올바르게 입력해 주세요.",
                        errorMessage = null,
                    )
                }
            }

            message.contains("휴대전화") || message.contains("전화번호") || normalizedMessage.contains("phone") -> {
                update {
                    copy(
                        isLoading = false,
                        phoneNumberError = "휴대전화 번호를 올바르게 입력해 주세요.",
                        errorMessage = null,
                    )
                }
            }

            else -> {
                update { copy(isLoading = false, errorMessage = "요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.") }
            }
        }
    }

    private fun requestVerification() {
        val state = _uiState.value
        val token = state.signupToken ?: return showError(IllegalStateException("Google 로그인이 필요합니다."))
        if (!Regex("^010[0-9]{8}$").matches(state.phoneNumber)) {
            return showError(IllegalStateException("휴대전화 번호를 올바르게 입력해 주세요."))
        }
        viewModelScope.launch {
            update {
                copy(
                    isLoading = true,
                    verificationId = null,
                    verificationCode = "",
                    isPhoneVerified = false,
                    errorMessage = null,
                )
            }
            runCatching {
                repository.requestPhoneVerification(token, SignupPhoneNumber(state.phoneNumber))
            }.onSuccess { challenge ->
                update {
                    copy(
                        isLoading = false,
                        isVerificationSent = true,
                        verificationId = challenge.id,
                        verificationPhoneNumber = state.phoneNumber,
                        verificationRemainingSeconds = challenge.expiresInSeconds,
                        resendRemainingSeconds = challenge.resendAfterSeconds,
                    )
                }
                startVerificationTimer()
            }.onFailure { error -> showError(error) }
        }
    }

    private fun startVerificationTimer() {
        verificationTimerJob?.cancel()
        verificationTimerJob =
            viewModelScope.launch {
                while (isActive &&
                    (_uiState.value.verificationRemainingSeconds > 0 || _uiState.value.resendRemainingSeconds > 0)
                ) {
                    delay(1_000)
                    update {
                        copy(
                            verificationRemainingSeconds = (verificationRemainingSeconds - 1).coerceAtLeast(0),
                            resendRemainingSeconds = (resendRemainingSeconds - 1).coerceAtLeast(0),
                        )
                    }
                }
            }
    }

    override fun onCleared() {
        verificationTimerJob?.cancel()
        super.onCleared()
    }

    private fun confirmVerification() {
        val state = _uiState.value
        val token = state.signupToken ?: return showError(IllegalStateException("Google 로그인이 필요합니다."))
        if (!Regex("^[0-9]{6}$").matches(state.verificationCode)) {
            return showError(IllegalStateException("인증번호는 6자리 숫자로 입력해 주세요."))
        }
        if (state.verificationRemainingSeconds <= 0L) {
            return showError(IllegalStateException("인증번호가 만료되었습니다. 재요청해 주세요."))
        }
        if (state.phoneNumber != state.verificationPhoneNumber) {
            return showError(IllegalStateException("현재 휴대전화 번호로 인증요청을 해주세요."))
        }
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
                verificationTimerJob?.cancel()
                update {
                    copy(
                        isLoading = false,
                        isPhoneVerified = true,
                        verificationRemainingSeconds = 0,
                        resendRemainingSeconds = 0,
                        isTermsVisible = true,
                    )
                }
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
