package com.classitda.feature.instructor.management.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.component.NavigateBackTopBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClassListRoute(
    onBackClick: () -> Unit,
    onCreateSessionClick: () -> Unit,
    onSessionCardClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    shouldRefresh: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: ClassListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.onRetry()
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshErrors.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    when (val state = uiState) {
        is ClassListUiState.InitialLoading -> {
            ClassListLoadingRoute(onBackClick = onBackClick, bottomBar = bottomBar, modifier = modifier)
        }

        is ClassListUiState.Success -> {
            ClassListScreen(
                sessionGroups = state.content.sessionGroups,
                customCategories = state.content.customCategories,
                selectedFilterLabel = state.content.selectedFilterLabel,
                isRefreshing = state.isRefreshing,
                snackbarHostState = snackbarHostState,
                onFilterSelected = viewModel::onFilterSelected,
                onBackClick = onBackClick,
                onCreateSessionClick = onCreateSessionClick,
                onSessionCardClick = onSessionCardClick,
                bottomBar = bottomBar,
                modifier = modifier,
            )
        }

        is ClassListUiState.Error -> {
            ClassListErrorRoute(
                message = state.message,
                onRetry = viewModel::onRetry,
                onBackClick = onBackClick,
                bottomBar = bottomBar,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClassListLoadingRoute(
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 목록",
            )
        },
        bottomBar = bottomBar,
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = InsColors.Primary)
        }
    }
}

@Composable
private fun ClassListErrorRoute(
    message: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 목록",
            )
        },
        bottomBar = bottomBar,
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message ?: "수업 정보를 불러오지 못했어요",
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextSecondary,
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
                modifier = Modifier.padding(top = AppSpacing.lg),
            ) {
                Text(text = "다시 시도")
            }
        }
    }
}
