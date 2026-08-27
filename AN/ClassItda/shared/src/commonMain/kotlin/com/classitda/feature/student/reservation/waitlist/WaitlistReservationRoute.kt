package com.classitda.feature.student.reservation.waitlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun WaitlistReservationRoute(
    classId: String,
    initialPassId: String,
    onBackClick: () -> Unit,
    onWaitlistComplete: (String, String) -> Unit,
    onWaitlistFailure: (String, String) -> Unit,
) {
    val viewModel =
        koinViewModel<WaitlistReservationViewModel>(
            key = classId,
            parameters = { parametersOf(classId, initialPassId) },
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WaitlistReservationScreen(
        selectedClass = uiState.selectedClass,
        classPasses = uiState.classPasses,
        selectedPassId = uiState.selectedPassId,
        expectedWaitingNumber = uiState.expectedWaitingNumber,
        onBackClick = onBackClick,
        onPassClick = viewModel::onPassClick,
        onApplyClick = {
            if (viewModel.submitWaitlist()) {
                onWaitlistComplete(uiState.classId, requireNotNull(uiState.selectedPassId))
            } else {
                onWaitlistFailure(
                    "수업 대기 요청이 실패했습니다.",
                    "대기 신청 가능 시간이 종료되었습니다.",
                )
            }
        },
    )
}
