package com.classitda.feature.student.reservation.classreservation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ClassReservationRoute(
    classId: String,
    onBackClick: () -> Unit,
    onReservationComplete: (String) -> Unit,
) {
    val viewModel =
        koinViewModel<ClassReservationViewModel>(
            key = classId,
            parameters = { parametersOf(classId) },
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
        onReservationClick = { onReservationComplete(uiState.classId) },
    )
}
