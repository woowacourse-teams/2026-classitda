package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId

sealed interface FacilityEditUiState {
    data object Loading : FacilityEditUiState

    data class Editing(
        val facilityId: InstructorFacilityId,
        val draft: FacilityInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<FacilityRegistrationField> = emptySet(),
        val imageError: FacilityImageUiError? = null,
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
        val isSubmitFailure: Boolean = false,
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

    data object RequestImageSource : FacilityEditAction

    data class ImageSelected(
        val image: FacilityImageInputUiModel,
    ) : FacilityEditAction

    data object RemoveImage : FacilityEditAction

    data class ImagePickerFailed(
        val reason: FacilityImageUiError,
    ) : FacilityEditAction

    data object RequestAddressSearch : FacilityEditAction

    data class AddressSelected(
        val address: FacilityAddress,
    ) : FacilityEditAction

    data object Submit : FacilityEditAction

    data class SuccessAcknowledged(
        val facilityId: InstructorFacilityId,
    ) : FacilityEditAction

    data object Retry : FacilityEditAction
}
