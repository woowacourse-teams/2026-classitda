package com.classitda.data.repository.reservation

import com.classitda.data.local.reservation.FakeReservationStore
import com.classitda.domain.model.reservation.ReservationClass
import com.classitda.domain.model.reservation.ReservationPass
import com.classitda.domain.repository.reservation.ReservationRepository

internal class FakeReservationRepository(
    private val store: FakeReservationStore = FakeReservationStore(),
) : ReservationRepository {
    override fun getClasses(): List<ReservationClass> = store.getClasses()

    override fun getPasses(): List<ReservationPass> =
        listOf(
            ReservationPass(
                id = "pass-1",
                name = "요가 10회권",
                remainingText = "잔여 7회 / 예약 가능 7회",
                validityPeriodText = "유효기간: 2026.08.01 ~ 2026.10.31",
            ),
            ReservationPass(
                id = "pass-2",
                name = "필라테스 20회권",
                remainingText = "잔여 12회 / 예약 가능 12회",
                validityPeriodText = "유효기간: 2026.08.01 ~ 2026.11.30",
            ),
            ReservationPass(
                id = "pass-3",
                name = "요가 / 필라테스 통합 1회권",
                remainingText = "잔여 1회 / 예약 가능 1회",
                validityPeriodText = "유효기간: 2026.08.01 ~ 2027.01.24",
            ),
        )
}
