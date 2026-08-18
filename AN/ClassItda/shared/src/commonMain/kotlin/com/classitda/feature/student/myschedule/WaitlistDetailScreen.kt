package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
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
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistCancellationConfirmDialog
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailContent
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleLoadingContent
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailAction
import com.classitda.feature.student.myschedule.contract.WaitlistDetailErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiState
import com.classitda.feature.student.myschedule.contract.cancellationActionOrNull
import com.classitda.feature.student.myschedule.preview.WaitlistCancellationResultPreviewFixture
import com.classitda.feature.student.myschedule.preview.WaitlistDetailPreviewFixture
import org.jetbrains.compose.resources.stringResource

@Composable
fun WaitlistDetailScreen(
    state: WaitlistDetailUiState,
    onAction: (WaitlistDetailAction) -> Unit,
    onBack: () -> Unit,
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is WaitlistDetailUiState.CancellationCompleted) {
        WaitlistCancelledScreen(
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
            WaitlistDetailTopBar(onBack = onBack)
            when (state) {
                WaitlistDetailUiState.Loading -> {
                    MyScheduleLoadingContent(modifier = Modifier.weight(1f))
                }

                is WaitlistDetailUiState.Content -> {
                    val cancellationAction = state.detail.cancellationActionOrNull()

                    WaitlistDetailContent(
                        model = state.detail,
                        onCancelWaitlist =
                            cancellationAction?.let { action ->
                                { onAction(action) }
                            },
                        modifier = Modifier.weight(1f),
                    )
                }

                is WaitlistDetailUiState.CancellationCompleted -> {}

                is WaitlistDetailUiState.Error -> {
                    WaitlistDetailErrorContent(
                        onRetry = { onAction(WaitlistDetailAction.Retry) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        val contentState = state as? WaitlistDetailUiState.Content
        val dialogState = contentState?.cancellationDialog
        if (contentState != null && dialogState != null) {
            WaitlistCancellationConfirmDialog(
                position = contentState.detail.currentPosition,
                state = dialogState,
                onConfirm = {
                    onAction(
                        WaitlistDetailAction.ConfirmCancellation(contentState.detail.waitlistId),
                    )
                },
                onRetry = {
                    onAction(
                        WaitlistDetailAction.RetryCancellation(contentState.detail.waitlistId),
                    )
                },
                onDismiss = { onAction(WaitlistDetailAction.DismissCancellation) },
            )
        }
    }
}

@Composable
private fun WaitlistDetailErrorContent(
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
    name = "F06 pending / Student / Default",
    group = "Screen/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1031,
)
@Composable
private fun WaitlistDetailScreenPreview_F06Pending_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state = WaitlistDetailUiState.Content(WaitlistDetailPreviewFixture.pending),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "F06 cancellation action harness / Student",
    group = "Harness/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 900,
)
@Composable
private fun WaitlistDetailScreenPreview_F06CancellationActionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }

        Column(modifier = Modifier.fillMaxSize()) {
            WaitlistDetailScreen(
                state = WaitlistDetailUiState.Content(WaitlistDetailPreviewFixture.pending),
                onAction = { action ->
                    lastEvent =
                        when (action) {
                            WaitlistDetailAction.Retry -> {
                                "마지막 Action/ID: Retry"
                            }

                            is WaitlistDetailAction.CancelWaitlist -> {
                                "마지막 Action/ID: CancelWaitlist/${action.waitlistId.value}"
                            }

                            WaitlistDetailAction.DismissCancellation -> {
                                "마지막 Action/ID: DismissCancellation"
                            }

                            is WaitlistDetailAction.ConfirmCancellation -> {
                                "마지막 Action/ID: ConfirmCancellation/${action.waitlistId.value}"
                            }

                            is WaitlistDetailAction.RetryCancellation -> {
                                "마지막 Action/ID: RetryCancellation/${action.waitlistId.value}"
                            }
                        }
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                onBookAnotherClass = {},
                onReturnToList = {},
                modifier = Modifier.height(840.dp),
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StuColors.SurfaceVariant,
            ) {
                Text(
                    text = lastEvent,
                    modifier = Modifier.padding(AppSpacing.md),
                    style = appTypography().bodySmall,
                    color = StuColors.TextPrimary,
                )
            }
        }
    }
}

@Preview(
    name = "F07 modal action harness / Student",
    group = "Harness/MySchedule/WaitlistCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 1000,
)
@Composable
private fun WaitlistDetailScreenPreview_F07ModalActionHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var lastEvent by remember { mutableStateOf("마지막 Action/ID: 없음") }
        var screenState by
            remember {
                mutableStateOf<WaitlistDetailUiState>(
                    WaitlistDetailUiState.Content(
                        detail = WaitlistDetailPreviewFixture.pending,
                        cancellationDialog = WaitlistCancellationDialogUiState.Waiting,
                    ),
                )
            }

        Column(modifier = Modifier.fillMaxSize()) {
            WaitlistDetailScreen(
                state = screenState,
                onAction = { action ->
                    lastEvent =
                        when (action) {
                            WaitlistDetailAction.Retry -> {
                                "마지막 Action/ID: Retry"
                            }

                            is WaitlistDetailAction.CancelWaitlist -> {
                                "마지막 Action/ID: CancelWaitlist/${action.waitlistId.value}"
                            }

                            WaitlistDetailAction.DismissCancellation -> {
                                screenState =
                                    WaitlistDetailUiState.Content(
                                        detail = WaitlistDetailPreviewFixture.pending,
                                    )
                                "마지막 Action/ID: DismissCancellation"
                            }

                            is WaitlistDetailAction.ConfirmCancellation -> {
                                screenState =
                                    WaitlistDetailUiState.Content(
                                        detail = WaitlistDetailPreviewFixture.pending,
                                        cancellationDialog = WaitlistCancellationDialogUiState.Submitting,
                                    )
                                "마지막 Action/ID: ConfirmCancellation/${action.waitlistId.value}"
                            }

                            is WaitlistDetailAction.RetryCancellation -> {
                                screenState =
                                    WaitlistDetailUiState.Content(
                                        detail = WaitlistDetailPreviewFixture.pending,
                                        cancellationDialog = WaitlistCancellationDialogUiState.Submitting,
                                    )
                                "마지막 Action/ID: RetryCancellation/${action.waitlistId.value}"
                            }
                        }
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
                onBookAnotherClass = {},
                onReturnToList = {},
                modifier = Modifier.weight(1f),
            )
            if ((screenState as? WaitlistDetailUiState.Content)?.cancellationDialog == null) {
                MyScheduleSecondaryButton(
                    text = "실패 상태 열기",
                    onClick = {
                        screenState =
                            WaitlistDetailUiState.Content(
                                detail = WaitlistDetailPreviewFixture.pending,
                                cancellationDialog =
                                    WaitlistCancellationDialogUiState.Failed(
                                        error = WaitlistCancellationErrorUiModel.NETWORK,
                                    ),
                            )
                    },
                    modifier = Modifier.padding(horizontal = AppSpacing.md),
                )
                MyScheduleSecondaryButton(
                    text = "성공 결과 fixture 열기",
                    onClick = {
                        screenState =
                            WaitlistDetailUiState.CancellationCompleted(
                                result = WaitlistCancellationResultPreviewFixture.completed,
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

@Preview(
    name = "Loading / Student / Default",
    group = "Screen/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistDetailScreenPreview_Loading_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state = WaitlistDetailUiState.Loading,
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "Error / Student / Default",
    group = "Screen/MySchedule/WaitlistDetail",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistDetailScreenPreview_Error_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            state = WaitlistDetailUiState.Error(WaitlistDetailErrorUiModel.NETWORK),
            onAction = {},
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}
