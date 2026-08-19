package com.classitda.feature.student.mypage.mypass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MyPassRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MyPassesViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPassScreen(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}
