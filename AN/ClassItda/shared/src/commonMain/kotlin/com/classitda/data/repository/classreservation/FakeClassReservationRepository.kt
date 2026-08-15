package com.classitda.data.repository.classreservation

import com.classitda.domain.model.classreservation.ClassPass
import com.classitda.domain.model.classreservation.ClassReservation
import com.classitda.domain.repository.classreservation.ClassReservationRepository

internal class FakeClassReservationRepository : ClassReservationRepository {
    override fun getClassReservation(classId: String): ClassReservation =
        ClassReservation(
            id = classId,
            className = "리포머 베이직",
            dateText = "2026.08.08 (토)",
            timeText = "오전 10:00 - 10:50",
            instructorName = "이지은 강사",
            roomName = "리포머룸",
            cancellationNotice = "예약 취소는 수업 2시간 전까지 가능합니다.",
            classPasses =
                listOf(
                    ClassPass(
                        id = "pass-1",
                        name = "[그룹] 8:1 리포머 10회권",
                        usageText = "잔여 6회 / 예약 가능 2회 / 취소 가능 10회",
                        expirationText = "2026.12.31까지",
                    ),
                    ClassPass(
                        id = "pass-2",
                        name = "[이벤트] 한정판 이용권",
                        usageText = "잔여 1회 / 예약 가능 1회 / 취소 가능 10회",
                        expirationText = "2026.11.30까지",
                    ),
                    ClassPass(
                        id = "pass-3",
                        name = "요가&필라테스 정규권",
                        usageText = "잔여 4회 / 예약 가능 3회 / 취소 가능 10회",
                        expirationText = "2027.01.24까지",
                    ),
                ),
        )
}
