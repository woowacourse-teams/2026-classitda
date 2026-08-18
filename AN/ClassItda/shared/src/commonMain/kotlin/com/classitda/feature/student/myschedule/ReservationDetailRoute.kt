package com.classitda.feature.student.myschedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ReservationDetailRoute(
    reservationId: ReservationId,
    onBack: () -> Unit,
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
) {
    val viewModel =
        koinViewModel<ReservationDetailViewModel>(
            key = reservationId.value,
            parameters = { parametersOf(reservationId) },
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ReservationDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack =
            if (state is ReservationDetailUiState.CancellationCompleted) {
                onReturnToList
            } else {
                onBack
            },
        onBookAnotherClass = onBookAnotherClass,
        onReturnToList = onReturnToList,
    )
}
