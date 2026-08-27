package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.classitda.feature.student.myschedule.component.common.MyScheduleSecondaryButton
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationCancellationConfirmDialog
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailContent
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleLoadingContent
import com.classitda.feature.student.myschedule.contract.ReservationCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.ReservationCancellationErrorUiModel
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
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is ReservationDetailUiState.CancellationCompleted) {
        ReservationCancellationCompletedScreen(
            result = state.result,
            onBack = onBack,
            onBookAnotherClass = onBookAnotherClass,
            onReturnToList = onReturnToList,
            modifier = modifier,
        )
        return
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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

                is ReservationDetailUiState.CancellationCompleted -> {}

                is ReservationDetailUiState.Error -> {
                    ReservationDetailErrorContent(
                        onRetry = { onAction(ReservationDetailAction.Retry) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        val contentState = state as? ReservationDetailUiState.Content
        val confirmed = contentState?.detail as? ReservationDetailUiModel.Confirmed
        val dialogState = contentState?.cancellationDialog
        if (confirmed != null && dialogState != null) {
            ReservationCancellationConfirmDialog(
                reservation = confirmed,
                state = dialogState,
                onConfirm = {
                    onAction(ReservationDetailAction.ConfirmCancellation(confirmed.reservationId))
                },
                onRetry = {
                    onAction(ReservationDetailAction.RetryCancellation(confirmed.reservationId))
                },
                onDismiss = { onAction(ReservationDetailAction.DismissCancellation) },
            )
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
    name = "F04 waiting · Student · Default",
    group = "Screen/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1031,
)
@Composable
private fun ReservationDetailScreenPreview_CancellationWaiting_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationDialogPreview(
            dialogState = ReservationCancellationDialogUiState.Waiting,
        )
    }
}

@Preview(
    name = "F04 submitting · Student · Default",
    group = "Screen/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1031,
)
@Composable
private fun ReservationDetailScreenPreview_CancellationSubmitting_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationDialogPreview(
            dialogState = ReservationCancellationDialogUiState.Submitting,
        )
    }
}

@Preview(
    name = "F04 failed · Student · Default",
    group = "Screen/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1031,
)
@Composable
private fun ReservationDetailScreenPreview_CancellationFailed_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationDialogPreview(
            dialogState =
                ReservationCancellationDialogUiState.Failed(
                    error = ReservationCancellationErrorUiModel.NETWORK,
                ),
        )
    }
}

@Preview(
    name = "F05 cancellation completed · Student",
    group = "Screen/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationDetailScreenPreview_CancellationCompleted_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailScreen(
            state =
                ReservationDetailUiState.CancellationCompleted(
                    result = ReservationDetailPreviewFixture.cancellationCompleted,
                ),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
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
    name = "Class cancelled · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 907,
)
@Composable
private fun ReservationDetailScreenPreview_ClassCancelled_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.classCancelled)
    }
}

@Preview(
    name = "F09 attended · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 907,
)
@Composable
private fun ReservationDetailScreenPreview_F09Attended_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailPreview(model = ReservationDetailPreviewFixture.attended)
    }
}

@Preview(
    name = "F10 absent · Student · Default",
    group = "Screen/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 942,
)
@Composable
private fun ReservationDetailScreenPreview_F10Absent_Student_Default() {
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
            onBookAnotherClass = {},
            onReturnToList = {},
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
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "R5 modal action harness · Student",
    group = "Harness/MySchedule/ReservationDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 900,
)
@Composable
private fun ReservationDetailScreenPreview_R5ModalActionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }
        var screenState by
            remember {
                mutableStateOf<ReservationDetailUiState>(
                    ReservationDetailUiState.Content(
                        detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                    ),
                )
            }

        Column(modifier = Modifier.fillMaxSize()) {
            ReservationDetailScreen(
                state = screenState,
                onAction = { action ->
                    lastEvent =
                        when (action) {
                            ReservationDetailAction.Retry -> {
                                "마지막 Action/ID: Retry"
                            }

                            is ReservationDetailAction.CancelReservation -> {
                                screenState =
                                    ReservationDetailUiState.Content(
                                        detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                                        cancellationDialog =
                                            ReservationCancellationDialogUiState.Waiting,
                                    )
                                "마지막 Action/ID: CancelReservation/${action.reservationId.value}"
                            }

                            ReservationDetailAction.DismissCancellation -> {
                                screenState =
                                    ReservationDetailUiState.Content(
                                        detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                                    )
                                "마지막 Action/ID: DismissCancellation"
                            }

                            is ReservationDetailAction.ConfirmCancellation -> {
                                screenState =
                                    ReservationDetailUiState.Content(
                                        detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                                        cancellationDialog =
                                            ReservationCancellationDialogUiState.Submitting,
                                    )
                                "마지막 Action/ID: ConfirmCancellation/${action.reservationId.value}"
                            }

                            is ReservationDetailAction.RetryCancellation -> {
                                screenState =
                                    ReservationDetailUiState.Content(
                                        detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                                        cancellationDialog =
                                            ReservationCancellationDialogUiState.Submitting,
                                    )
                                "마지막 Action/ID: RetryCancellation/${action.reservationId.value}"
                            }
                        }
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                onBookAnotherClass = {},
                onReturnToList = {},
                modifier = Modifier.weight(1f),
            )
            if ((screenState as? ReservationDetailUiState.Content)?.cancellationDialog == null) {
                MyScheduleSecondaryButton(
                    text = "실패 상태 열기",
                    onClick = {
                        screenState =
                            ReservationDetailUiState.Content(
                                detail = ReservationDetailPreviewFixture.Boundary.confirmed,
                                cancellationDialog =
                                    ReservationCancellationDialogUiState.Failed(
                                        error = ReservationCancellationErrorUiModel.NETWORK,
                                    ),
                            )
                    },
                    modifier = Modifier.padding(horizontal = AppSpacing.md),
                )
                MyScheduleSecondaryButton(
                    text = "성공 결과 fixture 열기",
                    onClick = {
                        screenState =
                            ReservationDetailUiState.CancellationCompleted(
                                result = ReservationDetailPreviewFixture.Boundary.cancellationCompleted,
                            )
                        lastEvent = "상태: CancellationCompleted / modal: 미노출"
                    },
                    modifier = Modifier.padding(horizontal = AppSpacing.md),
                )
            }
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
        onBookAnotherClass = {},
        onReturnToList = {},
    )
}

@Composable
private fun ReservationCancellationDialogPreview(dialogState: ReservationCancellationDialogUiState) {
    ReservationDetailScreen(
        state =
            ReservationDetailUiState.Content(
                detail = ReservationDetailPreviewFixture.confirmed,
                cancellationDialog = dialogState,
            ),
        onAction = {},
        onBack = {},
        onBookAnotherClass = {},
        onReturnToList = {},
    )
}
