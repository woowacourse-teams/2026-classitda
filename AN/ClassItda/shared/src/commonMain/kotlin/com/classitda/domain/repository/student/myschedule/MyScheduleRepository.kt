package com.classitda.domain.repository.student.myschedule

import com.classitda.domain.model.student.myschedule.ReservationCancellationReceipt
import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import com.classitda.domain.model.student.myschedule.UsageHistoryEntry
import com.classitda.domain.model.student.myschedule.WaitlistCancellationReceipt
import com.classitda.domain.model.student.myschedule.WaitlistDetail
import com.classitda.domain.model.student.myschedule.WaitlistId

interface MyScheduleRepository {
    suspend fun getUpcomingSchedules(): MyScheduleResult<List<UpcomingSchedule>>

    suspend fun getUsageHistory(): MyScheduleResult<List<UsageHistoryEntry>>

    suspend fun getReservationDetail(reservationId: ReservationId): MyScheduleResult<ReservationDetail>

    suspend fun getWaitlistDetail(waitlistId: WaitlistId): MyScheduleResult<WaitlistDetail>

    suspend fun cancelReservation(reservationId: ReservationId): MyScheduleResult<ReservationCancellationReceipt>

    suspend fun cancelWaitlist(waitlistId: WaitlistId): MyScheduleResult<WaitlistCancellationReceipt>

    suspend fun approveWaitlist(waitlistId: WaitlistId): MyScheduleResult<Unit>
}
