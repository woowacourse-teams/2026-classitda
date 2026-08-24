package com.classitda.feature.instructor.management.`class`.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.InsColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ClassTemplateCreateRoute(
    templateId: String?,
    categories: List<String>,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassTemplateCreateViewModel =
        koinViewModel(parameters = { parametersOf(templateId) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formLoadState by viewModel.formLoadState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ClassTemplateCreateUiState.Success) {
            onCreated()
        }
    }

    when (val state = formLoadState) {
        ClassTemplateFormLoadState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = InsColors.Primary)
            }
        }

        is ClassTemplateFormLoadState.Ready -> {
            if (viewModel.isEditMode) {
                ClassTemplateEditScreen(
                    categories = categories,
                    initialValues = state.initialValues,
                    onBackClick = onBackClick,
                    onSubmit = viewModel::submit,
                    modifier = modifier,
                )
            } else {
                ClassTemplateCreateScreen(
                    categories = categories,
                    onBackClick = onBackClick,
                    onSubmit = viewModel::submit,
                    modifier = modifier,
                )
            }
        }
    }
}
