package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.repository.instructor.mypage.StudioUpdateOperation

sealed interface StudioRegistrationUiState {
    data object Loading : StudioRegistrationUiState

    data class Editing(
        val draft: StudioInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<StudioRegistrationField> = emptySet(),
        val imageError: StudioImageUiError? = null,
    ) : StudioRegistrationUiState

    data object Submitting : StudioRegistrationUiState

    data object Success : StudioRegistrationUiState

    data class Error(
        val draft: StudioInputUiModel,
        val reason: StudioRegistrationUiError,
        val completedOperations: Set<StudioUpdateOperation> = emptySet(),
    ) : StudioRegistrationUiState
}

enum class StudioRegistrationField {
    NAME,
    ADDRESS,
    DETAIL_ADDRESS,
    PHONE_NUMBER,
    OPENING_TIME,
    CLOSING_TIME,
    DESCRIPTION,
    IMAGE,
}

enum class StudioRegistrationUiError {
    NETWORK,
    FORBIDDEN,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface StudioRegistrationAction {
    data object Back : StudioRegistrationAction

    data class NameChanged(
        val name: String,
    ) : StudioRegistrationAction

    data class AddressChanged(
        val address: String,
    ) : StudioRegistrationAction

    data class DetailAddressChanged(
        val detailAddress: String,
    ) : StudioRegistrationAction

    data class PhoneNumberChanged(
        val phoneNumber: String,
    ) : StudioRegistrationAction

    data class OpeningTimeChanged(
        val openingTime: String,
    ) : StudioRegistrationAction

    data class ClosingTimeChanged(
        val closingTime: String,
    ) : StudioRegistrationAction

    data class DescriptionChanged(
        val description: String,
    ) : StudioRegistrationAction

    data object RequestImageSource : StudioRegistrationAction

    data class ImageSelected(
        val image: StudioImageInputUiModel,
    ) : StudioRegistrationAction

    data object RemoveImage : StudioRegistrationAction

    data class ImagePickerFailed(
        val reason: StudioImageUiError,
    ) : StudioRegistrationAction

    data object RequestAddressSearch : StudioRegistrationAction

    data class AddressSelected(
        val address: StudioAddress,
    ) : StudioRegistrationAction

    data object Submit : StudioRegistrationAction

    data object Retry : StudioRegistrationAction
}

internal fun studioRegistrationFieldErrors(draft: StudioInputUiModel): Set<StudioRegistrationField> =
    buildSet {
        if (draft.name.isBlank()) add(StudioRegistrationField.NAME)
        if (!draft.address.hasBaseAddress) add(StudioRegistrationField.ADDRESS)
        if (!isStudioPhoneNumberValid(draft.phoneNumber)) add(StudioRegistrationField.PHONE_NUMBER)
        if (draft.openingTime.isNotBlank() && !isStudioTimeValid(draft.openingTime)) {
            add(StudioRegistrationField.OPENING_TIME)
        }
        if (draft.closingTime.isNotBlank() && !isStudioTimeValid(draft.closingTime)) {
            add(StudioRegistrationField.CLOSING_TIME)
        }
    }

internal fun StudioInputUiModel.isStudioRegistrationValid(): Boolean = studioRegistrationFieldErrors(this).isEmpty()

internal fun isStudioPhoneNumberValid(value: String): Boolean {
    val digits = value.filter(Char::isDigit)
    return value.isNotBlank() &&
        value.all { it.isDigit() || it == '-' || it == ' ' } &&
        digits.length in 9..11
}

private val studioTimePattern = Regex("""(?:[01]\d|2[0-3]):[0-5]\d""")

internal fun isStudioTimeValid(value: String): Boolean = studioTimePattern.matches(value)
