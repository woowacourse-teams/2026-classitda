package com.classitda.feature.instructor.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
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
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteError
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteState
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiError
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiError
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.contract.isMemberRegistrationValid
import com.classitda.feature.instructor.mypage.contract.memberRegistrationFieldErrors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toUiModel() =
    InstructorMyPageUiModel(
        name,
        maskProfilePhoneNumber(phoneNumber),
        profileImageUrl,
        name.firstOrNull()?.toString() ?: "?",
    )

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toProfileUiModel() =
    MemberProfileUiModel(name, maskProfilePhoneNumber(phoneNumber), email, profileImageUrl)

internal fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toEditingState() =
    ProfileEditUiState.Editing(toProfileUiModel(), phoneNumber, name, false)

internal fun ManagedFacility.toDraft() =
    FacilityRegistrationDraft(
        images = images,
        name = name,
        address = address,
        detailAddress = detailAddress,
        phoneNumber = phoneNumber,
        description = description,
        openingTime = openingTime,
        closingTime = closingTime,
    )

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

internal fun InstructorMyPageFailureReason.toFacilityError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityManagementUiError.NETWORK
        else -> FacilityManagementUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toFacilityRegistrationError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityRegistrationUiError.NETWORK
        InstructorMyPageFailureReason.CONFLICT -> FacilityRegistrationUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> FacilityRegistrationUiError.INVALID_REQUEST
        else -> FacilityRegistrationUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toFacilityDetailError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityDetailUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> FacilityDetailUiError.NOT_FOUND
        else -> FacilityDetailUiError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toFacilityDeleteError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityDeleteError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> FacilityDeleteError.NOT_FOUND
        else -> FacilityDeleteError.UNKNOWN
    }

internal fun InstructorMyPageFailureReason.toFacilityEditError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityEditUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> FacilityEditUiError.NOT_FOUND
        InstructorMyPageFailureReason.INVALID_REQUEST -> FacilityEditUiError.INVALID_REQUEST
        else -> FacilityEditUiError.UNKNOWN
    }
