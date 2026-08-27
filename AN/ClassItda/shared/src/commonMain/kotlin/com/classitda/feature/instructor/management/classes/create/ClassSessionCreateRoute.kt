package com.classitda.feature.instructor.management.classes.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.feature.instructor.management.classtemplates.model.ClassTemplateUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClassSessionCreateRoute(
    templates: List<ClassTemplateUiModel>,
    categories: List<String>,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassSessionCreateViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is ClassSessionCreateUiState.Success) {
            onCreated()
        }
    }

    ClassSessionCreateScreen(
        templates = templates,
        categories = categories,
        onBackClick = onBackClick,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}
