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
import classitda.shared.generated.resources.instructor_studio_harness_content
import classitda.shared.generated.resources.instructor_studio_harness_empty
import classitda.shared.generated.resources.instructor_studio_harness_error
import classitda.shared.generated.resources.instructor_studio_harness_last_event
import classitda.shared.generated.resources.instructor_studio_harness_loading
import classitda.shared.generated.resources.instructor_studio_harness_no_event
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.feature.instructor.mypage.contract.StudioListUiModel
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiState
import com.classitda.feature.instructor.mypage.contract.StudioSuccessNotice
import com.classitda.feature.instructor.mypage.contract.StudioUiModel
import com.classitda.feature.instructor.mypage.studio.StudioManagementScreen
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StudioManagementInteractionHarness(modifier: Modifier = Modifier) {
    var uiState by remember {
        mutableStateOf<StudioManagementUiState>(
            StudioManagementUiState.Content(
                page = studioHarnessPage,
                successNotice = StudioSuccessNotice.Visible,
            ),
        )
    }
    var lastEvent by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        StudioManagementScreen(
            uiState = uiState,
            onAction = { action ->
                when (action) {
                    StudioManagementAction.Back -> {
                        lastEvent = "Back"
                    }

                    is StudioManagementAction.EditStudio -> {
                        lastEvent = "EditStudio:${action.studioId.value}"
                    }

                    is StudioManagementAction.OpenStudioDetail -> {
                        lastEvent = "OpenStudioDetail:${action.studioId.value}"
                    }

                    StudioManagementAction.OpenStudioRegistration -> {
                        lastEvent = "OpenStudioRegistration"
                    }

                    StudioManagementAction.Retry -> {
                        lastEvent = "Retry"
                        uiState = StudioManagementUiState.Content(studioHarnessPage)
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = StudioManagementUiState.Content(studioHarnessPage) }) {
                Text(stringResource(Res.string.instructor_studio_harness_content))
            }
            TextButton(onClick = { uiState = StudioManagementUiState.Loading }) {
                Text(stringResource(Res.string.instructor_studio_harness_loading))
            }
            TextButton(onClick = { uiState = StudioManagementUiState.Empty }) {
                Text(stringResource(Res.string.instructor_studio_harness_empty))
            }
            TextButton(
                onClick = {
                    uiState =
                        StudioManagementUiState.Error(
                            com.classitda.feature.instructor.mypage.contract.StudioManagementUiError.NETWORK,
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_studio_harness_error))
            }
        }
        Text(
            text =
                stringResource(
                    Res.string.instructor_studio_harness_last_event,
                    lastEvent ?: stringResource(Res.string.instructor_studio_harness_no_event),
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private val studioHarnessPage =
    StudioListUiModel(
        totalCount = 2,
        studios =
            listOf(
                StudioUiModel(
                    id = InstructorStudioId("studio-harness-1"),
                    name = "더 에이치 휘트니스 강남점",
                    address = StudioAddress(roadAddress = "서울 강남구 테헤란로 123"),
                ),
                StudioUiModel(
                    id = InstructorStudioId("studio-harness-2"),
                    name = "린 필라테스 스튜디오",
                    address = StudioAddress(roadAddress = "서울 강남구 압구정로 45"),
                ),
            ),
    )

@Preview(
    name = "Interaction harness · StudioManagement · Instructor",
    group = "Harness/StudioManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun StudioManagementInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementInteractionHarness()
    }
}
