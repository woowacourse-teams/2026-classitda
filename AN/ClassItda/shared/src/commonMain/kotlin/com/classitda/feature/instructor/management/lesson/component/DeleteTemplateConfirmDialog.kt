package com.classitda.feature.instructor.management.lesson.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun DeleteTemplateConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = "템플릿을 삭제하시겠어요?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = "삭제하면 되돌릴 수 없어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextSecondary,
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = AppShape.Card,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = InsColors.Gray100,
                                contentColor = InsColors.TextSecondary,
                            ),
                    ) {
                        Text(text = "취소")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = AppShape.Card,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = InsColors.Red,
                                contentColor = InsColors.White,
                            ),
                    ) {
                        Text(text = "삭제")
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun DeleteTemplateConfirmDialogPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        DeleteTemplateConfirmDialog(
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
