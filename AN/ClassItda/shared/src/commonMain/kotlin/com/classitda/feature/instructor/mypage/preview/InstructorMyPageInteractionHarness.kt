package com.classitda.feature.instructor.mypage.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_my_page_harness_action_open_facilities
import classitda.shared.generated.resources.instructor_my_page_harness_action_open_members
import classitda.shared.generated.resources.instructor_my_page_harness_action_open_privacy
import classitda.shared.generated.resources.instructor_my_page_harness_action_open_profile
import classitda.shared.generated.resources.instructor_my_page_harness_action_retry
import classitda.shared.generated.resources.instructor_my_page_harness_content
import classitda.shared.generated.resources.instructor_my_page_harness_error
import classitda.shared.generated.resources.instructor_my_page_harness_last_action
import classitda.shared.generated.resources.instructor_my_page_harness_loading
import classitda.shared.generated.resources.instructor_my_page_harness_no_action
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.instructor.mypage.InstructorMyPageScreen
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InstructorMyPageInteractionHarness(modifier: Modifier = Modifier) {
    val actions = remember { mutableStateListOf<InstructorMyPageAction>() }
    val profile =
        InstructorMyPageUiModel(
            name = "이지은",
            phoneNumberLabel = "010-****-5678",
            profileImageUrl = null,
            avatarFallback = "이",
        )
    var uiState by remember { mutableStateOf<InstructorMyPageUiState>(InstructorMyPageUiState.Content(profile)) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        InstructorMyPageScreen(
            uiState = uiState,
            onAction = {
                actions += it
                if (it == InstructorMyPageAction.Retry) {
                    uiState = InstructorMyPageUiState.Content(profile)
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            TextButton(onClick = { uiState = InstructorMyPageUiState.Content(profile) }) {
                Text(stringResource(Res.string.instructor_my_page_harness_content))
            }
            TextButton(onClick = { uiState = InstructorMyPageUiState.Loading }) {
                Text(stringResource(Res.string.instructor_my_page_harness_loading))
            }
            TextButton(onClick = { uiState = InstructorMyPageUiState.Error(InstructorMyPageUiError.NETWORK) }) {
                Text(stringResource(Res.string.instructor_my_page_harness_error))
            }
        }
        val lastActionResource = actions.lastOrNull()?.toPreviewLabel()
        val lastActionText =
            if (lastActionResource == null) {
                stringResource(Res.string.instructor_my_page_harness_no_action)
            } else {
                stringResource(lastActionResource)
            }
        Text(
            text =
                stringResource(
                    Res.string.instructor_my_page_harness_last_action,
                    lastActionText,
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private fun InstructorMyPageAction.toPreviewLabel(): StringResource =
    when (this) {
        InstructorMyPageAction.OpenProfile -> Res.string.instructor_my_page_harness_action_open_profile
        InstructorMyPageAction.OpenMemberManagement -> Res.string.instructor_my_page_harness_action_open_members
        InstructorMyPageAction.OpenFacilityManagement -> Res.string.instructor_my_page_harness_action_open_facilities
        InstructorMyPageAction.OpenPrivacyPolicy -> Res.string.instructor_my_page_harness_action_open_privacy
        InstructorMyPageAction.Retry -> Res.string.instructor_my_page_harness_action_retry
    }

@Preview(
    name = "Interaction harness · Instructor",
    group = "Screen/InstructorMyPage",
)
@Composable
private fun InstructorMyPageInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageInteractionHarness()
    }
}
