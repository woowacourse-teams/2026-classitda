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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_member_management_harness_back
import classitda.shared.generated.resources.instructor_member_management_harness_content
import classitda.shared.generated.resources.instructor_member_management_harness_empty
import classitda.shared.generated.resources.instructor_member_management_harness_error
import classitda.shared.generated.resources.instructor_member_management_harness_last_action
import classitda.shared.generated.resources.instructor_member_management_harness_loading
import classitda.shared.generated.resources.instructor_member_management_harness_no_action
import classitda.shared.generated.resources.instructor_member_management_harness_open_member
import classitda.shared.generated.resources.instructor_member_management_harness_open_registration
import classitda.shared.generated.resources.instructor_member_management_harness_query_changed
import classitda.shared.generated.resources.instructor_member_management_harness_retry
import classitda.shared.generated.resources.instructor_member_management_harness_search_empty
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.member.MemberManagementScreen
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemberManagementInteractionHarness(modifier: Modifier = Modifier) {
    val actions = remember { mutableStateListOf<MemberManagementAction>() }
    val page =
        MemberListPage(
            totalCount = 128,
            members =
                listOf(
                    ManagedMember(
                        id = InstructorMemberId("member-1"),
                        name = "김민지",
                        phoneNumber = "01012345678",
                    ),
                ),
        )
    var uiState by remember { mutableStateOf<MemberManagementUiState>(MemberManagementUiState.Content(page)) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MemberManagementScreen(
            uiState = uiState,
            onAction = { action ->
                actions += action
                if (action == MemberManagementAction.Retry) {
                    uiState = MemberManagementUiState.Content(page)
                }
                if (action is MemberManagementAction.SortOrderChanged) {
                    uiState =
                        when (val state = uiState) {
                            is MemberManagementUiState.Content -> state.copy(sortOrder = action.sortOrder)
                            is MemberManagementUiState.Empty -> state.copy(sortOrder = action.sortOrder)
                            is MemberManagementUiState.SearchEmpty -> state.copy(sortOrder = action.sortOrder)
                            else -> state
                        }
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = MemberManagementUiState.Content(page) }) {
                Text(stringResource(Res.string.instructor_member_management_harness_content))
            }
            TextButton(onClick = { uiState = MemberManagementUiState.Loading }) {
                Text(stringResource(Res.string.instructor_member_management_harness_loading))
            }
            TextButton(onClick = { uiState = MemberManagementUiState.Empty() }) {
                Text(stringResource(Res.string.instructor_member_management_harness_empty))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = MemberManagementUiState.SearchEmpty("없는 회원") }) {
                Text(stringResource(Res.string.instructor_member_management_harness_search_empty))
            }
            TextButton(onClick = { uiState = MemberManagementUiState.Error(MemberManagementUiError.NETWORK) }) {
                Text(stringResource(Res.string.instructor_member_management_harness_error))
            }
        }
        val lastActionText =
            when (val action = actions.lastOrNull()) {
                null -> {
                    stringResource(Res.string.instructor_member_management_harness_no_action)
                }

                MemberManagementAction.Back -> {
                    stringResource(Res.string.instructor_member_management_harness_back)
                }

                is MemberManagementAction.QueryChanged -> {
                    stringResource(
                        Res.string.instructor_member_management_harness_query_changed,
                        action.query,
                    )
                }

                is MemberManagementAction.OpenMember -> {
                    stringResource(
                        Res.string.instructor_member_management_harness_open_member,
                        action.memberId.value,
                    )
                }

                MemberManagementAction.OpenMemberRegistration -> {
                    stringResource(Res.string.instructor_member_management_harness_open_registration)
                }

                MemberManagementAction.Retry -> {
                    stringResource(Res.string.instructor_member_management_harness_retry)
                }

                is MemberManagementAction.SortOrderChanged -> {
                    action.sortOrder.name
                }
            }
        Text(
            text = stringResource(Res.string.instructor_member_management_harness_last_action, lastActionText),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

@Preview(
    name = "Interaction harness · Instructor",
    group = "Screen/MemberManagement",
)
@Composable
private fun MemberManagementInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementInteractionHarness()
    }
}
