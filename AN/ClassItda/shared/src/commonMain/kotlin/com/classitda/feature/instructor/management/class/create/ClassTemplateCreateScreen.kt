package com.classitda.feature.instructor.management.`class`.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.feature.instructor.management.`class`.create.model.ClassTemplateDraftUiModel

@Composable
internal fun ClassTemplateCreateScreen(
    categories: List<String>,
    onBackClick: () -> Unit,
    onSubmit: (ClassTemplateDraftUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 템플릿 생성",
            )
        },
    ) { contentPadding ->
        ClassTemplateForm(
            categories = categories,
            initialValues = null,
            submitButtonText = "생성 완료",
            onSubmit = onSubmit,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
@Preview
private fun ClassTemplateCreateScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateCreateScreen(
            categories = listOf("필라테스", "요가", "그룹 PT"),
            onBackClick = {},
            onSubmit = {},
        )
    }
}
