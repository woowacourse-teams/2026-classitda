package com.classitda.feature.student.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.common.profile.ProfileViewScreen
import com.classitda.feature.common.profile.contract.ProfileViewAction
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProfileViewRoute(
    onBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onRequestLogout: () -> Unit,
    onRequestWithdrawal: () -> Unit,
    modifier: Modifier = Modifier,
    refreshToken: Int = 0,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshToken) {
        if (refreshToken > 0) {
            viewModel.refresh()
        }
    }

    ProfileViewScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ProfileViewAction.Back -> onBack()
                ProfileViewAction.Retry -> viewModel.onAction(action)
                ProfileViewAction.OpenEdit -> onOpenEdit()
                ProfileViewAction.RequestLogout -> onRequestLogout()
                ProfileViewAction.RequestWithdrawal -> onRequestWithdrawal()
            }
        },
        modifier = modifier,
    )
}
