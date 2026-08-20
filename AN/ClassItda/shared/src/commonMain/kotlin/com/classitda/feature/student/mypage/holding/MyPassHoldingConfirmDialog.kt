package com.classitda.feature.student.mypage.holding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.PrimaryButton

@Composable
fun MyPassHoldingConfirmDialog(
    description: String,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = !isSubmitting, dismissOnClickOutside = !isSubmitting),
    ) {
        MyPassHoldingConfirmDialogContent(
            description = description,
            isSubmitting = isSubmitting,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun MyPassHoldingConfirmDialogContent(
    description: String,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.sectionGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
    ) {
        Text(
            text = "수강권을 홀딩할까요?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                enabled = !isSubmitting,
                shape = AppShape.Card,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = StuColors.SurfaceVariant,
                        contentColor = StuColors.TextPrimary,
                    ),
            ) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
            PrimaryButton(
                text = if (isSubmitting) "처리 중" else "확인",
                onClick = onConfirm,
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun MyPassHoldingConfirmDialogPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassHoldingConfirmDialogContent(
            description = "2026년 8월 10일부터 8월 23일까지\n수강권 이용이 중지됩니다.",
            isSubmitting = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
