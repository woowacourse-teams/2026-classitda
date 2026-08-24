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
import classitda.shared.generated.resources.instructor_facility_registration_harness_five_images
import classitda.shared.generated.resources.instructor_facility_registration_harness_last_event
import classitda.shared.generated.resources.instructor_facility_registration_harness_no_event
import classitda.shared.generated.resources.instructor_facility_registration_harness_submitting
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.FacilityImageDraft
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.feature.instructor.mypage.FacilityRegistrationScreen
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationField
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FacilityRegistrationInteractionHarness(modifier: Modifier = Modifier) {
    val emptyDraft = FacilityRegistrationDraft()
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
                        uiState = editingState(uiState.draftOrEmpty().copy(address = action.address))
                    }

                    is FacilityRegistrationAction.DetailAddressChanged -> {
                        lastEvent = "DetailAddressChanged"
                        uiState = editingState(uiState.draftOrEmpty().copy(detailAddress = action.detailAddress))
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

                    FacilityRegistrationAction.RequestImages -> {
                        lastEvent = "RequestImages"
                    }

                    is FacilityRegistrationAction.ImagesSelected -> {
                        lastEvent = "ImagesSelected:${action.images.size}"
                        uiState =
                            editingState(
                                uiState.draftOrEmpty().copy(
                                    images = action.images.take(FacilityRegistrationDraft.MAX_IMAGE_COUNT),
                                ),
                            )
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
                                    detailAddress = action.detailAddress,
                                ),
                            )
                    }

                    FacilityRegistrationAction.Submit -> {
                        lastEvent = "Submit"
                        uiState = FacilityRegistrationUiState.Submitting
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
            TextButton(onClick = { uiState = editingState(fiveImageFacilityDraft) }) {
                Text(stringResource(Res.string.instructor_facility_registration_harness_five_images))
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
                            draft = FacilityRegistrationDraft(name = "", phoneNumber = "010"),
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

private fun FacilityRegistrationUiState.draftOrEmpty(): FacilityRegistrationDraft =
    when (this) {
        is FacilityRegistrationUiState.Editing -> draft
        is FacilityRegistrationUiState.Error -> draft
        else -> FacilityRegistrationDraft()
    }

private fun editingState(
    draft: FacilityRegistrationDraft,
    canSubmit: Boolean = draft.name.isNotBlank() && draft.address.isNotBlank() && draft.phoneNumber.isNotBlank(),
): FacilityRegistrationUiState = FacilityRegistrationUiState.Editing(draft = draft, canSubmit = canSubmit)

private val filledFacilityDraft =
    FacilityRegistrationDraft(
        name = "더 에이치 휘트니스 강남점",
        address = "서울 강남구 테헤란로 123",
        detailAddress = "2층",
        phoneNumber = "0212345678",
        description = "회원들이 편하게 운동할 수 있는 시설입니다.",
        openingTime = "09:00",
        closingTime = "22:00",
    )

private val fiveImageFacilityDraft =
    filledFacilityDraft.copy(
        images =
            (1..FacilityRegistrationDraft.MAX_IMAGE_COUNT).map {
                FacilityImageDraft("harness-image-$it", "harness-image-reference-$it")
            },
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
