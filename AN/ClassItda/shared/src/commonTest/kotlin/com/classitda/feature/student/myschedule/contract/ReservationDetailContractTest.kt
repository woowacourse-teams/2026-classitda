package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReservationDetailContractTest {
    @Test
    fun `예약 상세 네 상태는 하나의 Content 상태 안에서 서로 다른 타입으로 유지된다`() {
        val confirmedState = ReservationDetailUiState.Content(createConfirmed())
        val cancelledState = ReservationDetailUiState.Content(createCancelled())
        val classCancelledState = ReservationDetailUiState.Content(createClassCancelled())
        val attendedState = ReservationDetailUiState.Content(createAttended())
        val absentState = ReservationDetailUiState.Content(createAbsent())

        assertIs<ReservationDetailUiModel.Confirmed>(confirmedState.detail)
        assertIs<ReservationDetailUiModel.Cancelled>(cancelledState.detail)
        assertIs<ReservationDetailUiModel.ClassCancelled>(classCancelledState.detail)
        assertIs<ReservationDetailUiModel.Attended>(attendedState.detail)
        assertIs<ReservationDetailUiModel.Absent>(absentState.detail)
    }

    @Test
    fun `로딩 콘텐츠 오류는 동시에 표현되지 않는 배타적 상태다`() {
        val states =
            listOf(
                ReservationDetailUiState.Loading,
                ReservationDetailUiState.Content(createAttended()),
                ReservationDetailUiState.CancellationCompleted(createCancellationResult()),
                ReservationDetailUiState.Error(ReservationDetailErrorUiModel.NETWORK),
            )

        assertIs<ReservationDetailUiState.Loading>(states[0])
        assertIs<ReservationDetailUiState.Content>(states[1])
        assertIs<ReservationDetailUiState.CancellationCompleted>(states[2])
        assertIs<ReservationDetailUiState.Error>(states[3])
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
        assertNull(createClassCancelled().cancellationActionOrNull())
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

    @Test
    fun `취소 확인 창은 취소 가능한 예약 완료 상세에만 결합할 수 있다`() {
        val waiting = ReservationCancellationDialogUiState.Waiting

        ReservationDetailUiState.Content(
            detail = createConfirmed(),
            cancellationDialog = waiting,
        )

        assertFailsWith<IllegalArgumentException> {
            ReservationDetailUiState.Content(
                detail = createCancelled(),
                cancellationDialog = waiting,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ReservationDetailUiState.Content(
                detail =
                    createConfirmed().copy(
                        cancellation =
                            ReservationCancellationAvailabilityUiModel.Unavailable(
                                ReservationCancellationUnavailableReasonUiModel.DEADLINE_PASSED,
                            ),
                    ),
                cancellationDialog = waiting,
            )
        }
    }

    @Test
    fun `대기와 실패는 닫을 수 있고 제출 중에는 모든 dismiss를 차단한다`() {
        val failed =
            ReservationCancellationDialogUiState.Failed(
                ReservationCancellationErrorUiModel.NETWORK,
            )

        assertTrue(ReservationCancellationDialogUiState.Waiting.canDismiss)
        assertTrue(failed.canDismiss)
        assertFalse(ReservationCancellationDialogUiState.Submitting.canDismiss)
    }

    @Test
    fun `확인과 재시도 Action은 취소 대상 ReservationId를 유지한다`() {
        val reservationId = ReservationId("reservation-to-cancel")

        val confirm = ReservationDetailAction.ConfirmCancellation(reservationId)
        val retry = ReservationDetailAction.RetryCancellation(reservationId)

        assertEquals(reservationId, confirm.reservationId)
        assertEquals(reservationId, retry.reservationId)
    }

    @Test
    fun `취소 완료는 modal을 가질 수 없는 별도 결과 상태다`() {
        val state = ReservationDetailUiState.CancellationCompleted(createCancellationResult())

        assertEquals(ReservationId("reservation-confirmed"), state.result.reservationId)
        assertEquals(1, state.result.restoredPassUses)
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

    private fun createClassCancelled(): ReservationDetailUiModel.ClassCancelled =
        ReservationDetailUiModel.ClassCancelled(
            reservationId = ReservationId("reservation-class-cancelled"),
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

    private fun createCancellationResult(): ReservationCancellationResultUiModel =
        ReservationCancellationResultUiModel(
            reservationId = ReservationId("reservation-confirmed"),
            title = "체어 밸런스",
            classInfo = createClassInfo(),
            cancelledAtLabel = "2026.08.01 (토) 오후 3:25",
            restoredPassUses = 1,
        )
}
