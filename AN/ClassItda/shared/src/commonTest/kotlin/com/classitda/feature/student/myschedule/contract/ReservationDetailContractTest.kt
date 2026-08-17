package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class ReservationDetailContractTest {
    @Test
    fun `예약 상세 네 상태는 하나의 Content 상태 안에서 서로 다른 타입으로 유지된다`() {
        val confirmedState = ReservationDetailUiState.Content(createConfirmed())
        val cancelledState = ReservationDetailUiState.Content(createCancelled())
        val attendedState = ReservationDetailUiState.Content(createAttended())
        val absentState = ReservationDetailUiState.Content(createAbsent())

        assertIs<ReservationDetailUiModel.Confirmed>(confirmedState.detail)
        assertIs<ReservationDetailUiModel.Cancelled>(cancelledState.detail)
        assertIs<ReservationDetailUiModel.Attended>(attendedState.detail)
        assertIs<ReservationDetailUiModel.Absent>(absentState.detail)
    }

    @Test
    fun `로딩 콘텐츠 오류는 동시에 표현되지 않는 배타적 상태다`() {
        val states =
            listOf(
                ReservationDetailUiState.Loading,
                ReservationDetailUiState.Content(createAttended()),
                ReservationDetailUiState.Error(ReservationDetailErrorUiModel.NETWORK),
            )

        assertIs<ReservationDetailUiState.Loading>(states[0])
        assertIs<ReservationDetailUiState.Content>(states[1])
        assertIs<ReservationDetailUiState.Error>(states[2])
    }

    @Test
    fun `취소 가능한 예약 완료만 같은 ReservationId의 취소 Action을 제공한다`() {
        val confirmed = createConfirmed()

        val action = confirmed.cancellationActionOrNull()

        assertEquals(
            ReservationDetailAction.CancelReservation(confirmed.reservationId),
            action,
        )
        assertNull(createCancelled().cancellationActionOrNull())
        assertNull(createAttended().cancellationActionOrNull())
        assertNull(createAbsent().cancellationActionOrNull())
    }

    @Test
    fun `예약 완료여도 취소 불가 상태면 취소 Action을 제공하지 않는다`() {
        val unavailable =
            createConfirmed().copy(
                cancellation =
                    ReservationCancellationAvailabilityUiModel.Unavailable(
                        reason = ReservationCancellationUnavailableReasonUiModel.DEADLINE_PASSED,
                    ),
            )

        assertNull(unavailable.cancellationActionOrNull())
    }

    @Test
    fun `예약 취소 가능 기준 시간은 0을 허용하고 음수를 거부한다`() {
        createConfirmed().copy(cancellationDeadlineHoursBeforeStart = 0)

        assertFailsWith<IllegalArgumentException> {
            createConfirmed().copy(cancellationDeadlineHoursBeforeStart = -1)
        }
    }

    private fun createConfirmed(): ReservationDetailUiModel.Confirmed =
        ReservationDetailUiModel.Confirmed(
            reservationId = ReservationId("reservation-confirmed"),
            title = "체어 밸런스",
            classInfo = createClassInfo(),
            reservedAtLabel = "2026.08.01 (토) 오후 3:20",
            pass =
                ReservationPassAvailabilityUiModel(
                    name = "리포머 20회권",
                    validityLabel = "2026.07.01 ~ 2026.09.30",
                    remainingUses = 5,
                    reservableUses = 5,
                    cancellableUses = 2,
                ),
            cancellationDeadlineHoursBeforeStart = 4,
            cancellation =
                ReservationCancellationAvailabilityUiModel.Available(
                    hoursUntilStart = 22,
                    restoredPassUses = 1,
                ),
        )

    private fun createCancelled(): ReservationDetailUiModel.Cancelled =
        ReservationDetailUiModel.Cancelled(
            reservationId = ReservationId("reservation-cancelled"),
            title = "체어 밸런스",
            classInfo = createClassInfo(),
            cancelledAtLabel = "2026.08.01 (토) 오후 3:25",
        )

    private fun createAttended(): ReservationDetailUiModel.Attended =
        ReservationDetailUiModel.Attended(
            reservationId = ReservationId("reservation-attended"),
            title = "체어 밸런스",
            classInfo = createClassInfo(),
            checkedInAtLabel = "2026.08.04 (화) 오후 6:20",
            usedPass = createUsedPass(),
        )

    private fun createAbsent(): ReservationDetailUiModel.Absent =
        ReservationDetailUiModel.Absent(
            reservationId = ReservationId("reservation-absent"),
            title = "체어 밸런스",
            classInfo = createClassInfo(),
            attendanceTimePlaceholder = "--:--:--",
            usedPass = createUsedPass(),
        )

    private fun createClassInfo(): ReservationClassInfoUiModel =
        ReservationClassInfoUiModel(
            dateLabel = "2026.08.04 (화)",
            timeRangeLabel = "오후 6:30 ~ 7:20",
            memo = "오늘은 하타룸으로 오세요~",
            instructorName = "이지은 강사",
            facilityName = "밸런스 필라테스 성수점",
        )

    private fun createUsedPass(): ReservationUsedPassUiModel =
        ReservationUsedPassUiModel(
            name = "리포머 20회권",
            validityLabel = "2026.07.01 ~ 2026.09.30",
        )
}
