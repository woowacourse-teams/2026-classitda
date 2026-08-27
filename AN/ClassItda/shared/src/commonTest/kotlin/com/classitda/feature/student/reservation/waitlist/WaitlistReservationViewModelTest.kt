package com.classitda.feature.student.reservation.waitlist

import com.classitda.data.repository.waitlist.FakeWaitlistReservationRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class WaitlistReservationViewModelTest {
    @Test
    fun `Route에서 받은 ID와 예상 대기 번호가 상세 상태에 유지된다`() {
        val viewModel =
            WaitlistReservationViewModel(
                classId = "2",
                initialPassId = "pass-1",
                repository = FakeWaitlistReservationRepository(),
            )

        assertEquals("2", viewModel.uiState.value.classId)
        assertEquals("2", viewModel.uiState.value.selectedClass.id)
        assertEquals(3, viewModel.uiState.value.expectedWaitingNumber)
    }

    @Test
    fun `Route에서 받은 수업 ID에 해당하는 대기 상세 정보가 표시된다`() {
        val viewModel =
            WaitlistReservationViewModel(
                classId = "14",
                initialPassId = "pass-1",
                repository = FakeWaitlistReservationRepository(),
            )

        assertEquals("체어 밸런스", viewModel.uiState.value.selectedClass.className)
        assertEquals("2026.09.02 (수)", viewModel.uiState.value.selectedClass.dateText)
        assertEquals("오후 7:30 - 8:20", viewModel.uiState.value.selectedClass.timeText)
    }
}
