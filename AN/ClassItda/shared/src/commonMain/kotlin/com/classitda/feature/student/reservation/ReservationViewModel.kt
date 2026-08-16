package com.classitda.feature.student.reservation

import androidx.lifecycle.ViewModel
import com.classitda.domain.repository.reservation.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class ReservationUiState(
    val year: Int = 2026,
    val month: Int = 8,
    val selectedDayOfMonth: Int = 8,
    val todayDayOfMonth: Int = 5,
    val confirmedReservationDays: Set<Int> = setOf(7, 8),
    val waitlistReservationDays: Set<Int> = setOf(9),
    val isMonthMode: Boolean = false,
    val classes: List<ReservationClassUiModel> = emptyList(),
    val passes: List<ReservationPassUiModel> = emptyList(),
    val selectedPassId: String? = null,
    val isPassSelectionVisible: Boolean = true,
)

internal class ReservationViewModel(
    reservationRepository: ReservationRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            ReservationUiState(
                classes =
                    reservationRepository.getClasses().map { reservationClass ->
                        ReservationClassUiModel(
                            id = reservationClass.id,
                            classTime = reservationClass.classTime,
                            className = reservationClass.className,
                            instructorName = reservationClass.instructorName,
                            roomName = reservationClass.roomName,
                            leftStudentCount = reservationClass.leftStudentCount,
                            cardType =
                                when {
                                    reservationClass.isReserved -> ReservationClassCardType.RESERVED
                                    reservationClass.isWaitlisted -> ReservationClassCardType.WAITLISTED
                                    else -> ReservationClassCardType.DEFAULT
                                },
                        )
                    },
                passes =
                    reservationRepository.getPasses().map { pass ->
                        ReservationPassUiModel(
                            id = pass.id,
                            name = pass.name,
                            remainingText = pass.remainingText,
                            expirationText = pass.expirationText,
                        )
                    },
                selectedPassId = reservationRepository.getPasses().firstOrNull()?.id,
            ),
        )
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    fun onDayClick(dayOfMonth: Int) {
        _uiState.value = _uiState.value.copy(selectedDayOfMonth = dayOfMonth)
    }

    fun onMonthModeChange(isMonthMode: Boolean) {
        _uiState.value = _uiState.value.copy(isMonthMode = isMonthMode)
    }

    fun onPreviousMonthClick() {
        val current = _uiState.value
        _uiState.value =
            if (current.month == 1) {
                current.copy(year = current.year - 1, month = 12, selectedDayOfMonth = 1)
            } else {
                current.copy(month = current.month - 1, selectedDayOfMonth = 1)
            }
    }

    fun onNextMonthClick() {
        val current = _uiState.value
        _uiState.value =
            if (current.month == 12) {
                current.copy(year = current.year + 1, month = 1, selectedDayOfMonth = 1)
            } else {
                current.copy(month = current.month + 1, selectedDayOfMonth = 1)
            }
    }

    fun onTodayClick() {
        _uiState.value =
            _uiState.value.copy(
                selectedDayOfMonth = _uiState.value.todayDayOfMonth,
            )
    }

    fun onPassClick(passId: String) {
        _uiState.value = _uiState.value.copy(selectedPassId = passId)
    }

    fun showPassSelection() {
        _uiState.value = _uiState.value.copy(isPassSelectionVisible = true)
    }

    fun hidePassSelection() {
        _uiState.value = _uiState.value.copy(isPassSelectionVisible = false)
    }
}
