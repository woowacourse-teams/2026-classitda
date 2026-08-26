package com.classitda.feature.instructor.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiError
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileUiError
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.common.profile.contract.ProfileViewUiState
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import com.classitda.feature.instructor.mypage.contract.MemberEditUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementDeleteError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.StudioDeleteError
import com.classitda.feature.instructor.mypage.contract.StudioDeleteState
import com.classitda.feature.instructor.mypage.contract.StudioDetailAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiError
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiState
import com.classitda.feature.instructor.mypage.contract.StudioEditUiError
import com.classitda.feature.instructor.mypage.contract.StudioEditUiState
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiError
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiState
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.isMemberRegistrationValid
import com.classitda.feature.instructor.mypage.contract.isStudioRegistrationValid
import com.classitda.feature.instructor.mypage.contract.memberRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.studioRegistrationFieldErrors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toUiModel() =
    InstructorMyPageUiModel(
        displayProfileName(name),
        maskProfilePhoneNumber(phoneNumber),
        profileImageUrl,
        name.firstOrNull()?.toString() ?: "?",
    )

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toProfileUiModel() =
    MemberProfileUiModel(displayProfileName(name), maskProfilePhoneNumber(phoneNumber), email, profileImageUrl)

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toEditingState() =
    ProfileEditUiState.Editing(toProfileUiModel(), phoneNumber, name, false)

private fun maskProfilePhoneNumber(value: String): String {
    val digits = value.filter(Char::isDigit)
    return if (digits.length >=
        8
    ) {
        "${digits.take(3)}-****-${digits.takeLast(4)}"
    } else {
        value
    }
}

private fun displayProfileName(value: String): String = value.trim().ifBlank { "이름 없음" }

internal fun InstructorMyPageFailureReason.toMyPageError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> InstructorMyPageUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> InstructorMyPageUiError.NOT_FOUND
        else -> InstructorMyPageUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toProfileUiError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> ProfileUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> ProfileUiError.NOT_FOUND
        InstructorMyPageFailureReason.CONFLICT -> ProfileUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> ProfileUiError.INVALID_REQUEST
        else -> ProfileUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toPhoneError() =
    when (this) {
        InstructorMyPageFailureReason.VERIFICATION_EXPIRED -> PhoneNumberChangeUiError.VERIFICATION_EXPIRED
        InstructorMyPageFailureReason.VERIFICATION_FAILED -> PhoneNumberChangeUiError.VERIFICATION_FAILED
        else -> PhoneNumberChangeUiError.REQUEST_FAILED
    }

internal fun InstructorMyPageFailureReason.toListError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberManagementUiError.NETWORK
        else -> MemberManagementUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toMemberRegistrationError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberRegistrationUiError.NETWORK
        InstructorMyPageFailureReason.CONFLICT -> MemberRegistrationUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> MemberRegistrationUiError.INVALID_REQUEST
        else -> MemberRegistrationUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toMemberManagementDeleteError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberManagementDeleteError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> MemberManagementDeleteError.NOT_FOUND
        else -> MemberManagementDeleteError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toMemberEditError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberEditUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> MemberEditUiError.NOT_FOUND
        InstructorMyPageFailureReason.CONFLICT -> MemberEditUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> MemberEditUiError.INVALID_REQUEST
        else -> MemberEditUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toStudioError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> StudioManagementUiError.NETWORK
        else -> StudioManagementUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toStudioRegistrationError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> StudioRegistrationUiError.NETWORK
        InstructorMyPageFailureReason.CONFLICT -> StudioRegistrationUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> StudioRegistrationUiError.INVALID_REQUEST
        else -> StudioRegistrationUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toStudioDetailError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> StudioDetailUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> StudioDetailUiError.NOT_FOUND
        else -> StudioDetailUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toStudioDeleteError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> StudioDeleteError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> StudioDeleteError.NOT_FOUND
        else -> StudioDeleteError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toStudioEditError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> StudioEditUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> StudioEditUiError.NOT_FOUND
        InstructorMyPageFailureReason.FORBIDDEN -> StudioEditUiError.FORBIDDEN
        InstructorMyPageFailureReason.CONFLICT -> StudioEditUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> StudioEditUiError.INVALID_REQUEST
        else -> StudioEditUiError.UNKNOWN
    }
