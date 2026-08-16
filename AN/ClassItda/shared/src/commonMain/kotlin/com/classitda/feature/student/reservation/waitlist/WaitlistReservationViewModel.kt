package com.classitda.feature.student.reservation.waitlist

import androidx.lifecycle.ViewModel
import com.classitda.domain.repository.waitlist.WaitlistReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class WaitlistReservationUiState(
    val classId: String,
    val selectedClass: WaitlistClassUiModel,
    val classPasses: List<WaitlistClassPassUiModel>,
    val expectedWaitingNumber: Int,
    val selectedPassId: String? = null,
)

internal class WaitlistReservationViewModel(
    classId: String,
    repository: WaitlistReservationRepository,
) : ViewModel() {
    private val reservation = repository.getWaitlistReservation(classId)
    private val _uiState =
        MutableStateFlow(
            WaitlistReservationUiState(
                classId = classId,
                selectedClass =
                    WaitlistClassUiModel(
                        id = reservation.id,
                        className = reservation.className,
                        dateText = reservation.dateText,
                        timeText = reservation.timeText,
                        instructorName = reservation.instructorName,
                        roomName = reservation.roomName,
                        memoText = reservation.memoText,
                        cancellationNotice = reservation.cancellationNotice,
                    ),
                classPasses =
                    reservation.classPasses.map { classPass ->
                        WaitlistClassPassUiModel(
                            id = classPass.id,
                            name = classPass.name,
                            usageText = classPass.usageText,
                            expirationText = classPass.expirationText,
                        )
                    },
                expectedWaitingNumber = reservation.expectedWaitingNumber,
                selectedPassId = reservation.classPasses.firstOrNull()?.id,
            ),
        )
    val uiState: StateFlow<WaitlistReservationUiState> = _uiState.asStateFlow()

    fun onPassClick(passId: String) {
        _uiState.value = _uiState.value.copy(selectedPassId = passId)
    }
}
