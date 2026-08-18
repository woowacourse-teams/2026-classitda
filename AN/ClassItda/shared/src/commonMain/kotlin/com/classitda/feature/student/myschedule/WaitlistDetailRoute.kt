package com.classitda.feature.student.myschedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun WaitlistDetailRoute(
    waitlistId: WaitlistId,
    onBack: () -> Unit,
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
) {
    val viewModel =
        koinViewModel<WaitlistDetailViewModel>(
            key = waitlistId.value,
            parameters = { parametersOf(waitlistId) },
        )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WaitlistDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack =
            if (state is WaitlistDetailUiState.CancellationCompleted) {
                onReturnToList
            } else {
                onBack
            },
        onBookAnotherClass = onBookAnotherClass,
        onReturnToList = onReturnToList,
    )
}
