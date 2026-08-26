package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.repository.instructor.mypage.StudioUpdateOperation

sealed interface StudioEditUiState {
    data object Loading : StudioEditUiState

    data class Editing(
        val studioId: InstructorStudioId,
        val draft: StudioInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<StudioRegistrationField> = emptySet(),
        val imageError: StudioImageUiError? = null,
    ) : StudioEditUiState

    data class Submitting(
        val studioId: InstructorStudioId,
        val draft: StudioInputUiModel,
    ) : StudioEditUiState

    data class Success(
        val studioId: InstructorStudioId,
    ) : StudioEditUiState

    data class Error(
        val studioId: InstructorStudioId,
        val draft: StudioInputUiModel,
        val reason: StudioEditUiError,
        val isSubmitFailure: Boolean = false,
        val completedOperations: Set<StudioUpdateOperation> = emptySet(),
    ) : StudioEditUiState
}

enum class StudioEditUiError {
    NETWORK,
    NOT_FOUND,
    FORBIDDEN,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface StudioEditAction {
    data object Back : StudioEditAction

    data class NameChanged(
        val name: String,
    ) : StudioEditAction

    data class AddressChanged(
        val address: String,
    ) : StudioEditAction

    data class DetailAddressChanged(
        val detailAddress: String,
    ) : StudioEditAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : StudioEditAction

    data class OpeningTimeChanged(
        val openingTime: String,
    ) : StudioEditAction

    data class ClosingTimeChanged(
        val closingTime: String,
    ) : StudioEditAction

    data class DescriptionChanged(
        val description: String,
    ) : StudioEditAction

    data object RequestImageSource : StudioEditAction

    data class ImageSelected(
        val image: StudioImageInputUiModel,
    ) : StudioEditAction

    data object RemoveImage : StudioEditAction

    data class ImagePickerFailed(
        val reason: StudioImageUiError,
    ) : StudioEditAction

    data object RequestAddressSearch : StudioEditAction

    data class AddressSelected(
        val address: StudioAddress,
    ) : StudioEditAction

    data object Submit : StudioEditAction

    data class SuccessAcknowledged(
        val studioId: InstructorStudioId,
    ) : StudioEditAction

    data object Retry : StudioEditAction
}
