package com.classitda.feature.instructor.mypage.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_my_page_harness_last_action
import classitda.shared.generated.resources.instructor_my_page_harness_no_action
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.mypage.InstructorMyPageScreen
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
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

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Content(profile),
            onAction = { actions += it },
            modifier = Modifier.weight(1f),
        )
        Text(
            text =
                stringResource(
                    Res.string.instructor_my_page_harness_last_action,
                    actions.lastOrNull()?.toPreviewLabel()
                        ?: stringResource(Res.string.instructor_my_page_harness_no_action),
                ),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun InstructorMyPageAction.toPreviewLabel(): String =
    when (this) {
        InstructorMyPageAction.OpenProfile -> "OpenProfile"
        InstructorMyPageAction.OpenMemberManagement -> "OpenMemberManagement"
        InstructorMyPageAction.OpenFacilityManagement -> "OpenFacilityManagement"
        InstructorMyPageAction.OpenPrivacyPolicy -> "OpenPrivacyPolicy"
        InstructorMyPageAction.Retry -> "Retry"
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
