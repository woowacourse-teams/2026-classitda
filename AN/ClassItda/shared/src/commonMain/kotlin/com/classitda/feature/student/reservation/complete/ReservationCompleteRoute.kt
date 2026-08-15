package com.classitda.feature.student.reservation.complete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.classitda.feature.student.reservation.domain.repository.classreservation.ClassReservationRepository
import org.koin.compose.koinInject

@Composable
internal fun ReservationCompleteRoute(
    classId: String,
    onCloseClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onHomeClick: () -> Unit,
    repository: ClassReservationRepository = koinInject(),
) {
    val reservation =
        remember(classId) {
            repository.getClassReservation(classId)
        }

    ReservationCompleteScreen(
        reservation =
            ReservationCompleteUiModel(
                id = reservation.id,
                className = reservation.className,
                dateText = reservation.dateText,
                timeText = reservation.timeText,
                instructorName = reservation.instructorName,
                roomName = reservation.roomName,
                classPassName = reservation.classPasses.first().name,
                remainingCountText = "5회",
            ),
        onCloseClick = onCloseClick,
        onScheduleClick = onScheduleClick,
        onHomeClick = onHomeClick,
    )
}
