package com.classitda.feature.student.reservation.data.repository.waitlist

import com.classitda.feature.student.reservation.domain.model.waitlist.WaitlistClassPass
import com.classitda.feature.student.reservation.domain.model.waitlist.WaitlistReservation
import com.classitda.feature.student.reservation.domain.repository.waitlist.WaitlistReservationRepository

internal class FakeWaitlistReservationRepository : WaitlistReservationRepository {
    override fun getWaitlistReservation(classId: String): WaitlistReservation =
        WaitlistReservation(
            id = classId,
            className = "리포머 베이직",
            dateText = "2026.08.08 (토)",
            timeText = "오전 10:00 - 10:50",
            instructorName = "이지은 강사",
            roomName = "리포머룸",
            cancellationNotice = "예약 취소는 수업 2시간 전까지 가능합니다.",
            classPasses =
                listOf(
                    WaitlistClassPass(
                        id = "pass-1",
                        name = "[그룹] 8:1 리포머 10회권",
                        usageText = "잔여 6회 / 예약 가능 2회 / 취소 가능 10회",
                        expirationText = "2026.12.31까지",
                    ),
                    WaitlistClassPass(
                        id = "pass-2",
                        name = "[이벤트] 한정판 이용권",
                        usageText = "잔여 1회 / 예약 가능 1회 / 취소 가능 10회",
                        expirationText = "2026.11.30까지",
                    ),
                ),
            expectedWaitingNumber = 3,
        )
}
