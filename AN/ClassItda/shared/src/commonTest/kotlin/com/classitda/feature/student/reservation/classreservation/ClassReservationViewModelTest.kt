package com.classitda.feature.student.reservation.classreservation

import com.classitda.data.repository.classreservation.FakeClassReservationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassReservationViewModelTest {
    @Test
    fun `Route에서 받은 ID가 상세 상태에 유지된다`() {
        val viewModel =
            ClassReservationViewModel(
                classId = "1",
                initialPassId = "pass-1",
                repository = FakeClassReservationRepository(),
            )

        assertEquals("1", viewModel.uiState.value.classId)
        assertEquals("1", viewModel.uiState.value.selectedClass.id)
    }

    @Test
    fun `Route에서 받은 수업 ID에 해당하는 상세 정보가 표시된다`() {
        val viewModel =
            ClassReservationViewModel(
                classId = "13",
                initialPassId = "pass-1",
                repository = FakeClassReservationRepository(),
            )

        assertEquals("리포머 베이직", viewModel.uiState.value.selectedClass.className)
        assertEquals("2026.09.02 (수)", viewModel.uiState.value.selectedClass.dateText)
        assertEquals("오전 10:00 - 10:50", viewModel.uiState.value.selectedClass.timeText)
    }

    @Test
    fun `수강권과 약관을 선택하면 상태가 갱신된다`() {
        val viewModel =
            ClassReservationViewModel(
                classId = "1",
                initialPassId = "pass-1",
                repository = FakeClassReservationRepository(),
            )

        viewModel.onPassClick("pass-1")
        viewModel.onTermsAgreementChange(true)

        assertEquals("pass-1", viewModel.uiState.value.selectedPassId)
        assertTrue(viewModel.uiState.value.isTermsAgreed)
    }
}
