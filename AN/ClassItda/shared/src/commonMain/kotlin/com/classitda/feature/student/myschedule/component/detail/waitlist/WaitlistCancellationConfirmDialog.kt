package com.classitda.feature.student.myschedule.component.detail.waitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_error
import classitda.shared.generated.resources.my_schedule_waitlist_cancel_confirm_description
import classitda.shared.generated.resources.my_schedule_waitlist_cancel_confirm_dismiss
import classitda.shared.generated.resources.my_schedule_waitlist_cancel_confirm_submit
import classitda.shared.generated.resources.my_schedule_waitlist_cancel_confirm_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleWarningButton
import com.classitda.feature.student.myschedule.preview.WaitlistDetailPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistCancellationConfirmDialog(
    position: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.my_schedule_waitlist_cancel_confirm_title)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xxl)
                    .semantics { paneTitle = title },
            shape = AppShape.Card,
            color = StuColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
            ) {
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = StuColors.Orange,
                                shape = AppShape.Pill,
                            ).padding(AppSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = null,
                        tint = StuColors.White,
                    )
                }
                Text(
                    text = title,
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.my_schedule_waitlist_cancel_confirm_description,
                            position,
                        ),
                    style = appTypography().bodyMedium,
                    color = StuColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WaitlistCancellationDismissButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    MyScheduleWarningButton(
                        text = stringResource(Res.string.my_schedule_waitlist_cancel_confirm_submit),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitlistCancellationDismissButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.SurfaceVariant,
                contentColor = StuColors.TextSecondary,
            ),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_waitlist_cancel_confirm_dismiss),
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Preview(
    name = "Waitlist cancellation confirm dialog · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistCancellationConfirmDialogPreview_Confirming_Student_Default() {
    val fixture = WaitlistDetailPreviewFixture.pending

    AppTheme(theme = ThemeType.STUDENT) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(StuColors.Background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                WaitlistDetailTopBar(onBack = {})
                WaitlistDetailContent(
                    model = fixture,
                    onCancelWaitlist = {},
                    modifier = Modifier.weight(1f),
                )
            }
            WaitlistCancellationConfirmDialog(
                position = fixture.currentPosition,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
