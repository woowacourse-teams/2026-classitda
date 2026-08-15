package com.classitda.feature.student.reservation.waitlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun WaitlistReservationRoute(
    classId: String,
    onBackClick: () -> Unit,
    onWaitlistComplete: (String) -> Unit,
) {
    val viewModel =
        koinViewModel<WaitlistReservationViewModel>(
            key = classId,
            parameters = { parametersOf(classId) },
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WaitlistReservationScreen(
        selectedClass = uiState.selectedClass,
        classPasses = uiState.classPasses,
        selectedPassId = uiState.selectedPassId,
        expectedWaitingNumber = uiState.expectedWaitingNumber,
        onBackClick = onBackClick,
        onPassClick = viewModel::onPassClick,
        onApplyClick = { onWaitlistComplete(uiState.classId) },
    )
}
