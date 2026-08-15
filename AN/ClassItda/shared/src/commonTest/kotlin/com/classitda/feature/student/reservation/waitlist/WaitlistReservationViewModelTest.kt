package com.classitda.feature.student.reservation.waitlist

import com.classitda.feature.student.reservation.data.repository.waitlist.FakeWaitlistReservationRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class WaitlistReservationViewModelTest {
    @Test
    fun `Route에서 받은 ID와 예상 대기 번호가 상세 상태에 유지된다`() {
        val viewModel =
            WaitlistReservationViewModel(
                classId = "2",
                repository = FakeWaitlistReservationRepository(),
            )

        assertEquals("2", viewModel.uiState.value.classId)
        assertEquals("2", viewModel.uiState.value.selectedClass.id)
        assertEquals(3, viewModel.uiState.value.expectedWaitingNumber)
    }
}
