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
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailContent
import com.classitda.feature.student.myschedule.component.detail.reservation.ReservationDetailTopBar
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.preview.reservationDetailPreviewModel

@Composable
fun ReservationDetailScreen(
    model: ReservationDetailUiModel,
    onBack: () -> Unit,
    onInquiry: (ScheduleItemId) -> Unit,
    onCancelReservation: (ScheduleItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        ReservationDetailTopBar(onBack = onBack)
        ReservationDetailContent(
            model = model,
            onInquiry = { onInquiry(model.id) },
            onCancelReservation = { onCancelReservation(model.id) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(
    name = "Reservation detail · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationDetailScreenPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailScreen(
            model = reservationDetailPreviewModel(),
            onBack = {},
            onInquiry = {},
            onCancelReservation = {},
        )
    }
}
