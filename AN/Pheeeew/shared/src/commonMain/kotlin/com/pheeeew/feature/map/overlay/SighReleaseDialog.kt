package com.pheeeew.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pheeeew.core.designsystem.theme.AppColors
import com.pheeeew.core.designsystem.theme.AppTheme

@Composable
fun SighReleaseDialog(
    sighReleaseState: SighReleaseState,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        SighReleaseDialogContent(
            sighReleaseState = sighReleaseState,
            onSendClick = onSendClick,
            onCancelClick = onCancelClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun SighReleaseDialogContent(
    sighReleaseState: SighReleaseState,
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(20.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = AppTheme.colors.surface,
            contentColor = AppTheme.colors.onBackground,
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                Text(text = "한숨 야호옹~", style = AppTheme.typography.screenTitle)

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20))
                            .background(AppColors.Blue100)
                            .clickable(
                                enabled = sighReleaseState !is SighReleaseState.Submitting,
                                onClick = onSendClick,
                            ).padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (sighReleaseState is SighReleaseState.Submitting) "전송 중..." else "하늘에 슈우웃",
                        style = AppTheme.typography.menuItem,
                        color = AppColors.Navy900,
                    )
                }

                if (sighReleaseState is SighReleaseState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sighReleaseState.message,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.danger,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onCancelClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "취소",
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SighReleaseDialogIdlePreview() {
    AppTheme {
        SighReleaseDialogContent(
            sighReleaseState = SighReleaseState.Idle,
            onSendClick = {},
            onCancelClick = {},
        )
    }
}

@Preview
@Composable
private fun SighReleaseDialogSubmittingPreview() {
    AppTheme {
        SighReleaseDialogContent(
            sighReleaseState = SighReleaseState.Submitting,
            onSendClick = {},
            onCancelClick = {},
        )
    }
}

@Preview
@Composable
private fun SighReleaseDialogErrorPreview() {
    AppTheme {
        SighReleaseDialogContent(
            sighReleaseState = SighReleaseState.Error("전송에 실패했어요. 다시 시도해주세요."),
            onSendClick = {},
            onCancelClick = {},
        )
    }
}
