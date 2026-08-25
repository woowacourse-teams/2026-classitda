package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.FacilityAddress

sealed interface FacilityRegistrationUiState {
    data object Loading : FacilityRegistrationUiState

    data class Editing(
        val draft: FacilityInputUiModel,
        val canSubmit: Boolean,
        val fieldErrors: Set<FacilityRegistrationField> = emptySet(),
    ) : FacilityRegistrationUiState

    data object Submitting : FacilityRegistrationUiState

    data object Success : FacilityRegistrationUiState

    data class Error(
        val draft: FacilityInputUiModel,
        val reason: FacilityRegistrationUiError,
    ) : FacilityRegistrationUiState
}

enum class FacilityRegistrationField {
    NAME,
    ADDRESS,
    DETAIL_ADDRESS,
    PHONE_NUMBER,
    OPENING_TIME,
    CLOSING_TIME,
    DESCRIPTION,
    IMAGE,
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

    data class OpeningTimeChanged(
        val openingTime: String,
    ) : FacilityRegistrationAction

    data class ClosingTimeChanged(
        val closingTime: String,
    ) : FacilityRegistrationAction

    data class DescriptionChanged(
        val description: String,
    ) : FacilityRegistrationAction

    data object RequestImageSource : FacilityRegistrationAction

    data class ImageSelected(
        val image: FacilityImageInputUiModel,
    ) : FacilityRegistrationAction

    data object RemoveImage : FacilityRegistrationAction

    data object RequestAddressSearch : FacilityRegistrationAction

    data class AddressSelected(
        val address: FacilityAddress,
    ) : FacilityRegistrationAction

    data object Submit : FacilityRegistrationAction

    data object Retry : FacilityRegistrationAction
}

internal fun facilityRegistrationFieldErrors(draft: FacilityInputUiModel): Set<FacilityRegistrationField> =
    buildSet {
        if (draft.name.isBlank()) add(FacilityRegistrationField.NAME)
        if (!draft.address.hasBaseAddress) add(FacilityRegistrationField.ADDRESS)
        if (!isFacilityPhoneNumberValid(draft.phoneNumber)) add(FacilityRegistrationField.PHONE_NUMBER)
        if (draft.openingTime.isNotBlank() && !isFacilityTimeValid(draft.openingTime)) {
            add(FacilityRegistrationField.OPENING_TIME)
        }
        if (draft.closingTime.isNotBlank() && !isFacilityTimeValid(draft.closingTime)) {
            add(FacilityRegistrationField.CLOSING_TIME)
        }
    }

internal fun FacilityInputUiModel.isFacilityRegistrationValid(): Boolean =
    facilityRegistrationFieldErrors(this).isEmpty()

internal fun isFacilityPhoneNumberValid(value: String): Boolean {
    val digits = value.filter(Char::isDigit)
    return value.isNotBlank() &&
        value.all { it.isDigit() || it == '-' || it == ' ' } &&
        digits.length in 9..11
}

private val facilityTimePattern = Regex("""(?:[01]\d|2[0-3]):[0-5]\d""")

internal fun isFacilityTimeValid(value: String): Boolean = facilityTimePattern.matches(value)
