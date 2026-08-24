package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun ClassSessionEditExitDialog(
    onDismissRequest: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = InsColors.White,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = "작성 중인 내용이 있습니다.",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = "지금까지 작성한 내용이 사라집니다. 나가시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("취소", color = InsColors.TextSecondary)
                    }
                    Button(
                        onClick = onLeaveClick,
                        modifier = Modifier.weight(1f),
                        shape = AppShape.Card,
                        colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
                    ) {
                        Text("나가기")
                    }
                }
            }
        }
    }
}

@Composable
internal fun ClassSessionCapacityChangeDialog(onConfirmClick: () -> Unit) {
    Dialog(onDismissRequest = onConfirmClick) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = InsColors.White,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = "정원을 변경할 수 없습니다.",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = "이미 예약한 회원 수보다 작은 정원으로 변경할 수 없습니다. 정원을 다시 확인해 주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShape.Card,
                    colors = ButtonDefaults.buttonColors(containerColor = InsColors.Red),
                ) {
                    Text("확인")
                }
            }
        }
    }
}

@Composable
@Preview(name = "정원 변경 제한", showBackground = true)
private fun ClassSessionEditDialogsPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionCapacityChangeDialog(onConfirmClick = {})
    }
}
