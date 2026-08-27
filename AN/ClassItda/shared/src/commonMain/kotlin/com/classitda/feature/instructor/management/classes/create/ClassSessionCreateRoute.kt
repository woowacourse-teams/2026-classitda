package com.classitda.feature.instructor.management.classes.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.InsColors
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InsColors.Primary)
                }
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
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
