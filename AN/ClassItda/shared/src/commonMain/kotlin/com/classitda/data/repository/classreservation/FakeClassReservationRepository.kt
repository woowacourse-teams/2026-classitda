package com.classitda.data.repository.classreservation

import com.classitda.data.local.reservation.FakeReservationStore
import com.classitda.data.local.reservation.toReservationDateText
import com.classitda.domain.model.classreservation.ClassPass
import com.classitda.domain.model.classreservation.ClassReservation
import com.classitda.domain.model.classreservation.ReservationRequestResult
import com.classitda.domain.repository.classreservation.ClassReservationRepository

internal class FakeClassReservationRepository(
    private val store: FakeReservationStore = FakeReservationStore(),
) : ClassReservationRepository {
    override fun getClassReservation(classId: String): ClassReservation {
        val selectedClass = requireNotNull(store.getClassById(classId)) {
            "수업 정보를 찾을 수 없습니다."
        }
        return ClassReservation(
            id = selectedClass.id,
            className = selectedClass.className,
            dateText = selectedClass.date.toReservationDateText(),
            timeText = selectedClass.classTime,
            instructorName = selectedClass.instructorName,
            roomName = selectedClass.roomName.orEmpty(),
            memoText = selectedClass.roomName.orEmpty(),
            cancellationNotice = "예약 취소 및 변경은 수업 시작 4시간 전까지 가능합니다.",
            classPasses =
                listOf(
                    ClassPass(
                        id = "pass-1",
                        name = "[그룹] 8:1 리포머/체어 10회권",
                        usageText = "잔여 6회 / 예약 가능 2회 / 취소 가능 10회",
                        validityPeriodText = "유효기간: 2026.08.01 ~ 2026.12.31",
                    ),
                    ClassPass(
                        id = "pass-2",
                        name = "[이벤트] 한정판 이용권",
                        usageText = "잔여 1회 / 예약 가능 1회 / 취소 가능 10회",
                        validityPeriodText = "유효기간: 없음",
                    ),
                    ClassPass(
                        id = "pass-3",
                        name = "요가&필라테스 정규권",
                        usageText = "잔여 4회 / 예약 가능 3회 / 취소 가능 10회",
                        validityPeriodText = "유효기간: 2026.08.01 ~ 2027.01.24",
                    ),
                ),
        )
    }

    override fun reserve(
        classId: String,
        passId: String,
    ): ReservationRequestResult =
        when (passId) {
            "pass-2" ->
                ReservationRequestResult.TimeConflict(
                    className = "리포머 밸런스",
                    dateTimeText = "2026.08.08 (토) 오후 7:30 - 8:20",
                    studioName = "클래스잇다 2호점",
                )

            "pass-3" -> ReservationRequestResult.Failure("예약 가능 시간이 종료되었습니다.")
            else -> {
                store.saveReservation(classId)
                ReservationRequestResult.Success
            }
        }
}
