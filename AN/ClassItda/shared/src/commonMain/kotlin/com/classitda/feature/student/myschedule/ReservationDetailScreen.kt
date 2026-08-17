package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_load_error_description
import classitda.shared.generated.resources.my_schedule_load_error_title
import classitda.shared.generated.resources.my_schedule_retry
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MySchedulePrimaryButton
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailContent
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleLoadingContent
import com.classitda.feature.student.myschedule.contract.ReservationDetailAction
import com.classitda.feature.student.myschedule.contract.ReservationDetailErrorUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiState
import com.classitda.feature.student.myschedule.contract.cancellationActionOrNull
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReservationDetailScreen(
    state: ReservationDetailUiState,
    onAction: (ReservationDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        ReservationDetailTopBar(onBack = onBack)
        when (state) {
            ReservationDetailUiState.Loading -> {
                MyScheduleLoadingContent(modifier = Modifier.weight(1f))
            }

            is ReservationDetailUiState.Content -> {
                val cancellationAction = state.detail.cancellationActionOrNull()

                ReservationDetailContent(
                    model = state.detail,
                    onCancelReservation =
                        cancellationAction?.let { action ->
                            { onAction(action) }
                        },
                    modifier = Modifier.weight(1f),
                )
            }

            is ReservationDetailUiState.Error -> {
                ReservationDetailErrorContent(
                    onRetry = { onAction(ReservationDetailAction.Retry) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReservationDetailErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_load_error_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.my_schedule_load_error_description),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        MySchedulePrimaryButton(
            text = stringResource(Res.string.my_schedule_retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = AppSpacing.sectionGap),
        )
    }
}

@Preview(
    name = "Confirmed · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1031,
)
@Composable
private fun ReservationDetailScreenPreview_Confirmed_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.confirmed)
    }
}

@Preview(
    name = "Cancelled · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 900,
)
@Composable
private fun ReservationDetailScreenPreview_Cancelled_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.cancelled)
    }
}

@Preview(
    name = "Attended · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 907,
)
@Composable
private fun ReservationDetailScreenPreview_Attended_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.attended)
    }
}

@Preview(
    name = "Absent · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 942,
)
@Composable
private fun ReservationDetailScreenPreview_Absent_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.absent)
    }
}

@Preview(
    name = "Loading · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationDetailScreenPreview_Loading_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailScreen(
            state = ReservationDetailUiState.Loading,
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(
    name = "Error · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationDetailScreenPreview_Error_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailScreen(
            state =
                ReservationDetailUiState.Error(
                    error = ReservationDetailErrorUiModel.NETWORK,
                ),
            onAction = {},
            onBack = {},
        )
    }
}

@Preview(
    name = "Interaction harness · Student",
    group = "Harness/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 900,
)
@Composable
private fun ReservationDetailScreenPreview_InteractionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }

        Column(modifier = Modifier.fillMaxSize()) {
            ReservationDetailScreen(
                state =
                    ReservationDetailUiState.Content(
                        detail = ReservationDetailPreviewFixture.confirmed,
                    ),
                onAction = { action ->
                    lastEvent =
                        when (action) {
                            ReservationDetailAction.Retry -> {
                                "마지막 Action/ID: Retry"
                            }

                            is ReservationDetailAction.CancelReservation -> {
                                "마지막 Action/ID: CancelReservation/${action.reservationId.value}"
                            }
                        }
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = lastEvent,
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
private fun ReservationDetailPreview(model: ReservationDetailUiModel) {
    ReservationDetailScreen(
        state = ReservationDetailUiState.Content(detail = model),
        onAction = {},
        onBack = {},
    )
}
