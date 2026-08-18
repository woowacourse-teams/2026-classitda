package com.classitda.feature.student.reservation.classreservation

import androidx.lifecycle.ViewModel
import com.classitda.domain.model.classreservation.ReservationRequestResult
import com.classitda.domain.repository.classreservation.ClassReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ClassReservationUiState(
    val classId: String,
    val selectedClass: SelectedClassUiModel,
    val classPasses: List<ClassPassUiModel>,
    val selectedPassId: String? = null,
    val isTermsAgreed: Boolean = false,
    val timeConflict: ReservationTimeConflictUiModel? = null,
)

internal class ClassReservationViewModel(
    classId: String,
    initialPassId: String,
    repository: ClassReservationRepository,
) : ViewModel() {
    private val reservationRepository = repository
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
                        memoText = reservation.memoText,
                        cancellationNotice = reservation.cancellationNotice,
                    ),
                classPasses =
                    reservation.classPasses.map { classPass ->
                        ClassPassUiModel(
                            id = classPass.id,
                            name = classPass.name,
                            usageText = classPass.usageText,
                            validityPeriodText = classPass.validityPeriodText,
                        )
                    },
                selectedPassId =
                    initialPassId.takeIf { passId -> reservation.classPasses.any { it.id == passId } }
                        ?: reservation.classPasses.firstOrNull()?.id,
            ),
        )
    val uiState: StateFlow<ClassReservationUiState> = _uiState.asStateFlow()

    fun onPassClick(passId: String) {
        _uiState.value = _uiState.value.copy(selectedPassId = passId)
    }

    fun onTermsAgreementChange(isAgreed: Boolean) {
        _uiState.value = _uiState.value.copy(isTermsAgreed = isAgreed)
    }

    fun submitReservation(): ReservationRequestResult {
        val passId = _uiState.value.selectedPassId
            ?: return ReservationRequestResult.Failure("사용할 수강권을 선택해 주세요.")
        val result = reservationRepository.reserve(_uiState.value.classId, passId)
        if (result is ReservationRequestResult.TimeConflict) {
            _uiState.value =
                _uiState.value.copy(
                    timeConflict =
                        ReservationTimeConflictUiModel(
                            className = result.className,
                            dateTimeText = result.dateTimeText,
                            studioName = result.studioName,
                        ),
                )
        }
        return result
    }

    fun dismissTimeConflict() {
        _uiState.value = _uiState.value.copy(timeConflict = null)
    }
}
