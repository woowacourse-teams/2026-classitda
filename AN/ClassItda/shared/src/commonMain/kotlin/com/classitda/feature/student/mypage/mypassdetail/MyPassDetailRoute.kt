package com.classitda.feature.student.mypage.mypassdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassDetailUiState
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun MyPassDetailRoute(
    passId: String,
    onNavigateBack: () -> Unit,
    onHoldRequestClick: (passId: String, passName: String, currentExpireDate: LocalDate) -> Unit,
) {
    val viewModel =
        koinViewModel<MyPassDetailViewModel>(
            key = passId,
            parameters = { parametersOf(passId) },
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyPassDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onHoldRequestClick = {
            val content = uiState as? MyPassDetailUiState.Content
            val currentExpireDate = content?.detail?.currentExpireDate
            if (content != null && currentExpireDate != null) {
                onHoldRequestClick(passId, content.detail.title, currentExpireDate)
            }
        },
        onRetry = viewModel::onRetry,
    )
}
