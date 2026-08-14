package com.classitda.feature.student.myschedule.component.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_cancel_confirm_dismiss
import classitda.shared.generated.resources.my_schedule_cancel_reservation
import classitda.shared.generated.resources.my_schedule_cancel_waitlist
import classitda.shared.generated.resources.my_schedule_go_home
import classitda.shared.generated.resources.my_schedule_retry
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MySchedulePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.Green,
                contentColor = StuColors.White,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Preview(
    name = "Primary button / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `MySchedulePrimaryButtonPreview_Default_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MySchedulePrimaryButton(
            text = stringResource(Res.string.my_schedule_retry),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Composable
internal fun MyScheduleSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = StuColors.Surface,
                contentColor = StuColors.TextSecondary,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
internal fun MyScheduleDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.Red,
                contentColor = StuColors.White,
                disabledContainerColor = StuColors.Divider,
                disabledContentColor = StuColors.TextTertiary,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
internal fun MyScheduleWarningButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.Orange,
                contentColor = StuColors.White,
                disabledContainerColor = StuColors.Divider,
                disabledContentColor = StuColors.TextTertiary,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
internal fun MyScheduleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(contentColor = StuColors.TextSecondary),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Preview(
    name = "Secondary button / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `MyScheduleSecondaryButtonPreview_Default_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleSecondaryButton(
            text = stringResource(Res.string.my_schedule_go_home),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Destructive button / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleDestructiveButtonPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleDestructiveButton(
            text = stringResource(Res.string.my_schedule_cancel_reservation),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Warning button / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleWarningButtonPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleWarningButton(
            text = stringResource(Res.string.my_schedule_cancel_waitlist),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Text button / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun MyScheduleTextButtonPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTextButton(
            text = stringResource(Res.string.my_schedule_cancel_confirm_dismiss),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
