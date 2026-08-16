package com.classitda.feature.student.reservation.classreservation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.domain.model.classreservation.ReservationRequestResult
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ClassReservationRoute(
    classId: String,
    initialPassId: String,
    onBackClick: () -> Unit,
    onReservationComplete: (String, String) -> Unit,
    onReservationFailure: (String, String) -> Unit,
    onScheduleClick: () -> Unit,
) {
    val viewModel =
        koinViewModel<ClassReservationViewModel>(
            key = classId,
            parameters = { parametersOf(classId, initialPassId) },
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ClassReservationScreen(
        selectedClass = uiState.selectedClass,
        classPasses = uiState.classPasses,
        selectedPassId = uiState.selectedPassId,
        isTermsAgreed = uiState.isTermsAgreed,
        onBackClick = onBackClick,
        onPassClick = viewModel::onPassClick,
        onTermsAgreementChange = viewModel::onTermsAgreementChange,
        timeConflict = uiState.timeConflict,
        onTimeConflictDismiss = viewModel::dismissTimeConflict,
        onScheduleClick = onScheduleClick,
        onReservationClick = {
            when (val result = viewModel.submitReservation()) {
                ReservationRequestResult.Success ->
                    onReservationComplete(uiState.classId, requireNotNull(uiState.selectedPassId))
                is ReservationRequestResult.Failure ->
                    onReservationFailure("수업 예약에 실패했습니다.", result.message)
                is ReservationRequestResult.TimeConflict -> Unit
            }
        },
    )
}
