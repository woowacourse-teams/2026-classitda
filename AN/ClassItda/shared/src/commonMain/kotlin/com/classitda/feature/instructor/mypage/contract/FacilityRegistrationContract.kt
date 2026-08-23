package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityImageDraft
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId

sealed interface FacilityRegistrationUiState {
    data class Editing(
        val draft: FacilityRegistrationDraft,
        val canSubmit: Boolean,
        val fieldErrors: Set<FacilityRegistrationField> = emptySet(),
    ) : FacilityRegistrationUiState

    data object Submitting : FacilityRegistrationUiState

    data class Success(
        val facilityId: InstructorFacilityId,
    ) : FacilityRegistrationUiState

    data class Error(
        val draft: FacilityRegistrationDraft,
        val reason: FacilityRegistrationUiError,
    ) : FacilityRegistrationUiState
}

enum class FacilityRegistrationField {
    NAME,
    ADDRESS,
    DETAIL_ADDRESS,
    PHONE_NUMBER,
    DESCRIPTION,
    IMAGES,
}

enum class FacilityRegistrationUiError {
    NETWORK,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface FacilityRegistrationAction {
    data object Back : FacilityRegistrationAction

    data class NameChanged(
        val name: String,
    ) : FacilityRegistrationAction

    data class AddressChanged(
        val address: String,
    ) : FacilityRegistrationAction

    data class DetailAddressChanged(
        val detailAddress: String,
    ) : FacilityRegistrationAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : FacilityRegistrationAction

    data class DescriptionChanged(
        val description: String,
    ) : FacilityRegistrationAction

    data object RequestImages : FacilityRegistrationAction

    data class ImagesSelected(
        val images: List<FacilityImageDraft>,
    ) : FacilityRegistrationAction

    data object RequestAddressSearch : FacilityRegistrationAction

    data class AddressSelected(
        val address: String,
        val detailAddress: String = "",
    ) : FacilityRegistrationAction

    data object Submit : FacilityRegistrationAction

    data object Retry : FacilityRegistrationAction
}
