package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.ScheduleReservationOriginUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleTicketRestorationUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleWaitlistReapplicationUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.utils.previewUpcomingScheduleDateTime
import kotlin.time.Instant

@Composable
internal fun myScheduleReservationsPreviewItems(): List<UpcomingScheduleItemUiModel> =
    listOf(
        UpcomingScheduleItemUiModel.ConfirmedReservation(
            id = ScheduleItemId("preview-confirmed-reservation"),
            title = "리포머 밸런스",
            dateTime =
                previewUpcomingScheduleDateTime(
                    startAt = Instant.parse("2026-08-08T10:30:00Z"),
                    endAt = Instant.parse("2026-08-08T11:20:00Z"),
                ),
            locationLabel = "스튜디오 B",
            instructorName = "이지은",
            origin = ScheduleReservationOriginUiModel.DIRECT,
            cancellation =
                ScheduleCancellationAvailabilityUiModel.Available(
                    policy =
                        ScheduleCancellationPolicyUiModel.Reservation(
                            deadlineHoursBeforeStart = 12,
                            ticketRestoration = ScheduleTicketRestorationUiModel.AccordingToFacilityPolicy,
                        ),
                ),
        ),
        UpcomingScheduleItemUiModel.Waitlist(
            id = ScheduleItemId("preview-waitlist"),
            title = "캐딜락 스트레칭",
            dateTime =
                previewUpcomingScheduleDateTime(
                    startAt = Instant.parse("2026-08-09T02:00:00Z"),
                    endAt = Instant.parse("2026-08-09T02:50:00Z"),
                ),
            locationLabel = "하타룸",
            instructorName = "박소연",
            position = 1,
            cancellation =
                ScheduleCancellationAvailabilityUiModel.Available(
                    policy =
                        ScheduleCancellationPolicyUiModel.Waitlist(
                            reapplicationRule = ScheduleWaitlistReapplicationUiModel.LastPosition,
                        ),
                ),
        ),
        UpcomingScheduleItemUiModel.ConfirmedReservation(
            id = ScheduleItemId("preview-confirmed-reservation-basic"),
            title = "리포머 베이직",
            dateTime =
                previewUpcomingScheduleDateTime(
                    startAt = Instant.parse("2026-08-12T01:00:00Z"),
                    endAt = Instant.parse("2026-08-12T01:50:00Z"),
                ),
            locationLabel = "리포머룸",
            instructorName = "김하늘",
            origin = ScheduleReservationOriginUiModel.DIRECT,
            cancellation =
                ScheduleCancellationAvailabilityUiModel.Available(
                    policy =
                        ScheduleCancellationPolicyUiModel.Reservation(
                            deadlineHoursBeforeStart = 12,
                            ticketRestoration = ScheduleTicketRestorationUiModel.AccordingToFacilityPolicy,
                        ),
                ),
        ),
    )
