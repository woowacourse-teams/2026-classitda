package com.classitda.feature.student.reservation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.TopBar
import com.classitda.feature.student.reservation.component.ReservationCalendar
import com.classitda.feature.student.reservation.component.ReservationClassList
import com.classitda.feature.student.reservation.component.ReservationPassSelectionSheet
import com.classitda.feature.student.reservation.component.ReservationPassSelector
import com.classitda.feature.student.reservation.contract.ReservationClassUiModel
import com.classitda.feature.student.reservation.contract.ReservationPassUiModel
import com.classitda.feature.student.reservation.preview.ReservationScreenPreview
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ReservationScreen(
    onClassReservationClick: (String, String) -> Unit,
    onWaitlistReservationClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    viewModel: ReservationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReservationScreenContent(
        year = uiState.year,
        month = uiState.month,
        selectedDayOfMonth = uiState.selectedDayOfMonth,
        today = uiState.today,
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
        modifier = modifier,
        bottomBar = bottomBar,
    )
}

@Composable
internal fun ReservationScreenContent(
    year: Int,
    month: Int,
    selectedDayOfMonth: Int,
    today: LocalDate,
    confirmedReservationDays: Set<Int>,
    waitlistReservationDays: Set<Int>,
    isMonthMode: Boolean,
    classes: List<ReservationClassUiModel>,
    passes: List<ReservationPassUiModel>,
    selectedPassId: String?,
    isPassSelectionVisible: Boolean,
    onPassClick: (String) -> Unit,
    onPassSelectionDismiss: () -> Unit,
    onPassSelectionClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onClassButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            TopBar(
                title = "예약",
                hasBackground = true,
            )
        },
        bottomBar = bottomBar,
    ) { contentPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            item {
                ReservationPassSelector(
                    selectedPass = passes.firstOrNull { it.id == selectedPassId },
                    onClick = onPassSelectionClick,
                )
            }

            item {
                ReservationCalendar(
                    year = year,
                    month = month,
                    selectedDayOfMonth = selectedDayOfMonth,
                    today = today,
                    confirmedReservationDays = confirmedReservationDays,
                    waitlistReservationDays = waitlistReservationDays,
                    isMonthMode = isMonthMode,
                    onDayClick = onDayClick,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                    onMonthModeChange = onMonthModeChange,
                    onTodayClick = onTodayClick,
                )
            }

            item {
                ReservationClassList(
                    year = year,
                    month = month,
                    selectedDayOfMonth = selectedDayOfMonth,
                    classes = classes,
                    onClassButtonClick = onClassButtonClick,
                )
            }
        }
    }

    if (isPassSelectionVisible) {
        ReservationPassSelectionSheet(
            passes = passes,
            selectedPassId = selectedPassId,
            onPassClick = onPassClick,
            onDismissRequest = onPassSelectionDismiss,
        )
    }
}

@Preview(name = "주간 예약 화면")
@Composable
private fun ReservationScreenWeekPreview() {
    AppTheme {
        ReservationScreenPreview(initialMonthMode = false)
    }
}

@Preview(name = "월간 예약 화면")
@Composable
private fun ReservationScreenMonthPreview() {
    AppTheme {
        ReservationScreenPreview(initialMonthMode = true)
    }
}
