package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft

sealed interface MemberRegistrationUiState {
    data class Editing(
        val draft: MemberRegistrationDraft,
        val canSubmit: Boolean,
        val fieldErrors: Set<MemberRegistrationField> = emptySet(),
    ) : MemberRegistrationUiState

    data class Confirmation(
        val draft: MemberRegistrationDraft,
    ) : MemberRegistrationUiState

    /** Draft is retained while the confirmation dialog is submitting. */
    data class Submitting(
        val draft: MemberRegistrationDraft,
    ) : MemberRegistrationUiState

    data class Success(
        val memberId: InstructorMemberId,
    ) : MemberRegistrationUiState

    data class Error(
        val draft: MemberRegistrationDraft,
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

    data class SuccessAcknowledged(
        val memberId: InstructorMemberId,
    ) : MemberRegistrationAction
}

internal fun memberRegistrationFieldErrors(draft: MemberRegistrationDraft): Set<MemberRegistrationField> =
    buildSet {
        if (draft.name.isBlank()) add(MemberRegistrationField.NAME)
        if (draft.phoneNumber.filter(Char::isDigit).length !in 10..11) {
            add(MemberRegistrationField.PHONE_NUMBER)
        }
    }

internal fun MemberRegistrationDraft.isMemberRegistrationValid(): Boolean =
    memberRegistrationFieldErrors(this).isEmpty()
