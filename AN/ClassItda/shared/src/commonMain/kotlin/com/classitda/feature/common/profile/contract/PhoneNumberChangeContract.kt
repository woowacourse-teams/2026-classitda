package com.classitda.feature.common.profile.contract

enum class PhoneNumberChangeUiError {
    REQUEST_FAILED,
    VERIFICATION_EXPIRED,
    VERIFICATION_FAILED,
    UNKNOWN,
}

sealed interface PhoneNumberChangeUiState {
    data object Loading : PhoneNumberChangeUiState

    data class Editing(
        val phoneNumber: String,
        val verificationCode: String,
    ) : PhoneNumberChangeUiState

    data class Requesting(
        val phoneNumber: String,
        val verificationCode: String,
    ) : PhoneNumberChangeUiState

    data class CodeEntry(
        val phoneNumber: String,
        val verificationCode: String,
        val remainingSeconds: Int,
    ) : PhoneNumberChangeUiState

    data class Verifying(
        val phoneNumber: String,
        val verificationCode: String,
        val remainingSeconds: Int,
    ) : PhoneNumberChangeUiState

    data class Verified(
        val phoneNumber: String,
        val verificationCode: String,
    ) : PhoneNumberChangeUiState

    data class Error(
        val phoneNumber: String,
        val verificationCode: String,
        val reason: PhoneNumberChangeUiError,
        val remainingSeconds: Int? = null,
    ) : PhoneNumberChangeUiState
}

sealed interface PhoneNumberChangeAction {
    data object Back : PhoneNumberChangeAction

    data object Retry : PhoneNumberChangeAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : PhoneNumberChangeAction

    data object RequestVerification : PhoneNumberChangeAction

    data class VerificationCodeChanged(
        val verificationCode: String,
    ) : PhoneNumberChangeAction

    data object VerifyCode : PhoneNumberChangeAction

    data object Complete : PhoneNumberChangeAction
}
