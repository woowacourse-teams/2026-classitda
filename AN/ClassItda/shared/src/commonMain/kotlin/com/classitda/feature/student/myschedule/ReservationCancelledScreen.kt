package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_cancelled_at_label
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultActionSection
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultTopBar
import com.classitda.feature.student.myschedule.component.result.reservation.ReservationCancelledContent
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.preview.reservationDetailPreviewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReservationCancelledScreen(
    reservation: ReservationDetailUiModel,
    cancelledAtLabel: String,
    restoredTicketCount: Int,
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
        ReservationCancelledContent(
            reservation = reservation,
            cancelledAtLabel = cancelledAtLabel,
            restoredTicketCount = restoredTicketCount,
            modifier = Modifier.weight(1f),
        )
        MyScheduleResultActionSection(
            onBookAnotherClass = onBookAnotherClass,
            onReturnToList = onReturnToList,
        )
    }
}

@Preview(
    name = "Reservation cancelled · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationCancelledScreenPreview_Success_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancelledScreen(
            reservation = reservationDetailPreviewModel().copy(locationLabel = "리포머룸"),
            cancelledAtLabel =
                stringResource(
                    Res.string.my_schedule_cancelled_at_label,
                    2026,
                    "08",
                    "04",
                    "14:32",
                ),
            restoredTicketCount = 1,
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}
