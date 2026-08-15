package com.classitda.feature.student.reservation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ReservationRoute(
    onClassReservationClick: (String) -> Unit,
    onWaitlistReservationClick: (String) -> Unit,
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
        onDayClick = viewModel::onDayClick,
        onPreviousClick = viewModel::onPreviousMonthClick,
        onNextClick = viewModel::onNextMonthClick,
        onMonthModeChange = viewModel::onMonthModeChange,
        onTodayClick = viewModel::onTodayClick,
        onClassButtonClick = { classId ->
            val selectedClass = uiState.classes.first { it.id == classId }
            if (selectedClass.leftStudentCount == 0) {
                onWaitlistReservationClick(classId)
            } else {
                onClassReservationClick(classId)
            }
        },
    )
}
