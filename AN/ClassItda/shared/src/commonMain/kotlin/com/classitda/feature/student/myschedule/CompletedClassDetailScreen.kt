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
import com.classitda.feature.student.myschedule.component.detail.history.CompletedClassDetailContent
import com.classitda.feature.student.myschedule.component.detail.history.CompletedClassDetailTopBar
import com.classitda.feature.student.myschedule.contract.CompletedClassDetailUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.preview.completedClassDetailPreviewModel

@Composable
fun CompletedClassDetailScreen(
    model: CompletedClassDetailUiModel,
    onBack: () -> Unit,
    onOpenInstructor: (ScheduleItemId) -> Unit,
    onInquiry: (ScheduleItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        CompletedClassDetailTopBar(
            onBack = onBack,
        )
        CompletedClassDetailContent(
            model = model,
            onOpenInstructor = { onOpenInstructor(model.id) },
            onInquiry = { onInquiry(model.id) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(
    name = "Completed class detail · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 391,
    heightDp = 895,
)
@Composable
private fun CompletedClassDetailScreenPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        CompletedClassDetailScreen(
            model = completedClassDetailPreviewModel(),
            onBack = {},
            onOpenInstructor = {},
            onInquiry = {},
        )
    }
}
