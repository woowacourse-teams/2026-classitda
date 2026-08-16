package com.classitda.feature.student.reservation.complete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.classitda.domain.repository.classreservation.ClassReservationRepository
import org.koin.compose.koinInject

@Composable
internal fun ReservationCompleteRoute(
    classId: String,
    passId: String,
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
                classPassName = reservation.classPasses.firstOrNull { it.id == passId }?.name
                    ?: reservation.classPasses.first().name,
                remainingCountText = "1회",
            ),
        onCloseClick = onCloseClick,
        onScheduleClick = onScheduleClick,
        onHomeClick = onHomeClick,
    )
}
