package com.classitda.feature.instructor.mypage.studio

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.feature.instructor.mypage.contract.StudioEditAction
import com.classitda.feature.instructor.mypage.contract.StudioEditUiError
import com.classitda.feature.instructor.mypage.contract.StudioEditUiState
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState

@Composable
fun StudioEditScreen(
    uiState: StudioEditUiState,
    onAction: (StudioEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    StudioRegistrationScreen(
        uiState = uiState.toRegistrationUiState(),
        onAction = { action -> onAction(action.toEditAction()) },
        modifier = modifier,
        isEditing = true,
        onSuccessAcknowledged = {
            val success = uiState as? StudioEditUiState.Success
            success?.let { onAction(StudioEditAction.SuccessAcknowledged(it.studioId)) }
        },
    )
}

private fun StudioEditUiState.toRegistrationUiState(): StudioRegistrationUiState =
    when (this) {
        StudioEditUiState.Loading -> {
            StudioRegistrationUiState.Loading
        }

        is StudioEditUiState.Editing -> {
            StudioRegistrationUiState.Editing(
                draft = draft,
                canSubmit = canSubmit,
                fieldErrors = fieldErrors,
                imageError = imageError,
            )
        }

        is StudioEditUiState.Submitting -> {
            StudioRegistrationUiState.Submitting
        }

        is StudioEditUiState.Success -> {
            StudioRegistrationUiState.Success
        }

        is StudioEditUiState.Error -> {
            StudioRegistrationUiState.Error(
                draft = draft,
                reason = reason.toRegistrationError(),
                completedOperations = completedOperations,
            )
        }
    }

private fun StudioRegistrationAction.toEditAction(): StudioEditAction =
    when (this) {
        StudioRegistrationAction.Back -> StudioEditAction.Back
        is StudioRegistrationAction.NameChanged -> StudioEditAction.NameChanged(name)
        is StudioRegistrationAction.AddressChanged -> StudioEditAction.AddressChanged(address)
        is StudioRegistrationAction.DetailAddressChanged -> StudioEditAction.DetailAddressChanged(detailAddress)
        is StudioRegistrationAction.PhoneNumberChanged -> StudioEditAction.PhoneNumberChanged(phoneNumber)
        is StudioRegistrationAction.OpeningTimeChanged -> StudioEditAction.OpeningTimeChanged(openingTime)
        is StudioRegistrationAction.ClosingTimeChanged -> StudioEditAction.ClosingTimeChanged(closingTime)
        is StudioRegistrationAction.DescriptionChanged -> StudioEditAction.DescriptionChanged(description)
        StudioRegistrationAction.RequestImageSource -> StudioEditAction.RequestImageSource
        is StudioRegistrationAction.ImageSelected -> StudioEditAction.ImageSelected(image)
        StudioRegistrationAction.RemoveImage -> StudioEditAction.RemoveImage
        is StudioRegistrationAction.ImagePickerFailed -> StudioEditAction.ImagePickerFailed(reason)
        StudioRegistrationAction.RequestAddressSearch -> StudioEditAction.RequestAddressSearch
        is StudioRegistrationAction.AddressSelected -> StudioEditAction.AddressSelected(address)
        StudioRegistrationAction.Submit -> StudioEditAction.Submit
        StudioRegistrationAction.Retry -> StudioEditAction.Retry
    }

private fun StudioEditUiError.toRegistrationError() =
    when (this) {
        StudioEditUiError.NETWORK -> StudioRegistrationUiError.NETWORK
        StudioEditUiError.FORBIDDEN -> StudioRegistrationUiError.FORBIDDEN
        StudioEditUiError.CONFLICT -> StudioRegistrationUiError.CONFLICT
        StudioEditUiError.INVALID_REQUEST -> StudioRegistrationUiError.INVALID_REQUEST
        StudioEditUiError.NOT_FOUND -> StudioRegistrationUiError.UNKNOWN
        StudioEditUiError.UNKNOWN -> StudioRegistrationUiError.UNKNOWN
    }

private val studioEditPreviewState =
    StudioEditUiState.Editing(
        studioId = InstructorStudioId("studio-edit-preview"),
        draft =
            StudioInputUiModel(
                name = "클래스잇다 스튜디오",
                address =
                    com.classitda.domain.model.instructor.mypage.StudioAddress(
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

@Preview(name = "Studio edit", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun StudioEditScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioEditScreen(studioEditPreviewState, onAction = {})
    }
}
