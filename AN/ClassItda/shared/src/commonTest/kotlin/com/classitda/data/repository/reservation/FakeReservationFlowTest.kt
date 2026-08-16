package com.classitda.data.repository.reservation

import com.classitda.data.local.reservation.FakeReservationStore
import com.classitda.data.repository.classreservation.FakeClassReservationRepository
import com.classitda.data.repository.waitlist.FakeWaitlistReservationRepository
import com.classitda.domain.model.classreservation.ReservationRequestResult
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeReservationFlowTest {
    @Test
    fun `예약 성공 결과는 공용 저장소의 수업 상태에 반영된다`() {
        val store = FakeReservationStore()
        val requestRepository = FakeClassReservationRepository(store)
        val listRepository = FakeReservationRepository(store)

        val result = requestRepository.reserve(classId = "1", passId = "pass-1")

        assertIs<ReservationRequestResult.Success>(result)
        assertTrue(listRepository.getClasses().first { it.id == "1" }.isReserved)
    }

    @Test
    fun `두 번째 수강권은 시간 충돌 결과를 반환하고 저장하지 않는다`() {
        val store = FakeReservationStore()
        val requestRepository = FakeClassReservationRepository(store)
        val listRepository = FakeReservationRepository(store)

        val result = requestRepository.reserve(classId = "1", passId = "pass-2")

        assertIs<ReservationRequestResult.TimeConflict>(result)
        assertFalse(listRepository.getClasses().first { it.id == "1" }.isReserved)
    }

    @Test
    fun `대기 예약 성공 결과는 공용 저장소의 수업 상태에 반영된다`() {
        val store = FakeReservationStore()
        val requestRepository = FakeWaitlistReservationRepository(store)
        val listRepository = FakeReservationRepository(store)

        val result = requestRepository.applyWaitlist(classId = "2", passId = "pass-1")

        assertTrue(result)
        assertTrue(listRepository.getClasses().first { it.id == "2" }.isWaitlisted)
    }

    @Test
    fun `세 번째 수강권은 일반 예약 실패 결과를 반환한다`() {
        val repository = FakeClassReservationRepository(FakeReservationStore())

        val result = repository.reserve(classId = "1", passId = "pass-3")

        assertEquals("예약 가능 시간이 종료되었습니다.", assertIs<ReservationRequestResult.Failure>(result).message)
    }

    @Test
    fun `두 번째 수강권은 대기 예약 실패 결과를 반환한다`() {
        val repository = FakeWaitlistReservationRepository(FakeReservationStore())

        assertFalse(repository.applyWaitlist(classId = "2", passId = "pass-2"))
    }
}
