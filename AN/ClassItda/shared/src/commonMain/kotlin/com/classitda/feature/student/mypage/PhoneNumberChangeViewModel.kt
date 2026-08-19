package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeAction
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeUiState
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
        MutableStateFlow<PhoneNumberChangeUiState>(
            PhoneNumberChangeUiState.Editing(
                phoneNumber = initialPhoneNumber,
                verificationCode = "",
            ),
        )
    val uiState: StateFlow<PhoneNumberChangeUiState> = _uiState.asStateFlow()

    fun onAction(action: PhoneNumberChangeAction) {
        when (action) {
            PhoneNumberChangeAction.Back,
            PhoneNumberChangeAction.Complete,
            -> Unit

            PhoneNumberChangeAction.Retry -> retry()

            is PhoneNumberChangeAction.PhoneNumberChanged -> changePhoneNumber(action.phoneNumber)

            PhoneNumberChangeAction.RequestVerification -> requestVerification()

            is PhoneNumberChangeAction.VerificationCodeChanged -> changeVerificationCode(action.verificationCode)

            PhoneNumberChangeAction.VerifyCode -> verifyCode()
        }
    }

    private fun retry() {
        when (val state = _uiState.value) {
            is PhoneNumberChangeUiState.Error -> {
                if (state.verificationId == null || state.reason == MyPageFailureReason.VERIFICATION_EXPIRED) {
                    _uiState.value =
                        PhoneNumberChangeUiState.Editing(
                            phoneNumber = state.phoneNumber,
                            verificationCode = "",
                        )
                    requestVerification()
                } else {
                    _uiState.value =
                        PhoneNumberChangeUiState.CodeEntry(
                            phoneNumber = state.phoneNumber,
                            verificationCode = state.verificationCode,
                            verificationId = state.verificationId,
                            remainingSeconds = initialRemainingSeconds,
                        )
                }
            }

            else -> {
                return
            }
        }
    }

    private fun changePhoneNumber(phoneNumber: String) {
        when (_uiState.value) {
            is PhoneNumberChangeUiState.Editing,
            is PhoneNumberChangeUiState.Error,
            -> {
                _uiState.value =
                    PhoneNumberChangeUiState.Editing(
                        phoneNumber = phoneNumber,
                        verificationCode = "",
                    )
            }

            else -> {
                return
            }
        }
    }

    private fun requestVerification() {
        val state = _uiState.value
        val phoneNumber =
            when (state) {
                is PhoneNumberChangeUiState.Editing -> state.phoneNumber
                else -> return
            }
        if (phoneNumber.isBlank()) return

        _uiState.value = PhoneNumberChangeUiState.Requesting(phoneNumber, "")
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.requestPhoneVerification(phoneNumber)) {
                    is MyPageResult.Success -> {
                        PhoneNumberChangeUiState.CodeEntry(
                            phoneNumber = phoneNumber,
                            verificationCode = "",
                            verificationId = result.value.verificationId,
                            remainingSeconds = initialRemainingSeconds,
                        )
                    }

                    is MyPageResult.Failure -> {
                        PhoneNumberChangeUiState.Error(
                            phoneNumber = phoneNumber,
                            verificationCode = "",
                            verificationId = null,
                            reason = result.reason,
                        )
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
                if (state.verificationId != null && state.reason != MyPageFailureReason.VERIFICATION_EXPIRED) {
                    _uiState.value =
                        PhoneNumberChangeUiState.CodeEntry(
                            phoneNumber = state.phoneNumber,
                            verificationCode = sanitized,
                            verificationId = state.verificationId,
                            remainingSeconds = initialRemainingSeconds,
                        )
                }
            }

            else -> {
                return
            }
        }
    }

    private fun verifyCode() {
        val state = _uiState.value as? PhoneNumberChangeUiState.CodeEntry ?: return
        if (state.verificationCode.length != VERIFICATION_CODE_LENGTH || state.remainingSeconds <= 0) return

        _uiState.value =
            PhoneNumberChangeUiState.Verifying(
                phoneNumber = state.phoneNumber,
                verificationCode = state.verificationCode,
                verificationId = state.verificationId,
                remainingSeconds = state.remainingSeconds,
            )
        viewModelScope.launch {
            val result =
                repository.verifyPhoneNumber(
                    verificationId = state.verificationId,
                    phoneNumber = state.phoneNumber,
                    verificationCode = state.verificationCode,
                )
            _uiState.value =
                when (result) {
                    is MyPageResult.Success -> {
                        PhoneNumberChangeUiState.Verified(
                            phoneNumber = result.value.phoneNumber,
                            verificationCode = state.verificationCode,
                        )
                    }

                    is MyPageResult.Failure -> {
                        PhoneNumberChangeUiState.Error(
                            phoneNumber = state.phoneNumber,
                            verificationCode = state.verificationCode,
                            verificationId = state.verificationId,
                            reason = result.reason,
                        )
                    }
                }
        }
    }

    private companion object {
        const val DEFAULT_REMAINING_SECONDS = 180
        const val VERIFICATION_CODE_LENGTH = 6
    }
}
