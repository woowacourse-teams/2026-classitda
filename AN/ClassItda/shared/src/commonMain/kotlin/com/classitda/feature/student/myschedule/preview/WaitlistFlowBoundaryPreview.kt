package com.classitda.feature.student.myschedule.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.WaitlistCancelledScreen
import com.classitda.feature.student.myschedule.WaitlistDetailScreen
import com.classitda.feature.student.myschedule.component.common.MyScheduleTextButton
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailContent
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailAction
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiState

@Preview(
    name = "W4 F06 long content / Student / Large font",
    group = "Screen/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun WaitlistFlowBoundaryPreview_F06LongContent_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state = WaitlistDetailUiState.Content(WaitlistDetailPreviewFixture.Boundary.pending),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "W4 F06 footer reached / Student / Large font",
    group = "Screen/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 520,
    fontScale = 1.5f,
)
@Composable
private fun WaitlistFlowBoundaryPreview_F06FooterReached_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailContent(
            model = WaitlistDetailPreviewFixture.Boundary.pending,
            onCancelWaitlist = {},
            listState = rememberLazyListState(initialFirstVisibleItemIndex = 5),
        )
    }
}

@Preview(
    name = "W4 F07 submitting disabled / Student / Large font",
    group = "Screen/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun WaitlistFlowBoundaryPreview_F07SubmittingDisabled_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state =
                WaitlistDetailUiState.Content(
                    detail = WaitlistDetailPreviewFixture.Boundary.pending,
                    cancellationDialog = WaitlistCancellationDialogUiState.Submitting,
                ),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "W4 F07 failed dismiss / Student / Large font",
    group = "Screen/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun WaitlistFlowBoundaryPreview_F07FailedDismiss_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state =
                WaitlistDetailUiState.Content(
                    detail = WaitlistDetailPreviewFixture.Boundary.pending,
                    cancellationDialog =
                        WaitlistCancellationDialogUiState.Failed(
                            error = WaitlistCancellationErrorUiModel.NETWORK,
                        ),
                ),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "W4 F08 result actions / Student / Large font",
    group = "Screen/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun WaitlistFlowBoundaryPreview_F08ResultActions_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistCancelledScreen(
            result = WaitlistDetailPreviewFixture.Boundary.completed,
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "W4 flow state action harness / Student",
    group = "Harness/MySchedule/WaitlistBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1100,
)
@Composable
private fun WaitlistFlowBoundaryPreview_StateActionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var selectedState by remember { mutableStateOf(WaitlistBoundaryState.DETAIL) }
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }

        Column(modifier = Modifier.fillMaxSize()) {
            WaitlistDetailScreen(
                state = selectedState.uiState(),
                onAction = { action ->
                    lastEvent = action.toBoundaryPreviewDescription()
                    selectedState = action.nextBoundaryState(selectedState)
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                onBookAnotherClass = {
                    lastEvent = "마지막 Callback: onBookAnotherClass"
                },
                onReturnToList = { lastEvent = "마지막 Callback: onReturnToList" },
                modifier = Modifier.weight(1f),
            )
            WaitlistBoundaryStateSelector(
                onSelect = { state ->
                    selectedState = state
                    lastEvent = "선택 상태: ${state.name} / Action: 없음"
                },
            )
            Text(
                text = "선택 상태: ${selectedState.name} / $lastEvent",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(StuColors.SurfaceVariant)
                        .padding(AppSpacing.md),
                style = appTypography().bodySmall,
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun WaitlistBoundaryStateSelector(onSelect: (WaitlistBoundaryState) -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        WaitlistBoundaryStateRow(
            first = WaitlistBoundaryState.DETAIL,
            second = WaitlistBoundaryState.WAITING,
            onSelect = onSelect,
        )
        WaitlistBoundaryStateRow(
            first = WaitlistBoundaryState.FAILED,
            second = WaitlistBoundaryState.COMPLETED,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun WaitlistBoundaryStateRow(
    first: WaitlistBoundaryState,
    second: WaitlistBoundaryState,
    onSelect: (WaitlistBoundaryState) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        MyScheduleTextButton(
            text = first.name,
            onClick = { onSelect(first) },
            modifier = Modifier.weight(1f),
        )
        MyScheduleTextButton(
            text = second.name,
            onClick = { onSelect(second) },
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class WaitlistBoundaryState {
    DETAIL,
    WAITING,
    FAILED,
    SUBMITTING,
    COMPLETED,
}

private fun WaitlistBoundaryState.uiState(): WaitlistDetailUiState =
    when (this) {
        WaitlistBoundaryState.DETAIL -> {
            WaitlistDetailUiState.Content(WaitlistDetailPreviewFixture.Boundary.pending)
        }

        WaitlistBoundaryState.WAITING -> {
            WaitlistDetailUiState.Content(
                detail = WaitlistDetailPreviewFixture.Boundary.pending,
                cancellationDialog = WaitlistCancellationDialogUiState.Waiting,
            )
        }

        WaitlistBoundaryState.FAILED -> {
            WaitlistDetailUiState.Content(
                detail = WaitlistDetailPreviewFixture.Boundary.pending,
                cancellationDialog =
                    WaitlistCancellationDialogUiState.Failed(
                        error = WaitlistCancellationErrorUiModel.NETWORK,
                    ),
            )
        }

        WaitlistBoundaryState.SUBMITTING -> {
            WaitlistDetailUiState.Content(
                detail = WaitlistDetailPreviewFixture.Boundary.pending,
                cancellationDialog = WaitlistCancellationDialogUiState.Submitting,
            )
        }

        WaitlistBoundaryState.COMPLETED -> {
            WaitlistDetailUiState.CancellationCompleted(
                result = WaitlistDetailPreviewFixture.Boundary.completed,
            )
        }
    }

private fun WaitlistDetailAction.nextBoundaryState(current: WaitlistBoundaryState): WaitlistBoundaryState =
    when (this) {
        WaitlistDetailAction.Retry -> current
        is WaitlistDetailAction.CancelWaitlist -> WaitlistBoundaryState.WAITING
        is WaitlistDetailAction.ApproveWaitlist -> current
        WaitlistDetailAction.DismissCancellation -> WaitlistBoundaryState.DETAIL
        is WaitlistDetailAction.ConfirmCancellation -> WaitlistBoundaryState.SUBMITTING
        is WaitlistDetailAction.RetryCancellation -> WaitlistBoundaryState.SUBMITTING
    }

private fun WaitlistDetailAction.toBoundaryPreviewDescription(): String =
    when (this) {
        WaitlistDetailAction.Retry -> {
            "마지막 Action/ID: Retry"
        }

        is WaitlistDetailAction.CancelWaitlist -> {
            "마지막 Action/ID: CancelWaitlist/${waitlistId.value}"
        }

        is WaitlistDetailAction.ApproveWaitlist -> {
            "마지막 Action/ID: ApproveWaitlist/${waitlistId.value}"
        }

        WaitlistDetailAction.DismissCancellation -> {
            "마지막 Action/ID: DismissCancellation"
        }

        is WaitlistDetailAction.ConfirmCancellation -> {
            "마지막 Action/ID: ConfirmCancellation/${waitlistId.value}"
        }

        is WaitlistDetailAction.RetryCancellation -> {
            "마지막 Action/ID: RetryCancellation/${waitlistId.value}"
        }
    }
