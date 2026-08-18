package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.WaitlistId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class WaitlistDetailContractTest {
    @Test
    fun `로딩 콘텐츠 오류는 동시에 표현되지 않는 배타적 상태다`() {
        val states =
            listOf(
                WaitlistDetailUiState.Loading,
                WaitlistDetailUiState.Content(createDetail()),
                WaitlistDetailUiState.Error(WaitlistDetailErrorUiModel.NETWORK),
            )

        assertIs<WaitlistDetailUiState.Loading>(states[0])
        assertIs<WaitlistDetailUiState.Content>(states[1])
        assertIs<WaitlistDetailUiState.Error>(states[2])
    }

    @Test
    fun `현재 대기 순번은 1을 허용하고 0과 음수를 거부한다`() {
        createDetail().copy(currentPosition = 1)

        assertFailsWith<IllegalArgumentException> {
            createDetail().copy(currentPosition = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            createDetail().copy(currentPosition = -1)
        }
    }

    @Test
    fun `취소 가능한 상세는 같은 WaitlistId의 취소 Action을 제공한다`() {
        val detail = createDetail()

        val action = detail.cancellationActionOrNull()

        assertEquals(
            WaitlistDetailAction.CancelWaitlist(detail.waitlistId),
            action,
        )
    }

    @Test
    fun `취소 불가 상세는 취소 Action을 제공하지 않는다`() {
        val detail =
            createDetail().copy(
                cancellation =
                    WaitlistCancellationAvailabilityUiModel.Unavailable(
                        WaitlistCancellationUnavailableReasonUiModel.DEADLINE_PASSED,
                    ),
            )

        assertNull(detail.cancellationActionOrNull())
    }

    @Test
    fun `수강권 횟수는 0을 허용하고 음수를 거부한다`() {
        createPass().copy(
            remainingUses = 0,
            reservableUses = 0,
            cancellableUses = 0,
        )

        assertFailsWith<IllegalArgumentException> {
            createPass().copy(remainingUses = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            createPass().copy(reservableUses = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            createPass().copy(cancellableUses = -1)
        }
    }

    private fun createDetail(): WaitlistDetailUiModel =
        WaitlistDetailUiModel(
            waitlistId = WaitlistId("waitlist-detail"),
            title = "체어 밸런스",
            appliedAtLabel = "2026.08.01 (토) 오후 3:20",
            currentPosition = 2,
            classInfo =
                WaitlistClassInfoUiModel(
                    dateLabel = "2026.08.04 (화)",
                    timeRangeLabel = "오후 6:30 ~ 7:20",
                    memo = "수업 메모",
                    instructorName = "박소연 강사",
                    facilityName = "클래스잇다 금토동지점",
                ),
            pass = createPass(),
            cancellation = WaitlistCancellationAvailabilityUiModel.Available,
        )

    private fun createPass(): WaitlistPassAvailabilityUiModel =
        WaitlistPassAvailabilityUiModel(
            name = "[8:1] 그룹 레슨 20회권",
            validityLabel = "2026.06.30 ~ 2026.08.20",
            remainingUses = 14,
            reservableUses = 5,
            cancellableUses = 2,
        )
}
