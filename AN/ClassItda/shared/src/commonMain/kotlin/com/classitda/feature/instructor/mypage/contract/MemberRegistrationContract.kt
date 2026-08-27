package com.classitda.feature.instructor.mypage.contract

sealed interface MemberRegistrationUiState {
    data class Editing(
        val draft: MemberInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<MemberRegistrationField> = emptySet(),
    ) : MemberRegistrationUiState

    data class Confirmation(
        val draft: MemberInputUiModel,
    ) : MemberRegistrationUiState

    /** Draft is retained while the confirmation dialog is submitting. */
    data class Submitting(
        val draft: MemberInputUiModel,
    ) : MemberRegistrationUiState

    data object Success : MemberRegistrationUiState

    data class Error(
        val draft: MemberInputUiModel,
        val reason: MemberRegistrationUiError,
    ) : MemberRegistrationUiState
}

enum class MemberRegistrationField {
    NAME,
    PHONE_NUMBER,
}

enum class MemberRegistrationUiError {
    NETWORK,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface MemberRegistrationAction {
    data object Back : MemberRegistrationAction

    data class NameChanged(
        val name: String,
    ) : MemberRegistrationAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : MemberRegistrationAction

    data object OpenConfirmation : MemberRegistrationAction

    data object CancelConfirmation : MemberRegistrationAction

    data object ConfirmRegistration : MemberRegistrationAction

    data object Retry : MemberRegistrationAction

    data object SuccessAcknowledged : MemberRegistrationAction
}

internal fun memberRegistrationFieldErrors(draft: MemberInputUiModel): Set<MemberRegistrationField> =
    buildSet {
        if (draft.name.isBlank()) add(MemberRegistrationField.NAME)
        if (!isMemberPhoneNumberValid(draft.phoneNumber)) {
            add(MemberRegistrationField.PHONE_NUMBER)
        }
    }

internal fun MemberInputUiModel.isMemberRegistrationValid(): Boolean = memberRegistrationFieldErrors(this).isEmpty()

internal fun isMemberPhoneNumberValid(value: String): Boolean {
    val digits = value.filter(Char::isDigit)
    return value.isNotBlank() &&
        value.all { it.isDigit() || it == '-' || it == ' ' } &&
        digits.length in 10..11
}
