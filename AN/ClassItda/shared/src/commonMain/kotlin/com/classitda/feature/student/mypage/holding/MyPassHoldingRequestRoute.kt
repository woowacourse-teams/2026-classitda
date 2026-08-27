package com.classitda.feature.student.mypage.holding

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingCompletedUiModel
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun MyPassHoldingRequestRoute(
    passId: String,
    passName: String,
    currentExpireDate: LocalDate,
    onNavigateBack: () -> Unit,
    onCompleted: (MyPassHoldingCompletedUiModel) -> Unit,
) {
    val viewModel =
        koinViewModel<MyPassHoldingRequestViewModel>(
            key = passId,
            parameters = { parametersOf(MyPassHoldingRequestArgs(passId, passName, currentExpireDate)) },
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.completedEvent.collect { completed -> onCompleted(completed) }
    }
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    MyPassHoldingRequestScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onStartDateClick = viewModel::onStartDateClick,
        onEndDateClick = viewModel::onEndDateClick,
        onMemoChanged = viewModel::onMemoChanged,
        onCancelClick = onNavigateBack,
        onSubmitClick = viewModel::onSubmitClick,
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss,
        snackbarHostState = snackbarHostState,
    )
}
