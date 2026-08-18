package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailContent
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleLoadingContent
import com.classitda.feature.student.myschedule.contract.WaitlistDetailAction
import com.classitda.feature.student.myschedule.contract.WaitlistDetailErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiState
import com.classitda.feature.student.myschedule.contract.cancellationActionOrNull
import com.classitda.feature.student.myschedule.preview.WaitlistDetailPreviewFixture
import org.jetbrains.compose.resources.stringResource

@Composable
fun WaitlistDetailScreen(
    state: WaitlistDetailUiState,
    onAction: (WaitlistDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
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

            is WaitlistDetailUiState.Error -> {
                WaitlistDetailErrorContent(
                    onRetry = { onAction(WaitlistDetailAction.Retry) },
                    modifier = Modifier.weight(1f),
                )
            }
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
                        }
                },
                onBack = { lastEvent = "마지막 Action/ID: Back" },
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
        )
    }
}
