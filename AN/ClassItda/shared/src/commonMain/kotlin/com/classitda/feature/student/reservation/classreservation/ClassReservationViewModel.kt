package com.classitda.feature.student.reservation.classreservation

import androidx.lifecycle.ViewModel
import com.classitda.feature.student.reservation.domain.repository.classreservation.ClassReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ClassReservationUiState(
    val classId: String,
    val selectedClass: SelectedClassUiModel,
    val classPasses: List<ClassPassUiModel>,
    val selectedPassId: String? = null,
    val isTermsAgreed: Boolean = false,
)

internal class ClassReservationViewModel(
    classId: String,
    repository: ClassReservationRepository,
) : ViewModel() {
    private val reservation = repository.getClassReservation(classId)
    private val _uiState =
        MutableStateFlow(
            ClassReservationUiState(
                classId = classId,
                selectedClass =
                    SelectedClassUiModel(
                        id = reservation.id,
                        className = reservation.className,
                        dateText = reservation.dateText,
                        timeText = reservation.timeText,
                        instructorName = reservation.instructorName,
                        roomName = reservation.roomName,
                        cancellationNotice = reservation.cancellationNotice,
                    ),
                classPasses =
                    reservation.classPasses.map { classPass ->
                        ClassPassUiModel(
                            id = classPass.id,
                            name = classPass.name,
                            usageText = classPass.usageText,
                            expirationText = classPass.expirationText,
                        )
                    },
            ),
        )
    val uiState: StateFlow<ClassReservationUiState> = _uiState.asStateFlow()

    fun onPassClick(passId: String) {
        _uiState.value = _uiState.value.copy(selectedPassId = passId)
    }

    fun onTermsAgreementChange(isAgreed: Boolean) {
        _uiState.value = _uiState.value.copy(isTermsAgreed = isAgreed)
    }
}
