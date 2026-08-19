package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UpcomingDateSectionUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleTabState

internal object MyScheduleUpcomingPreviewFixture {
    val confirmedReservation =
        UpcomingScheduleCardUiModel.ConfirmedReservation(
            reservationId = ReservationId("preview-reservation-f01"),
            timeRangeLabel = "오후 7:30 ~ 8:20",
            title = "리포머 밸런스",
            instructorName = "이지은 강사",
        )

    val waitlisted =
        UpcomingScheduleCardUiModel.Waitlisted(
            waitlistId = WaitlistId("preview-waitlist-f01"),
            currentPosition = 1,
            timeRangeLabel = "오전 11:00 ~ 오후 12:50",
            title = "엄청나게 어마어마하게 긴 글자의 수업을 가진 필라테스",
            instructorName = "박소연 대표 강사",
        )

    val approvalRequired =
        waitlisted.copy(
            waitlistId = WaitlistId("preview-waitlist-approval-required"),
            currentPosition = 0,
        )

    val waitlistedPosition2 =
        waitlisted.copy(
            waitlistId = WaitlistId("preview-waitlist-position-2"),
            currentPosition = 2,
        )

    val approvedFromWaitlist =
        UpcomingScheduleCardUiModel.ConfirmedReservation(
            reservationId = ReservationId("preview-reservation-approved-from-waitlist"),
            timeRangeLabel = approvalRequired.timeRangeLabel,
            title = approvalRequired.title,
            instructorName = approvalRequired.instructorName,
        )

    val sections =
        listOf(
            UpcomingDateSectionUiModel(
                dateLabel = "8월 8일 토요일",
                items = listOf(confirmedReservation),
            ),
            UpcomingDateSectionUiModel(
                dateLabel = "8월 9일 일요일",
                items = listOf(approvalRequired, waitlisted, waitlistedPosition2),
            ),
        )

    val sectionsAfterApproval =
        listOf(
            UpcomingDateSectionUiModel(
                dateLabel = "8월 8일 토요일",
                items = listOf(confirmedReservation),
            ),
            UpcomingDateSectionUiModel(
                dateLabel = "8월 9일 일요일",
                items = listOf(approvedFromWaitlist, waitlisted, waitlistedPosition2),
            ),
        )

    val state =
        MyScheduleUiState(
            upcoming = UpcomingScheduleTabState.Content(sections = sections),
        )
}
