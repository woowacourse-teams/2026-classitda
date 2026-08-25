package com.classitda.feature.instructor.mypage.facility

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiError
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState

@Composable
fun FacilityEditScreen(
    uiState: FacilityEditUiState,
    onAction: (FacilityEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    FacilityRegistrationScreen(
        uiState = uiState.toRegistrationUiState(),
        onAction = { action -> onAction(action.toEditAction()) },
        modifier = modifier,
        isEditing = true,
        onSuccessAcknowledged = {
            val success = uiState as? FacilityEditUiState.Success
            success?.let { onAction(FacilityEditAction.SuccessAcknowledged(it.facilityId)) }
        },
    )
}

private fun FacilityEditUiState.toRegistrationUiState(): FacilityRegistrationUiState =
    when (this) {
        FacilityEditUiState.Loading -> {
            FacilityRegistrationUiState.Loading
        }

        is FacilityEditUiState.Editing -> {
            FacilityRegistrationUiState.Editing(
                draft = draft,
                canSubmit = canSubmit,
                fieldErrors = fieldErrors,
            )
        }

        is FacilityEditUiState.Submitting -> {
            FacilityRegistrationUiState.Submitting
        }

        is FacilityEditUiState.Success -> {
            FacilityRegistrationUiState.Success
        }

        is FacilityEditUiState.Error -> {
            FacilityRegistrationUiState.Error(
                draft = draft,
                reason = reason.toRegistrationError(),
            )
        }
    }

private fun FacilityRegistrationAction.toEditAction(): FacilityEditAction =
    when (this) {
        FacilityRegistrationAction.Back -> FacilityEditAction.Back
        is FacilityRegistrationAction.NameChanged -> FacilityEditAction.NameChanged(name)
        is FacilityRegistrationAction.AddressChanged -> FacilityEditAction.AddressChanged(address)
        is FacilityRegistrationAction.DetailAddressChanged -> FacilityEditAction.DetailAddressChanged(detailAddress)
        is FacilityRegistrationAction.PhoneNumberChanged -> FacilityEditAction.PhoneNumberChanged(phoneNumber)
        is FacilityRegistrationAction.OpeningTimeChanged -> FacilityEditAction.OpeningTimeChanged(openingTime)
        is FacilityRegistrationAction.ClosingTimeChanged -> FacilityEditAction.ClosingTimeChanged(closingTime)
        is FacilityRegistrationAction.DescriptionChanged -> FacilityEditAction.DescriptionChanged(description)
        FacilityRegistrationAction.RequestImageSource -> FacilityEditAction.RequestImageSource
        is FacilityRegistrationAction.ImageSelected -> FacilityEditAction.ImageSelected(image)
        FacilityRegistrationAction.RemoveImage -> FacilityEditAction.RemoveImage
        FacilityRegistrationAction.RequestAddressSearch -> FacilityEditAction.RequestAddressSearch
        is FacilityRegistrationAction.AddressSelected -> FacilityEditAction.AddressSelected(address)
        FacilityRegistrationAction.Submit -> FacilityEditAction.Submit
        FacilityRegistrationAction.Retry -> FacilityEditAction.Retry
    }

private fun FacilityEditUiError.toRegistrationError() =
    when (this) {
        FacilityEditUiError.NETWORK -> FacilityRegistrationUiError.NETWORK
        FacilityEditUiError.INVALID_REQUEST -> FacilityRegistrationUiError.INVALID_REQUEST
        FacilityEditUiError.NOT_FOUND -> FacilityRegistrationUiError.UNKNOWN
        FacilityEditUiError.UNKNOWN -> FacilityRegistrationUiError.UNKNOWN
    }

private val facilityEditPreviewState =
    FacilityEditUiState.Editing(
        facilityId = InstructorFacilityId("facility-edit-preview"),
        draft =
            FacilityInputUiModel(
                name = "클래스잇다 스튜디오",
                address =
                    com.classitda.domain.model.instructor.mypage.FacilityAddress(
                        roadAddress = "서울특별시 강남구 테헤란로",
                        detailAddress = "5층 501호",
                    ),
                phoneNumber = "0212345678",
                description = "회원들이 편하게 운동할 수 있는 시설입니다.",
                openingTime = "09:00",
                closingTime = "22:00",
            ),
        canSubmit = true,
    )

@Preview(name = "Facility edit", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FacilityEditScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityEditScreen(facilityEditPreviewState, onAction = {})
    }
}
