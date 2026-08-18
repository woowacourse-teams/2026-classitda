package com.classitda.domain.repository.student.myschedule

import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MyScheduleResultTest {
    @Test
    fun `실패 결과는 반환 데이터 타입과 무관하게 사용할 수 있다`() {
        val failure = MyScheduleResult.Failure(MyScheduleFailureReason.NETWORK)

        val upcomingResult: MyScheduleResult<List<UpcomingSchedule>> = failure
        val reservationDetailResult: MyScheduleResult<ReservationDetail> = failure

        assertEquals(failure, upcomingResult)
        assertEquals(failure, reservationDetailResult)
    }

    @Test
    fun `실패 이유는 확정된 도메인 상태만 제공한다`() {
        assertContentEquals(
            listOf(
                MyScheduleFailureReason.NETWORK,
                MyScheduleFailureReason.NOT_FOUND,
                MyScheduleFailureReason.CONFLICT,
                MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED,
                MyScheduleFailureReason.UNKNOWN,
            ),
            MyScheduleFailureReason.entries,
        )
    }
}
