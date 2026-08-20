package com.classitda.feature.student.mypage.holding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar
import classitda.shared.generated.resources.ic_confirmation_number
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingUiState
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPassHoldingRequestScreen(
    uiState: MyPassHoldingUiState,
    onNavigateBack: () -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onMemoChanged: (String) -> Unit,
    onCancelClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val dialogDescription = uiState.confirmDialogDescription
    if (dialogDescription != null) {
        MyPassHoldingConfirmDialog(
            description = dialogDescription,
            isSubmitting = uiState.isSubmitting,
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss,
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            NavigateBackTopBar(
                onNavigateBack = onNavigateBack,
                modifier = Modifier.background(StuColors.White),
                title = "홀딩 요청",
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(AppSpacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
            ) {
                MyPassHoldingSelectedPassCard(passName = uiState.passName)

                MyPassHoldingDateField(
                    label = "홀딩 시작일 (필수)",
                    dateLabel = uiState.startDateLabel,
                    onClick = onStartDateClick,
                )
                MyPassHoldingDateField(
                    label = "홀딩 종료일 (필수)",
                    dateLabel = uiState.endDateLabel,
                    onClick = onEndDateClick,
                )
                MyPassHoldingMemoField(
                    memo = uiState.memo,
                    onMemoChanged = onMemoChanged,
                )
                MyPassHoldingSummary(uiState = uiState)
            }
            MyPassHoldingActionButtons(
                isSubmitting = uiState.isSubmitting,
                onCancelClick = onCancelClick,
                onSubmitClick = onSubmitClick,
                modifier = Modifier.padding(AppSpacing.screenPadding),
            )
        }
    }
}

@Composable
private fun MyPassHoldingSelectedPassCard(
    passName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(AppShape.Card)
                    .background(StuColors.SurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_confirmation_number),
                contentDescription = null,
                tint = StuColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column {
            Text(
                text = "선택한 수강권",
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextSecondary,
            )
            Text(
                text = passName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun MyPassHoldingDateField(
    label: String,
    dateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(AppShape.Card)
                    .border(1.dp, StuColors.Divider, AppShape.Card)
                    .clickable(onClick = onClick)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = StuColors.TextPrimary,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = null,
                tint = StuColors.TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun MyPassHoldingMemoField(
    memo: String,
    onMemoChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = "메모하기 (선택)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .clip(AppShape.Card)
                    .border(1.dp, StuColors.Divider, AppShape.Card)
                    .padding(AppSpacing.lg),
        ) {
            if (memo.isEmpty()) {
                Text(
                    text = "메모를 입력해 주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StuColors.TextTertiary,
                )
            }
            BasicTextField(
                value = memo,
                onValueChange = onMemoChanged,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = StuColors.TextPrimary),
                cursorBrush = SolidColor(StuColors.TextPrimary),
            )
        }
    }
}

@Composable
private fun MyPassHoldingSummary(
    uiState: MyPassHoldingUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        MyPassHoldingSummaryRow(
            label = "총 홀딩 일수",
            value = uiState.totalHoldingDaysLabel,
            valueColor = StuColors.TextPrimary,
            emphasizeValue = true,
        )
        HorizontalDivider(color = StuColors.Divider)
        MyPassHoldingSummaryRow(
            label = "현재 만료일",
            value = uiState.currentExpireDateLabel,
            valueColor = StuColors.TextSecondary,
        )
        MyPassHoldingSummaryRow(
            label = "변경 예정 만료일",
            value = uiState.newExpireDateLabel,
            valueColor = StuColors.TextPrimary,
            emphasizeValue = true,
        )
    }
}

@Composable
private fun MyPassHoldingSummaryRow(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    emphasizeValue: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style =
                if (emphasizeValue) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                } else {
                    MaterialTheme.typography.bodyMedium
                },
            color = valueColor,
        )
    }
}

@Composable
private fun MyPassHoldingActionButtons(
    isSubmitting: Boolean,
    onCancelClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
            enabled = !isSubmitting,
            shape = AppShape.Card,
            border = BorderStroke(1.dp, StuColors.DividerStrong),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StuColors.TextPrimary),
        ) {
            Text(
                text = "취소",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        PrimaryButton(
            text = "홀딩 요청하기",
            onClick = onSubmitClick,
            enabled = !isSubmitting,
            modifier = Modifier.weight(2f),
        )
    }
}

private val previewUiState =
    MyPassHoldingUiState(
        passName = "리포머 20회권",
        startDateLabel = "2026.08.10",
        endDateLabel = "2026.08.23",
        memo = "",
        totalHoldingDaysLabel = "14일",
        currentExpireDateLabel = "2026.09.30",
        newExpireDateLabel = "2026.10.14",
    )

@Preview
@Composable
private fun MyPassHoldingRequestScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassHoldingRequestScreen(
            uiState = previewUiState,
            onNavigateBack = {},
            onStartDateClick = {},
            onEndDateClick = {},
            onMemoChanged = {},
            onCancelClick = {},
            onSubmitClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {},
        )
    }
}

@Preview
@Composable
private fun MyPassHoldingRequestScreenDialogPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassHoldingRequestScreen(
            uiState =
                previewUiState.copy(
                    confirmDialogDescription = "2026년 8월 10일부터 8월 23일까지\n수강권 이용이 중지됩니다.",
                ),
            onNavigateBack = {},
            onStartDateClick = {},
            onEndDateClick = {},
            onMemoChanged = {},
            onCancelClick = {},
            onSubmitClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {},
        )
    }
}
