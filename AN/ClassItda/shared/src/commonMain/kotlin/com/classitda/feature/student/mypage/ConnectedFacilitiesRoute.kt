package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ConnectedFacilitiesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConnectedFacilitiesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConnectedFacilitiesScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ConnectedFacilitiesAction.Back -> onBack()
                ConnectedFacilitiesAction.Retry -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}
