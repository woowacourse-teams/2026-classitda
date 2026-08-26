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
import classitda.shared.generated.resources.instructor_studio_registration_harness_empty
import classitda.shared.generated.resources.instructor_studio_registration_harness_errors
import classitda.shared.generated.resources.instructor_studio_registration_harness_failed
import classitda.shared.generated.resources.instructor_studio_registration_harness_filled
import classitda.shared.generated.resources.instructor_studio_registration_harness_last_event
import classitda.shared.generated.resources.instructor_studio_registration_harness_no_event
import classitda.shared.generated.resources.instructor_studio_registration_harness_submitting
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationField
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.isStudioRegistrationValid
import com.classitda.feature.instructor.mypage.contract.studioRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.studio.StudioRegistrationScreen
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StudioRegistrationInteractionHarness(modifier: Modifier = Modifier) {
    val emptyDraft = StudioInputUiModel()
    var uiState by remember { mutableStateOf(editingState(emptyDraft, canSubmit = false)) }
    var lastEvent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        StudioRegistrationScreen(
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    StudioRegistrationAction.Back -> {
                        lastEvent = "Back"
                    }

                    is StudioRegistrationAction.NameChanged -> {
                        lastEvent = "NameChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(name = action.name))
                    }

                    is StudioRegistrationAction.AddressChanged -> {
                        lastEvent = "AddressChanged"
                        val address = uiState.draftOrEmpty().address.copy(roadAddress = action.address)
                        uiState = editingState(uiState.draftOrEmpty().copy(address = address))
                    }

                    is StudioRegistrationAction.DetailAddressChanged -> {
                        lastEvent = "DetailAddressChanged"
                        val address = uiState.draftOrEmpty().address.copy(detailAddress = action.detailAddress)
                        uiState = editingState(uiState.draftOrEmpty().copy(address = address))
                    }

                    is StudioRegistrationAction.PhoneNumberChanged -> {
                        lastEvent = "PhoneNumberChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(phoneNumber = action.phoneNumber))
                    }

                    is StudioRegistrationAction.OpeningTimeChanged -> {
                        lastEvent = "OpeningTimeChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(openingTime = action.openingTime))
                    }

                    is StudioRegistrationAction.ClosingTimeChanged -> {
                        lastEvent = "ClosingTimeChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(closingTime = action.closingTime))
                    }

                    is StudioRegistrationAction.DescriptionChanged -> {
                        lastEvent = "DescriptionChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(description = action.description))
                    }

                    StudioRegistrationAction.RequestImageSource -> {
                        lastEvent = "RequestImageSource"
                    }

                    is StudioRegistrationAction.ImageSelected -> {
                        lastEvent = "ImageSelected"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    image = action.image,
                                ),
                            )
                    }

                    StudioRegistrationAction.RemoveImage -> {
                        lastEvent = "RemoveImage"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    image = null,
                                ),
                            )
                    }

                    is StudioRegistrationAction.ImagePickerFailed -> {
                        lastEvent = "ImagePickerFailed"
                    }

                    StudioRegistrationAction.RequestAddressSearch -> {
                        lastEvent = "RequestAddressSearch"
                    }

                    is StudioRegistrationAction.AddressSelected -> {
                        lastEvent = "AddressSelected"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    address = action.address,
                                ),
                            )
                    }

                    StudioRegistrationAction.Submit -> {
                        lastEvent = "Submit"
                        val draft = uiState.draftOrEmpty()
                        val fieldErrors = studioRegistrationFieldErrors(draft)
                        uiState =
                            if (fieldErrors.isEmpty()) {
                                StudioRegistrationUiState.Submitting
                            } else {
                                StudioRegistrationUiState.Editing(
                                    draft = draft,
                                    canSubmit = false,
                                    fieldErrors = fieldErrors,
                                )
                            }
                    }

                    StudioRegistrationAction.Retry -> {
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
                Text(stringResource(Res.string.instructor_studio_registration_harness_empty))
            }
            TextButton(onClick = { uiState = editingState(filledStudioDraft) }) {
                Text(stringResource(Res.string.instructor_studio_registration_harness_filled))
            }
            TextButton(onClick = { uiState = editingState(singleImageStudioDraft) }) {
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
                        StudioRegistrationUiState.Editing(
                            draft = StudioInputUiModel(name = "", phoneNumber = "010"),
                            canSubmit = false,
                            fieldErrors = setOf(StudioRegistrationField.NAME, StudioRegistrationField.PHONE_NUMBER),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_studio_registration_harness_errors))
            }
            TextButton(onClick = { uiState = StudioRegistrationUiState.Submitting }) {
                Text(stringResource(Res.string.instructor_studio_registration_harness_submitting))
            }
            TextButton(
                onClick = {
                    uiState =
                        StudioRegistrationUiState.Error(filledStudioDraft, StudioRegistrationUiError.NETWORK)
                },
            ) {
                Text(stringResource(Res.string.instructor_studio_registration_harness_failed))
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.instructor_studio_registration_harness_last_event,
                    lastEvent ?: stringResource(Res.string.instructor_studio_registration_harness_no_event),
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private fun StudioRegistrationUiState.draftOrEmpty(): StudioInputUiModel =
    when (this) {
        is StudioRegistrationUiState.Editing -> draft
        is StudioRegistrationUiState.Error -> draft
        else -> StudioInputUiModel()
    }

private fun editingState(
    draft: StudioInputUiModel,
    canSubmit: Boolean = draft.isStudioRegistrationValid(),
): StudioRegistrationUiState = StudioRegistrationUiState.Editing(draft = draft, canSubmit = canSubmit)

private val filledStudioDraft =
    StudioInputUiModel(
        name = "더 에이치 휘트니스 강남점",
        address = StudioAddress(roadAddress = "서울 강남구 테헤란로 123", detailAddress = "2층"),
        phoneNumber = "0212345678",
        description = "회원들이 편하게 운동할 수 있는 시설입니다.",
        openingTime = "09:00",
        closingTime = "22:00",
    )

private val singleImageStudioDraft =
    filledStudioDraft.copy(
        image =
            StudioImageInputUiModel(
                StudioImageSelection.Local(
                    handle = "harness-image-handle",
                    previewReference = "harness-image-reference",
                    mimeType = "image/jpeg",
                    fileName = "studio.jpg",
                    sizeBytes = 1024,
                ),
            ),
    )

@Preview(
    name = "Interaction harness · StudioRegistration · Instructor",
    group = "Harness/StudioRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun StudioRegistrationInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationInteractionHarness()
    }
}
