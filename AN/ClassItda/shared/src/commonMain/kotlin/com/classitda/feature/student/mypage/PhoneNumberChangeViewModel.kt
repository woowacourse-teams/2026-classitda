package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.mypage.PhoneVerificationId
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiError
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PhoneNumberChangeViewModel(
    private val repository: MyPageRepository,
    initialPhoneNumber: String = "",
    private val initialRemainingSeconds: Int = DEFAULT_REMAINING_SECONDS,
) : ViewModel() {
    init {
        require(initialRemainingSeconds >= 0) { "인증 남은 시간은 0 이상이어야 합니다." }
    }

    private val _uiState =
        MutableStateFlow<PhoneNumberChangeUiState>(PhoneNumberChangeUiState.Editing(initialPhoneNumber, ""))
    val uiState: StateFlow<PhoneNumberChangeUiState> = _uiState.asStateFlow()
    private var countdownJob: Job? = null
    private var verificationId: PhoneVerificationId? = null

    fun onAction(action: PhoneNumberChangeAction) {
        when (action) {
            PhoneNumberChangeAction.Back, PhoneNumberChangeAction.Complete -> Unit
            PhoneNumberChangeAction.Retry -> retry()
            is PhoneNumberChangeAction.PhoneNumberChanged -> changePhoneNumber(action.phoneNumber)
            PhoneNumberChangeAction.RequestVerification -> requestVerification()
            is PhoneNumberChangeAction.VerificationCodeChanged -> changeVerificationCode(action.verificationCode)
            PhoneNumberChangeAction.VerifyCode -> verifyCode()
        }
    }

    private fun retry() {
        val state = _uiState.value as? PhoneNumberChangeUiState.Error ?: return
        if (state.reason == PhoneNumberChangeUiError.REQUEST_FAILED ||
            state.reason == PhoneNumberChangeUiError.VERIFICATION_EXPIRED
        ) {
            verificationId = null
            _uiState.value = PhoneNumberChangeUiState.Editing(state.phoneNumber, "")
            requestVerification()
        } else {
            val id = verificationId ?: return
            _uiState.value =
                PhoneNumberChangeUiState.CodeEntry(
                    state.phoneNumber,
                    state.verificationCode,
                    state.remainingSeconds ?: initialRemainingSeconds,
                )
            startCountdown(id)
        }
    }

    private fun changePhoneNumber(phoneNumber: String) {
        stopCountdown()
        verificationId = null
        when (_uiState.value) {
            is PhoneNumberChangeUiState.Editing, is PhoneNumberChangeUiState.Error -> {
                _uiState.value = PhoneNumberChangeUiState.Editing(phoneNumber, "")
            }

            else -> {}
        }
    }

    private fun requestVerification() {
        stopCountdown()
        val phoneNumber = (_uiState.value as? PhoneNumberChangeUiState.Editing)?.phoneNumber ?: return
        if (phoneNumber.isBlank()) return
        _uiState.value = PhoneNumberChangeUiState.Requesting(phoneNumber, "")
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.requestPhoneVerification(phoneNumber)) {
                    is MyPageResult.Success -> {
                        verificationId = result.value.verificationId
                        startCountdown(result.value.verificationId)
                        PhoneNumberChangeUiState.CodeEntry(phoneNumber, "", initialRemainingSeconds)
                    }

                    is MyPageResult.Failure -> {
                        PhoneNumberChangeUiState.Error(phoneNumber, "", result.reason.toRequestUiError())
                    }
                }
        }
    }

    private fun changeVerificationCode(value: String) {
        val sanitized = value.filter { it in '0'..'9' }.take(VERIFICATION_CODE_LENGTH)
        when (val state = _uiState.value) {
            is PhoneNumberChangeUiState.CodeEntry -> {
                _uiState.value = state.copy(verificationCode = sanitized)
            }

            is PhoneNumberChangeUiState.Error -> {
                if (state.reason == PhoneNumberChangeUiError.VERIFICATION_FAILED) {
                    val id = verificationId ?: return
                    _uiState.value =
                        PhoneNumberChangeUiState.CodeEntry(
                            state.phoneNumber,
                            sanitized,
                            state.remainingSeconds ?: initialRemainingSeconds,
                        )
                    startCountdown(id)
                }
            }

            else -> {}
        }
    }

    private fun verifyCode() {
        val state = _uiState.value as? PhoneNumberChangeUiState.CodeEntry ?: return
        val id = verificationId ?: return
        if (state.verificationCode.length != VERIFICATION_CODE_LENGTH || state.remainingSeconds <= 0) return
        stopCountdown()
        _uiState.value =
            PhoneNumberChangeUiState.Verifying(state.phoneNumber, state.verificationCode, state.remainingSeconds)
        viewModelScope.launch {
            val result = repository.verifyPhoneNumber(id, state.phoneNumber, state.verificationCode)
            _uiState.value =
                when (result) {
                    is MyPageResult.Success -> {
                        PhoneNumberChangeUiState.Verified(result.value.phoneNumber, state.verificationCode)
                    }

                    is MyPageResult.Failure -> {
                        val remaining =
                            if (result.reason ==
                                MyPageFailureReason.VERIFICATION_EXPIRED
                            ) {
                                0
                            } else {
                                state.remainingSeconds
                            }
                        val next =
                            PhoneNumberChangeUiState.Error(
                                state.phoneNumber,
                                state.verificationCode,
                                result.reason.toVerificationUiError(),
                                remaining,
                            )
                        if (remaining > 0) startCountdown(id)
                        next
                    }
                }
        }
    }

    private fun startCountdown(id: PhoneVerificationId) {
        countdownJob?.cancel()
        countdownJob =
            viewModelScope.launch {
                while (true) {
                    delay(ONE_SECOND_MILLIS)
                    if (verificationId != id) break
                    val state = _uiState.value
                    val remaining =
                        when (state) {
                            is PhoneNumberChangeUiState.CodeEntry -> {
                                state.remainingSeconds
                            }

                            is PhoneNumberChangeUiState.Error -> {
                                if (state.reason ==
                                    PhoneNumberChangeUiError.VERIFICATION_EXPIRED
                                ) {
                                    break
                                } else {
                                    state.remainingSeconds ?: break
                                }
                            }

                            else -> {
                                break
                            }
                        }
                    val nextRemaining = remaining - 1
                    if (nextRemaining <= 0) {
                        val phoneNumber = state.phoneNumberOrNull() ?: break
                        _uiState.value =
                            PhoneNumberChangeUiState.Error(
                                phoneNumber,
                                state.verificationCodeOrEmpty(),
                                PhoneNumberChangeUiError.VERIFICATION_EXPIRED,
                                0,
                            )
                        break
                    }
                    _uiState.value =
                        when (val current = _uiState.value) {
                            is PhoneNumberChangeUiState.CodeEntry -> current.copy(remainingSeconds = nextRemaining)
                            is PhoneNumberChangeUiState.Error -> current.copy(remainingSeconds = nextRemaining)
                            else -> break
                        }
                }
            }
    }

    private fun PhoneNumberChangeUiState.phoneNumberOrNull(): String? =
        when (this) {
            is PhoneNumberChangeUiState.CodeEntry -> phoneNumber
            is PhoneNumberChangeUiState.Error -> phoneNumber
            else -> null
        }

    private fun PhoneNumberChangeUiState.verificationCodeOrEmpty(): String =
        when (this) {
            is PhoneNumberChangeUiState.CodeEntry -> verificationCode
            is PhoneNumberChangeUiState.Error -> verificationCode
            else -> ""
        }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        stopCountdown()
        super.onCleared()
    }
}

private fun MyPageFailureReason.toRequestUiError(): PhoneNumberChangeUiError =
    when (this) {
        MyPageFailureReason.VERIFICATION_EXPIRED -> PhoneNumberChangeUiError.VERIFICATION_EXPIRED
        else -> PhoneNumberChangeUiError.REQUEST_FAILED
    }

private fun MyPageFailureReason.toVerificationUiError(): PhoneNumberChangeUiError =
    when (this) {
        MyPageFailureReason.VERIFICATION_EXPIRED -> PhoneNumberChangeUiError.VERIFICATION_EXPIRED
        else -> PhoneNumberChangeUiError.VERIFICATION_FAILED
    }

private const val DEFAULT_REMAINING_SECONDS = 180
private const val VERIFICATION_CODE_LENGTH = 6
private const val ONE_SECOND_MILLIS = 1_000L
