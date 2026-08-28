package com.classitda.feature.instructor.management.classtemplates

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
internal fun ClassTemplateManagementRoute(
    onBackClick: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onTemplateCardClick: (String) -> Unit,
    onTemplateEditClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    shouldRefresh: Boolean = false,
    refreshKey: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: ClassTemplateManagementViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.onRetry()
    }

    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) viewModel.onRetry()
    }

    LaunchedEffect(viewModel) {
        viewModel.refreshErrors.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    when (val state = uiState) {
        is ClassTemplateManagementUiState.InitialLoading -> {
            ClassTemplateManagementLoadingRoute(onBackClick = onBackClick, bottomBar = bottomBar, modifier = modifier)
        }

        is ClassTemplateManagementUiState.Success -> {
            ClassTemplateManagementScreen(
                templates = state.content.templates,
                classTypes = state.content.classTypes,
                selectedFilter = state.content.selectedFilter,
                isRefreshing = state.isRefreshing,
                snackbarHostState = snackbarHostState,
                onFilterSelected = viewModel::onFilterSelected,
                onRefresh = viewModel::onRetry,
                onBackClick = onBackClick,
                onCreateTemplateClick = onCreateTemplateClick,
                onTemplateCardClick = onTemplateCardClick,
                onTemplateEditClick = onTemplateEditClick,
                onTemplateDeleteConfirmed = viewModel::deleteTemplate,
                bottomBar = bottomBar,
                modifier = modifier,
            )
        }

        is ClassTemplateManagementUiState.Error -> {
            ClassTemplateManagementErrorRoute(
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
private fun ClassTemplateManagementLoadingRoute(
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
                title = "수업 템플릿 관리",
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
private fun ClassTemplateManagementErrorRoute(
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
                title = "수업 템플릿 관리",
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
                text = message ?: "수업 템플릿 정보를 불러오지 못했어요",
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
