package com.classitda.feature.student.reservation

import androidx.lifecycle.ViewModel
import com.classitda.domain.model.reservation.ReservationClass
import com.classitda.domain.repository.reservation.ReservationRepository
import com.classitda.feature.student.reservation.contract.ReservationClassCardType
import com.classitda.feature.student.reservation.contract.ReservationClassUiModel
import com.classitda.feature.student.reservation.contract.ReservationPassUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal data class ReservationUiState(
    val year: Int,
    val month: Int,
    val selectedDayOfMonth: Int,
    val today: LocalDate,
    val confirmedReservationDays: Set<Int> = emptySet(),
    val waitlistReservationDays: Set<Int> = emptySet(),
    val isMonthMode: Boolean = false,
    val classes: List<ReservationClassUiModel> = emptyList(),
    val passes: List<ReservationPassUiModel> = emptyList(),
    val selectedPassId: String? = null,
    val isPassSelectionVisible: Boolean = true,
)

internal class ReservationViewModel(
    reservationRepository: ReservationRepository,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
) : ViewModel() {
    private val reservationClasses = reservationRepository.getClasses()

    private val _uiState =
        MutableStateFlow(
            ReservationUiState(
                year = today.year,
                month = today.month.number,
                selectedDayOfMonth = today.day,
                today = today,
                classes = classesForDate(today.year, today.month.number, today.day),
                confirmedReservationDays = confirmedReservationDaysForMonth(today.year, today.month.number),
                waitlistReservationDays = waitlistReservationDaysForMonth(today.year, today.month.number),
                passes =
                    reservationRepository.getPasses().map { pass ->
                        ReservationPassUiModel(
                            id = pass.id,
                            name = pass.name,
                            remainingText = pass.remainingText,
                            validityPeriodText = pass.validityPeriodText,
                        )
                    },
                selectedPassId = reservationRepository.getPasses().firstOrNull()?.id,
            ),
        )
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    fun onDayClick(dayOfMonth: Int) {
        val current = _uiState.value
        val selectedDate = LocalDate(current.year, current.month, dayOfMonth)
        if (selectedDate < current.today) return

        _uiState.value =
            current.copy(
                selectedDayOfMonth = dayOfMonth,
                classes = classesForDate(current.year, current.month, dayOfMonth),
            )
    }

    fun onMonthModeChange(isMonthMode: Boolean) {
        _uiState.value = _uiState.value.copy(isMonthMode = isMonthMode)
    }

    fun onPreviousMonthClick() {
        val current = _uiState.value
        val isCurrentMonth =
            current.year == current.today.year &&
                current.month == current.today.month.number
        if (isCurrentMonth) return

        val updated =
            if (current.month == 1) {
                current.copy(year = current.year - 1, month = 12, selectedDayOfMonth = 1)
            } else {
                current.copy(month = current.month - 1, selectedDayOfMonth = 1)
            }
        _uiState.value =
            updated.copy(
                classes = classesForDate(updated.year, updated.month, updated.selectedDayOfMonth),
                confirmedReservationDays = confirmedReservationDaysForMonth(updated.year, updated.month),
                waitlistReservationDays = waitlistReservationDaysForMonth(updated.year, updated.month),
            )
    }

    fun onNextMonthClick() {
        val current = _uiState.value
        val updated =
            if (current.month == 12) {
                current.copy(year = current.year + 1, month = 1, selectedDayOfMonth = 1)
            } else {
                current.copy(month = current.month + 1, selectedDayOfMonth = 1)
            }
        _uiState.value =
            updated.copy(
                classes = classesForDate(updated.year, updated.month, updated.selectedDayOfMonth),
                confirmedReservationDays = confirmedReservationDaysForMonth(updated.year, updated.month),
                waitlistReservationDays = waitlistReservationDaysForMonth(updated.year, updated.month),
            )
    }

    fun onTodayClick() {
        val current = _uiState.value
        _uiState.value =
            current.copy(
                year = current.today.year,
                month = current.today.month.number,
                selectedDayOfMonth = current.today.day,
                classes = classesForDate(current.today.year, current.today.month.number, current.today.day),
                confirmedReservationDays =
                    confirmedReservationDaysForMonth(
                        current.today.year,
                        current.today.month.number,
                    ),
                waitlistReservationDays =
                    waitlistReservationDaysForMonth(
                        current.today.year,
                        current.today.month.number,
                    ),
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

    private fun classesForDate(
        year: Int,
        month: Int,
        dayOfMonth: Int,
    ): List<ReservationClassUiModel> {
        val selectedDate = LocalDate(year, month, dayOfMonth)
        return reservationClasses
            .filter { it.date == selectedDate }
            .map { it.toUiModel() }
    }

    private fun confirmedReservationDaysForMonth(
        year: Int,
        month: Int,
    ): Set<Int> =
        reservationClasses
            .filter { reservationClass ->
                reservationClass.date.year == year &&
                    reservationClass.date.month.number == month &&
                    reservationClass.isReserved
            }.map { it.date.day }
            .toSet()

    private fun waitlistReservationDaysForMonth(
        year: Int,
        month: Int,
    ): Set<Int> =
        reservationClasses
            .filter { reservationClass ->
                reservationClass.date.year == year &&
                    reservationClass.date.month.number == month &&
                    reservationClass.isWaitlisted
            }.map { it.date.day }
            .toSet()

    private fun ReservationClass.toUiModel(): ReservationClassUiModel =
        ReservationClassUiModel(
            id = id,
            classTime = classTime,
            className = className,
            instructorName = instructorName,
            memo = roomName,
            leftStudentCount = leftStudentCount,
            cardType =
                when {
                    isReserved -> ReservationClassCardType.RESERVED
                    isWaitlisted -> ReservationClassCardType.WAITLISTED
                    else -> ReservationClassCardType.DEFAULT
                },
        )
}
