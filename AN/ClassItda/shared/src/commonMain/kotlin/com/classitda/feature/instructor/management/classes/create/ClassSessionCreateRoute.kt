package com.classitda.feature.instructor.management.classes.create

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
import androidx.compose.material3.SnackbarHost
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
internal fun ClassSessionCreateRoute(
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassSessionCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formLoadState by viewModel.formLoadState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ClassSessionCreateUiState.Success -> {
                onCreated()
            }

            is ClassSessionCreateUiState.Error -> {
                snackbarHostState.showSnackbar(state.message ?: "수업 등록에 실패했어요")
            }

            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = formLoadState) {
            ClassSessionCreateFormLoadState.Loading -> {
                ClassSessionCreateLoadingRoute(onBackClick = onBackClick)
            }

            is ClassSessionCreateFormLoadState.Ready -> {
                ClassSessionCreateScreen(
                    templates = state.templates,
                    classTypes = state.classTypes,
                    onBackClick = onBackClick,
                    onSubmit = viewModel::submit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            is ClassSessionCreateFormLoadState.Error -> {
                ClassSessionCreateFormErrorRoute(
                    message = state.message,
                    onRetry = viewModel::onRetry,
                    onBackClick = onBackClick,
                )
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ClassSessionCreateLoadingRoute(onBackClick: () -> Unit) {
    Scaffold(
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 등록",
            )
        },
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
private fun ClassSessionCreateFormErrorRoute(
    message: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 등록",
            )
        },
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
                text = message ?: "정보를 불러오지 못했어요",
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
