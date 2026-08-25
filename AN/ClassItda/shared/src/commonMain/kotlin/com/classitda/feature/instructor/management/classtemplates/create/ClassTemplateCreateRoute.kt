package com.classitda.feature.instructor.management.classtemplates.create

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
import org.koin.core.parameter.parametersOf

@Composable
internal fun ClassTemplateCreateRoute(
    templateId: String?,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassTemplateCreateViewModel =
        koinViewModel(parameters = { parametersOf(templateId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formLoadState by viewModel.formLoadState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ClassTemplateCreateUiState.Success -> {
                onCreated()
            }

            is ClassTemplateCreateUiState.Error -> {
                snackbarHostState.showSnackbar(state.message ?: "템플릿 저장에 실패했어요")
            }

            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = formLoadState) {
            ClassTemplateFormLoadState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = InsColors.Primary)
                }
            }

            is ClassTemplateFormLoadState.Ready -> {
                if (viewModel.isEditMode) {
                    ClassTemplateEditScreen(
                        classTypes = state.classTypes,
                        initialValues = state.initialValues,
                        onBackClick = onBackClick,
                        onSubmit = viewModel::submit,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ClassTemplateCreateScreen(
                        classTypes = state.classTypes,
                        onBackClick = onBackClick,
                        onSubmit = viewModel::submit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
