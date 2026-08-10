package com.classitda.feature.student.myschedule.preview

import com.classitda.feature.student.myschedule.contract.ActiveScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleDateTimeUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.ScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleReservationOriginUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleTicketRestorationUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleWaitlistReapplicationUiModel

internal val myScheduleReservationsPreviewItems: List<ActiveScheduleItemUiModel> =
    listOf(
        ScheduleItemUiModel.ConfirmedReservation(
            id = ScheduleItemId("preview-confirmed-reservation"),
            title = "리포머 밸런스",
            dateTime =
                ScheduleDateTimeUiModel(
                    dateLabel = "8월 8일 토요일",
                    timeLabel = "오후 7:30 - 8:20",
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
        ScheduleItemUiModel.Waitlist(
            id = ScheduleItemId("preview-waitlist"),
            title = "캐딜락 스트레칭",
            dateTime =
                ScheduleDateTimeUiModel(
                    dateLabel = "8월 9일 일요일",
                    timeLabel = "오전 11:00 - 11:50",
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
        ScheduleItemUiModel.ConfirmedReservation(
            id = ScheduleItemId("preview-confirmed-reservation-basic"),
            title = "리포머 베이직",
            dateTime =
                ScheduleDateTimeUiModel(
                    dateLabel = "8월 12일 수요일",
                    timeLabel = "오전 10:00 - 10:50",
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
