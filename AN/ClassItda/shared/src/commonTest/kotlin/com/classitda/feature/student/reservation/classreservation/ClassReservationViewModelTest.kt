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
