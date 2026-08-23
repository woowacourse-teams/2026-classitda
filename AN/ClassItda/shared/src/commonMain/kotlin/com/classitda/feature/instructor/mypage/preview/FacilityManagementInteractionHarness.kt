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
import classitda.shared.generated.resources.instructor_facility_harness_content
import classitda.shared.generated.resources.instructor_facility_harness_empty
import classitda.shared.generated.resources.instructor_facility_harness_error
import classitda.shared.generated.resources.instructor_facility_harness_last_event
import classitda.shared.generated.resources.instructor_facility_harness_loading
import classitda.shared.generated.resources.instructor_facility_harness_no_event
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.feature.instructor.mypage.FacilityManagementScreen
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilitySuccessNotice
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FacilityManagementInteractionHarness(modifier: Modifier = Modifier) {
    var uiState by remember {
        mutableStateOf<FacilityManagementUiState>(
            FacilityManagementUiState.Content(
                page = facilityHarnessPage,
                successNotice = FacilitySuccessNotice.Visible,
            ),
        )
    }
    var lastEvent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        FacilityManagementScreen(
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    FacilityManagementAction.Back -> {
                        lastEvent = "Back"
                    }

                    is FacilityManagementAction.EditFacility -> {
                        lastEvent = "EditFacility:${action.facilityId.value}"
                    }

                    is FacilityManagementAction.OpenFacilityDetail -> {
                        lastEvent = "OpenFacilityDetail:${action.facilityId.value}"
                    }

                    FacilityManagementAction.OpenFacilityRegistration -> {
                        lastEvent = "OpenFacilityRegistration"
                    }

                    FacilityManagementAction.Retry -> {
                        lastEvent = "Retry"
                        uiState = FacilityManagementUiState.Content(facilityHarnessPage)
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = FacilityManagementUiState.Content(facilityHarnessPage) }) {
                Text(stringResource(Res.string.instructor_facility_harness_content))
            }
            TextButton(onClick = { uiState = FacilityManagementUiState.Loading }) {
                Text(stringResource(Res.string.instructor_facility_harness_loading))
            }
            TextButton(onClick = { uiState = FacilityManagementUiState.Empty }) {
                Text(stringResource(Res.string.instructor_facility_harness_empty))
            }
            TextButton(
                onClick = {
                    uiState =
                        FacilityManagementUiState.Error(
                            com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError.NETWORK,
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_facility_harness_error))
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.instructor_facility_harness_last_event,
                    lastEvent ?: stringResource(Res.string.instructor_facility_harness_no_event),
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private val facilityHarnessPage =
    FacilityList(
        totalCount = 2,
        facilities =
            listOf(
                ManagedFacility(
                    id = InstructorFacilityId("facility-harness-1"),
                    name = "더 에이치 휘트니스 강남점",
                    address = "서울 강남구 테헤란로 123",
                ),
                ManagedFacility(
                    id = InstructorFacilityId("facility-harness-2"),
                    name = "린 필라테스 스튜디오",
                    address = "서울 강남구 압구정로 45",
                ),
            ),
    )

@Preview(
    name = "Interaction harness · FacilityManagement · Instructor",
    group = "Harness/FacilityManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun FacilityManagementInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementInteractionHarness()
    }
}
