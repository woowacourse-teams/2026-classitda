package com.classitda.feature.instructor.mypage.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_facility_registration_harness_empty
import classitda.shared.generated.resources.instructor_facility_registration_harness_errors
import classitda.shared.generated.resources.instructor_facility_registration_harness_failed
import classitda.shared.generated.resources.instructor_facility_registration_harness_filled
import classitda.shared.generated.resources.instructor_facility_registration_harness_last_event
import classitda.shared.generated.resources.instructor_facility_registration_harness_no_event
import classitda.shared.generated.resources.instructor_facility_registration_harness_submitting
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.feature.instructor.mypage.contract.FacilityImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationField
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.facility.FacilityRegistrationScreen
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FacilityRegistrationInteractionHarness(modifier: Modifier = Modifier) {
    val emptyDraft = FacilityInputUiModel()
    var uiState by remember { mutableStateOf(editingState(emptyDraft, canSubmit = false)) }
    var lastEvent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        FacilityRegistrationScreen(
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    FacilityRegistrationAction.Back -> {
                        lastEvent = "Back"
                    }

                    is FacilityRegistrationAction.NameChanged -> {
                        lastEvent = "NameChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(name = action.name))
                    }

                    is FacilityRegistrationAction.AddressChanged -> {
                        lastEvent = "AddressChanged"
                        val address = uiState.draftOrEmpty().address.copy(roadAddress = action.address)
                        uiState = editingState(uiState.draftOrEmpty().copy(address = address))
                    }

                    is FacilityRegistrationAction.DetailAddressChanged -> {
                        lastEvent = "DetailAddressChanged"
                        val address = uiState.draftOrEmpty().address.copy(detailAddress = action.detailAddress)
                        uiState = editingState(uiState.draftOrEmpty().copy(address = address))
                    }

                    is FacilityRegistrationAction.PhoneNumberChanged -> {
                        lastEvent = "PhoneNumberChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(phoneNumber = action.phoneNumber))
                    }

                    is FacilityRegistrationAction.OpeningTimeChanged -> {
                        lastEvent = "OpeningTimeChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(openingTime = action.openingTime))
                    }

                    is FacilityRegistrationAction.ClosingTimeChanged -> {
                        lastEvent = "ClosingTimeChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(closingTime = action.closingTime))
                    }

                    is FacilityRegistrationAction.DescriptionChanged -> {
                        lastEvent = "DescriptionChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(description = action.description))
                    }

                    FacilityRegistrationAction.RequestImageSource -> {
                        lastEvent = "RequestImageSource"
                    }

                    is FacilityRegistrationAction.ImageSelected -> {
                        lastEvent = "ImageSelected"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    image = action.image,
                                ),
                            )
                    }

                    FacilityRegistrationAction.RemoveImage -> {
                        lastEvent = "RemoveImage"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    image = null,
                                ),
                            )
                    }

                    is FacilityRegistrationAction.ImagePickerFailed -> {
                        lastEvent = "ImagePickerFailed"
                    }

                    FacilityRegistrationAction.RequestAddressSearch -> {
                        lastEvent = "RequestAddressSearch"
                    }

                    is FacilityRegistrationAction.AddressSelected -> {
                        lastEvent = "AddressSelected"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    address = action.address,
                                ),
                            )
                    }

                    FacilityRegistrationAction.Submit -> {
                        lastEvent = "Submit"
                        val draft = uiState.draftOrEmpty()
                        val fieldErrors = facilityRegistrationFieldErrors(draft)
                        uiState =
                            if (fieldErrors.isEmpty()) {
                                FacilityRegistrationUiState.Submitting
                            } else {
                                FacilityRegistrationUiState.Editing(
                                    draft = draft,
                                    canSubmit = false,
                                    fieldErrors = fieldErrors,
                                )
                            }
                    }

                    FacilityRegistrationAction.Retry -> {
                        lastEvent = "Retry"
                        uiState = editingState(uiState.draftOrEmpty())
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = editingState(emptyDraft, canSubmit = false) }) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_empty))
            }
            TextButton(onClick = { uiState = editingState(filledFacilityDraft) }) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_filled))
            }
            TextButton(onClick = { uiState = editingState(singleImageFacilityDraft) }) {
                Text("단일 이미지")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(
                onClick = {
                    uiState =
                        FacilityRegistrationUiState.Editing(
                            draft = FacilityInputUiModel(name = "", phoneNumber = "010"),
                            canSubmit = false,
                            fieldErrors = setOf(FacilityRegistrationField.NAME, FacilityRegistrationField.PHONE_NUMBER),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_errors))
            }
            TextButton(onClick = { uiState = FacilityRegistrationUiState.Submitting }) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_submitting))
            }
            TextButton(
                onClick = {
                    uiState =
                        FacilityRegistrationUiState.Error(filledFacilityDraft, FacilityRegistrationUiError.NETWORK)
                },
            ) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_failed))
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.instructor_facility_registration_harness_last_event,
                    lastEvent ?: stringResource(Res.string.instructor_facility_registration_harness_no_event),
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private fun FacilityRegistrationUiState.draftOrEmpty(): FacilityInputUiModel =
    when (this) {
        is FacilityRegistrationUiState.Editing -> draft
        is FacilityRegistrationUiState.Error -> draft
        else -> FacilityInputUiModel()
    }

private fun editingState(
    draft: FacilityInputUiModel,
    canSubmit: Boolean = draft.isFacilityRegistrationValid(),
): FacilityRegistrationUiState = FacilityRegistrationUiState.Editing(draft = draft, canSubmit = canSubmit)

private val filledFacilityDraft =
    FacilityInputUiModel(
        name = "더 에이치 휘트니스 강남점",
        address = FacilityAddress(roadAddress = "서울 강남구 테헤란로 123", detailAddress = "2층"),
        phoneNumber = "0212345678",
        description = "회원들이 편하게 운동할 수 있는 시설입니다.",
        openingTime = "09:00",
        closingTime = "22:00",
    )

private val singleImageFacilityDraft =
    filledFacilityDraft.copy(
        image =
            FacilityImageInputUiModel(
                FacilityImageSelection.Local(
                    handle = "harness-image-handle",
                    previewReference = "harness-image-reference",
                    mimeType = "image/jpeg",
                    fileName = "facility.jpg",
                    sizeBytes = 1024,
                ),
            ),
    )

@Preview(
    name = "Interaction harness · FacilityRegistration · Instructor",
    group = "Harness/FacilityRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun FacilityRegistrationInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityRegistrationInteractionHarness()
    }
}
