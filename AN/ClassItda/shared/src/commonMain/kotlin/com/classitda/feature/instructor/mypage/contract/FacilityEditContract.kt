package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorFacilityId

sealed interface FacilityEditUiState {
    data object Loading : FacilityEditUiState

    data class Editing(
        val facilityId: InstructorFacilityId,
        val draft: FacilityInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<FacilityRegistrationField> = emptySet(),
    ) : FacilityEditUiState

    data class Submitting(
        val facilityId: InstructorFacilityId,
        val draft: FacilityInputUiModel,
    ) : FacilityEditUiState

    data class Success(
        val facilityId: InstructorFacilityId,
    ) : FacilityEditUiState

    data class Error(
        val facilityId: InstructorFacilityId,
        val draft: FacilityInputUiModel,
        val reason: FacilityEditUiError,
    ) : FacilityEditUiState
}

enum class FacilityEditUiError {
    NETWORK,
    NOT_FOUND,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface FacilityEditAction {
    data object Back : FacilityEditAction

    data class NameChanged(
        val name: String,
    ) : FacilityEditAction

    data class AddressChanged(
        val address: String,
    ) : FacilityEditAction

    data class DetailAddressChanged(
        val detailAddress: String,
    ) : FacilityEditAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : FacilityEditAction

    data class OpeningTimeChanged(
        val openingTime: String,
    ) : FacilityEditAction

    data class ClosingTimeChanged(
        val closingTime: String,
    ) : FacilityEditAction

    data class DescriptionChanged(
        val description: String,
    ) : FacilityEditAction

    data object RequestImages : FacilityEditAction

    data class ImagesSelected(
        val images: List<FacilityImageInputUiModel>,
    ) : FacilityEditAction

    data object RequestAddressSearch : FacilityEditAction

    data class AddressSelected(
        val address: String,
        val detailAddress: String = "",
    ) : FacilityEditAction

    data object Submit : FacilityEditAction

    data class SuccessAcknowledged(
        val facilityId: InstructorFacilityId,
    ) : FacilityEditAction

    data object Retry : FacilityEditAction
}
