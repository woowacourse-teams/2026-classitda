package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultActionSection
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultTopBar
import com.classitda.feature.student.myschedule.component.result.reservation.ReservationCancellationCompletedContent
import com.classitda.feature.student.myschedule.contract.ReservationCancellationResultUiModel
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture

@Composable
fun ReservationCancellationCompletedScreen(
    result: ReservationCancellationResultUiModel,
    onBack: () -> Unit,
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        MyScheduleResultTopBar(onBack = onBack)
        ReservationCancellationCompletedContent(
            result = result,
            modifier = Modifier.weight(1f),
        )
        MyScheduleResultActionSection(
            onBookAnotherClass = onBookAnotherClass,
            onReturnToList = onReturnToList,
        )
    }
}

@Preview(
    name = "F05 reservation cancellation completed / Student",
    group = "Screen/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationCancellationCompletedScreenPreview_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationCompletedScreen(
            result = ReservationDetailPreviewFixture.cancellationCompleted,
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}

@Preview(
    name = "F05 callback harness / Student",
    group = "Harness/MySchedule/ReservationCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 900,
)
@Composable
private fun ReservationCancellationCompletedScreenPreview_CallbackHarness_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        var lastCallback by remember { mutableStateOf("마지막 Callback: 없음") }

        Column(modifier = Modifier.fillMaxSize()) {
            ReservationCancellationCompletedScreen(
                result = ReservationDetailPreviewFixture.cancellationCompleted,
                onBack = { lastCallback = "마지막 Callback: onBack" },
                onBookAnotherClass = {
                    lastCallback = "마지막 Callback: onBookAnotherClass"
                },
                onReturnToList = { lastCallback = "마지막 Callback: onReturnToList" },
                modifier = Modifier.weight(1f),
            )
            Text(
                text = lastCallback,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(StuColors.SurfaceVariant)
                        .padding(AppSpacing.md),
                style = appTypography().bodySmall,
                color = StuColors.TextPrimary,
            )
        }
    }
}
