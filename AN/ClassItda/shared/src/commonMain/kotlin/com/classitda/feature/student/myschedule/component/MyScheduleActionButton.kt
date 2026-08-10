package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_go_home
import classitda.shared.generated.resources.my_schedule_retry
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
        shape = MaterialTheme.shapes.large,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = StuColors.PrimaryGreen,
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
        shape = MaterialTheme.shapes.large,
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
