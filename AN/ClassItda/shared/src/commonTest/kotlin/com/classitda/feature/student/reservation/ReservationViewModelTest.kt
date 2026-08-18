package com.classitda.feature.student.reservation

import com.classitda.data.repository.reservation.FakeReservationRepository
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReservationViewModelTest {
    @Test
    fun `주입한 오늘 날짜로 캘린더가 시작된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        assertEquals(previewToday, viewModel.uiState.value.today)
        assertEquals(2026, viewModel.uiState.value.year)
        assertEquals(8, viewModel.uiState.value.month)
        assertEquals(5, viewModel.uiState.value.selectedDayOfMonth)
    }

    @Test
    fun `수업 목록은 String 타입 ID를 유지한다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onDayClick(8)

        assertEquals(
            "1",
            viewModel.uiState.value.classes
                .first()
                .id,
        )
    }

    @Test
    fun `월간 보기 선택은 단일 UiState에 반영된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onMonthModeChange(true)

        assertTrue(viewModel.uiState.value.isMonthMode)
    }

    @Test
    fun `날짜를 선택하면 해당 날짜의 수업만 표시된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onDayClick(7)

        assertEquals(
            listOf("3"),
            viewModel.uiState.value.classes
                .map { it.id },
        )

        viewModel.onDayClick(9)

        assertEquals(
            listOf("4"),
            viewModel.uiState.value.classes
                .map { it.id },
        )
    }

    @Test
    fun `초록 점은 예약 확정 수업이 있는 날짜에만 표시된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        assertEquals(setOf(7, 20, 28), viewModel.uiState.value.confirmedReservationDays)

        viewModel.onNextMonthClick()

        assertEquals(setOf(5, 16, 28), viewModel.uiState.value.confirmedReservationDays)
    }

    @Test
    fun `주황 점은 대기 중인 수업이 있는 날짜에만 표시된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        assertEquals(setOf(9, 22, 30), viewModel.uiState.value.waitlistReservationDays)

        viewModel.onNextMonthClick()

        assertEquals(setOf(9, 20, 30), viewModel.uiState.value.waitlistReservationDays)
    }

    @Test
    fun `9월 날짜를 선택하면 해당 날짜의 수업만 표시된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onNextMonthClick()
        viewModel.onDayClick(2)

        assertEquals(
            listOf("13", "14"),
            viewModel.uiState.value.classes
                .map { it.id },
        )
    }

    @Test
    fun `오늘 이전 날짜는 선택되지 않는다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onDayClick(4)

        assertEquals(5, viewModel.uiState.value.selectedDayOfMonth)
        assertEquals(
            listOf("weekday-8-5"),
            viewModel.uiState.value.classes
                .map { it.id },
        )
    }

    @Test
    fun `현재 달보다 이전 달로 이동할 수 없다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onPreviousMonthClick()

        assertEquals(2026, viewModel.uiState.value.year)
        assertEquals(8, viewModel.uiState.value.month)

        viewModel.onNextMonthClick()
        viewModel.onPreviousMonthClick()

        assertEquals(2026, viewModel.uiState.value.year)
        assertEquals(8, viewModel.uiState.value.month)
    }

    @Test
    fun `수강권을 선택하고 바텀시트를 닫으면 단일 UiState에 반영된다`() {
        val viewModel = ReservationViewModel(FakeReservationRepository(), previewToday)

        viewModel.onPassClick("pass-2")
        viewModel.hidePassSelection()

        assertEquals("pass-2", viewModel.uiState.value.selectedPassId)
        assertTrue(!viewModel.uiState.value.isPassSelectionVisible)
    }

    private companion object {
        val previewToday = LocalDate(2026, 8, 5)
    }
}
