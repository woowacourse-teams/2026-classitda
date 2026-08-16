package com.classitda.feature.student.reservation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ReservationRoute(
    onClassReservationClick: (String, String) -> Unit,
    onWaitlistReservationClick: (String, String) -> Unit,
    viewModel: ReservationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReservationScreen(
        year = uiState.year,
        month = uiState.month,
        selectedDayOfMonth = uiState.selectedDayOfMonth,
        todayDayOfMonth = uiState.todayDayOfMonth,
        confirmedReservationDays = uiState.confirmedReservationDays,
        waitlistReservationDays = uiState.waitlistReservationDays,
        isMonthMode = uiState.isMonthMode,
        classes = uiState.classes,
        passes = uiState.passes,
        selectedPassId = uiState.selectedPassId,
        isPassSelectionVisible = uiState.isPassSelectionVisible,
        onPassClick = viewModel::onPassClick,
        onPassSelectionDismiss = viewModel::hidePassSelection,
        onPassSelectionClick = viewModel::showPassSelection,
        onDayClick = viewModel::onDayClick,
        onPreviousClick = viewModel::onPreviousMonthClick,
        onNextClick = viewModel::onNextMonthClick,
        onMonthModeChange = viewModel::onMonthModeChange,
        onTodayClick = viewModel::onTodayClick,
        onClassButtonClick = { classId ->
            uiState.selectedPassId?.let { passId ->
                val selectedClass = uiState.classes.first { it.id == classId }
                if (selectedClass.leftStudentCount == 0) {
                    onWaitlistReservationClick(classId, passId)
                } else {
                    onClassReservationClick(classId, passId)
                }
            }
        },
    )
}
