package com.classitda.feature.student.reservation.complete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.classitda.domain.repository.waitlist.WaitlistReservationRepository
import org.koin.compose.koinInject

@Composable
internal fun WaitlistCompleteRoute(
    classId: String,
    onCloseClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onHomeClick: () -> Unit,
    repository: WaitlistReservationRepository = koinInject(),
) {
    val reservation =
        remember(classId) {
            repository.getWaitlistReservation(classId)
        }

    WaitlistCompleteScreen(
        reservation =
            WaitlistCompleteUiModel(
                id = reservation.id,
                className = reservation.className,
                dateText = reservation.dateText,
                timeText = reservation.timeText,
                instructorName = reservation.instructorName,
                roomName = reservation.roomName,
                expectedWaitingNumber = reservation.expectedWaitingNumber,
            ),
        onCloseClick = onCloseClick,
        onScheduleClick = onScheduleClick,
        onHomeClick = onHomeClick,
    )
}
