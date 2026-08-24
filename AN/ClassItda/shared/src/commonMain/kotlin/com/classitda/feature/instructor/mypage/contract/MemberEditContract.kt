package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft

sealed interface MemberEditUiState {
    data object Loading : MemberEditUiState

    data class Editing(
        val memberId: InstructorMemberId,
        val draft: MemberRegistrationDraft,
        val canSubmit: Boolean,
        val fieldErrors: Set<MemberEditField> = emptySet(),
    ) : MemberEditUiState

    data class Submitting(
        val memberId: InstructorMemberId,
        val draft: MemberRegistrationDraft,
    ) : MemberEditUiState

    data class Success(
        val memberId: InstructorMemberId,
    ) : MemberEditUiState

    data class Error(
        val memberId: InstructorMemberId,
        val draft: MemberRegistrationDraft,
        val reason: MemberEditUiError,
    ) : MemberEditUiState
}

enum class MemberEditField {
    NAME,
    PHONE_NUMBER,
}

enum class MemberEditUiError {
    NETWORK,
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface MemberEditAction {
    data object Back : MemberEditAction

    data class NameChanged(
        val name: String,
    ) : MemberEditAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : MemberEditAction

    data object Submit : MemberEditAction

    data class SuccessAcknowledged(
        val memberId: InstructorMemberId,
    ) : MemberEditAction

    data object Retry : MemberEditAction
}

internal fun memberEditFieldErrors(draft: MemberRegistrationDraft): Set<MemberEditField> =
    buildSet {
        if (draft.name.isBlank()) add(MemberEditField.NAME)
        if (draft.phoneNumber.filter(Char::isDigit).length !in 10..11) {
            add(MemberEditField.PHONE_NUMBER)
        }
    }

internal fun MemberRegistrationDraft.isMemberEditValid(): Boolean = memberEditFieldErrors(this).isEmpty()
