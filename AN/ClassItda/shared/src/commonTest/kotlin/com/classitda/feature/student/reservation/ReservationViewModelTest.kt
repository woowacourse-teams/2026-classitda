package com.classitda.feature.student.reservation

import com.classitda.data.repository.reservation.FakeReservationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReservationViewModelTest {
    @Test
    fun `수업 목록은 String 타입 ID를 유지한다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository())

        assertEquals(
            "1",
            viewModel.uiState.value.classes
                .first()
                .id,
        )
    }

    @Test
    fun `월간 보기 선택은 단일 UiState에 반영된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository())

        viewModel.onMonthModeChange(true)

        assertTrue(viewModel.uiState.value.isMonthMode)
    }

    @Test
    fun `수강권을 선택하고 바텀시트를 닫으면 단일 UiState에 반영된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository())

        viewModel.onPassClick("pass-2")
        viewModel.hidePassSelection()

        assertEquals("pass-2", viewModel.uiState.value.selectedPassId)
        assertTrue(!viewModel.uiState.value.isPassSelectionVisible)
    }
}
