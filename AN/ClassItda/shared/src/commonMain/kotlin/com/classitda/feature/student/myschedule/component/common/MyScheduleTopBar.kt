package com.classitda.feature.student.myschedule.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_title
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

internal val myScheduleTopBarHeight = 52.dp

@Composable
internal fun MyScheduleTopBar(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StuColors.Surface,
    ) {
        Box(
            modifier =
                Modifier
                    .height(myScheduleTopBarHeight)
                    .padding(horizontal = AppSpacing.screenPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(Res.string.my_schedule_title),
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Preview(
    name = "Top bar / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `MyScheduleTopBarPreview_Default_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleTopBar()
    }
}
