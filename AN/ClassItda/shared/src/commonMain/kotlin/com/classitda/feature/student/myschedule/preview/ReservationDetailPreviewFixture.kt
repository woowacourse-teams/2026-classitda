package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationTicketUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.ScheduleTicketRestorationUiModel
import com.classitda.feature.student.myschedule.utils.previewReservationDetailDateTime
import com.classitda.feature.student.myschedule.utils.previewTicketValidUntilLabel
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Composable
internal fun reservationDetailPreviewModel(): ReservationDetailUiModel =
    ReservationDetailUiModel(
        id = ScheduleItemId("preview-reservation-detail"),
        title = "리포머 밸런스",
        instructorName = "이지은",
        dateTime =
            previewReservationDetailDateTime(
                startAt = Instant.parse("2026-08-06T10:30:00Z"),
                endAt = Instant.parse("2026-08-06T11:20:00Z"),
            ),
        locationLabel = "밸런스 필라테스 성수점 · 리포머룸",
        ticket =
            ReservationTicketUiModel(
                name = "리포머 20회권",
                validUntilLabel = previewTicketValidUntilLabel(LocalDate(2026, 9, 30)),
                remainingReservationCount = 5,
            ),
        attendeeCount = 1,
        cancellation =
            ScheduleCancellationAvailabilityUiModel.Available(
                policy =
                    ScheduleCancellationPolicyUiModel.Reservation(
                        deadlineHoursBeforeStart = 6,
                        ticketRestoration = ScheduleTicketRestorationUiModel.AccordingToFacilityPolicy,
                    ),
            ),
    )
