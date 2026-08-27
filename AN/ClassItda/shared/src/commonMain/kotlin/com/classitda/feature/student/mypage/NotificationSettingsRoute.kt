package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.mypage.contract.NotificationSettingsAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NotificationSettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationSettingsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                NotificationSettingsAction.Back -> onBack()

                NotificationSettingsAction.Retry,
                is NotificationSettingsAction.Toggle,
                -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}
