package com.classitda.feature.student.myschedule.component.error

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_load_error_mark
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyScheduleLoadErrorIcon(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clearAndSetSemantics { },
        shape = CircleShape,
        color = StuColors.Surface,
    ) {
        Box(
            modifier = Modifier.padding(AppSpacing.xxxl),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.my_schedule_load_error_mark),
                style = appTypography().headlineMedium.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextTertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(
    name = "Load error icon / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
)
@Composable
private fun `MyScheduleLoadErrorIconPreview_Default_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleLoadErrorIcon(modifier = Modifier.padding(AppSpacing.sectionGap))
    }
}
