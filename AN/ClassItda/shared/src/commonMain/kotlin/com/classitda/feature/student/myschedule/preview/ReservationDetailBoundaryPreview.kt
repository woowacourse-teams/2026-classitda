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
import com.classitda.feature.student.myschedule.ReservationCancellationCompletedScreen
import com.classitda.feature.student.myschedule.ReservationDetailScreen
import com.classitda.feature.student.myschedule.component.common.MyScheduleTextButton
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailContent
import com.classitda.feature.student.myschedule.contract.ReservationCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.ReservationDetailAction
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiState

@Preview(
    name = "R5 long content · Student · Large font",
    group = "Screen/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun ReservationDetailBoundaryPreview_LongContent_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailBoundaryScreen(model = ReservationDetailPreviewFixture.Boundary.confirmed)
    }
}

@Preview(
    name = "R5 cancelled prohibited content · Student · Large font",
    group = "Screen/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun ReservationDetailBoundaryPreview_CancelledProhibited_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailBoundaryScreen(model = ReservationDetailPreviewFixture.Boundary.cancelled)
    }
}

@Preview(
    name = "R5 confirmed footer reached · Student · Large font",
    group = "Screen/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 520,
    fontScale = 1.5f,
)
@Composable
private fun ReservationDetailBoundaryPreview_ConfirmedFooterReached_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailContent(
            model = ReservationDetailPreviewFixture.Boundary.confirmed,
            onCancelReservation = {},
            listState = rememberLazyListState(initialFirstVisibleItemIndex = 5),
        )
    }
}

@Preview(
    name = "R5 submitting disabled · Student · Large font",
    group = "Screen/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun ReservationDetailBoundaryPreview_SubmittingDisabled_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailScreen(
            state =
                ReservationDetailUiState.Content(
                    detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                    cancellationDialog = ReservationCancellationDialogUiState.Submitting,
                ),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "R5 F05 result actions · Student · Large font",
    group = "Screen/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun ReservationDetailBoundaryPreview_F05ResultActions_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationCompletedScreen(
            result = ReservationDetailPreviewFixture.Boundary.cancellationCompleted,
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "R5 four states action harness · Student",
    group = "Harness/MySchedule/ReservationBoundary",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1100,
)
@Composable
private fun ReservationDetailBoundaryPreview_FourStatesActionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var selectedState by remember { mutableStateOf(ReservationBoundaryState.CONFIRMED) }
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }
        val model = selectedState.model()

        Column(modifier = Modifier.fillMaxSize()) {
            ReservationDetailScreen(
                state = ReservationDetailUiState.Content(detail = model),
                onAction = { action -> lastEvent = action.toBoundaryPreviewDescription() },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                onBookAnotherClass = {},
                onReturnToList = {},
                modifier = Modifier.weight(1f),
            )
            ReservationBoundaryStateSelector(
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
private fun ReservationBoundaryStateSelector(onSelect: (ReservationBoundaryState) -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ReservationBoundaryStateRow(
            first = ReservationBoundaryState.CONFIRMED,
            second = ReservationBoundaryState.CANCELLED,
            onSelect = onSelect,
        )
        ReservationBoundaryStateRow(
            first = ReservationBoundaryState.ATTENDED,
            second = ReservationBoundaryState.ABSENT,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun ReservationBoundaryStateRow(
    first: ReservationBoundaryState,
    second: ReservationBoundaryState,
    onSelect: (ReservationBoundaryState) -> Unit,
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

@Composable
private fun ReservationDetailBoundaryScreen(model: ReservationDetailUiModel) {
    ReservationDetailScreen(
        state = ReservationDetailUiState.Content(detail = model),
        onAction = {},
        onBack = {},
        onBookAnotherClass = {},
        onReturnToList = {},
    )
}

private enum class ReservationBoundaryState {
    CONFIRMED,
    CANCELLED,
    ATTENDED,
    ABSENT,
}

private fun ReservationBoundaryState.model(): ReservationDetailUiModel =
    when (this) {
        ReservationBoundaryState.CONFIRMED -> ReservationDetailPreviewFixture.Boundary.confirmed
        ReservationBoundaryState.CANCELLED -> ReservationDetailPreviewFixture.Boundary.cancelled
        ReservationBoundaryState.ATTENDED -> ReservationDetailPreviewFixture.Boundary.attended
        ReservationBoundaryState.ABSENT -> ReservationDetailPreviewFixture.Boundary.absent
    }

private fun ReservationDetailAction.toBoundaryPreviewDescription(): String =
    when (this) {
        ReservationDetailAction.Retry -> {
            "마지막 Action/ID: Retry"
        }

        is ReservationDetailAction.CancelReservation -> {
            "마지막 Action/ID: CancelReservation/${reservationId.value}"
        }

        ReservationDetailAction.DismissCancellation -> {
            "마지막 Action/ID: DismissCancellation"
        }

        is ReservationDetailAction.ConfirmCancellation -> {
            "마지막 Action/ID: ConfirmCancellation/${reservationId.value}"
        }

        is ReservationDetailAction.RetryCancellation -> {
            "마지막 Action/ID: RetryCancellation/${reservationId.value}"
        }
    }
