package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.common.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.error.MyScheduleLoadErrorContent

@Composable
fun MyScheduleLoadErrorScreen(
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        MyScheduleTopBar()
        MyScheduleLoadErrorContent(
            onRetry = onRetry,
            onGoHome = onGoHome,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(
    name = "Load error / Student / Default",
    group = "Screen/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 756,
)
@Composable
private fun MyScheduleLoadErrorScreenPreview_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleLoadErrorScreen(
            onRetry = {},
            onGoHome = {},
        )
    }
}
