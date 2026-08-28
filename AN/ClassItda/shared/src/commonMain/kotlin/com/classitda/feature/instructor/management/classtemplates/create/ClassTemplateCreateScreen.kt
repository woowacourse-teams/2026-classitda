package com.classitda.feature.instructor.management.classtemplates.create

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateDraftUiModel

@Composable
internal fun ClassTemplateCreateScreen(
    classTypes: List<ClassType>,
    onBackClick: () -> Unit,
    onSubmit: (ClassTemplateDraftUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClassTemplateForm(
        classTypes = classTypes,
        initialValues = null,
        submitButtonText = "생성 완료",
        onSubmit = onSubmit,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 템플릿 생성",
            )
        },
        modifier = modifier,
    )
}

@Composable
@Preview
private fun ClassTemplateCreateScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateCreateScreen(
            classTypes =
                listOf(
                    ClassType(id = "1", name = "필라테스"),
                    ClassType(id = "2", name = "요가"),
                    ClassType(id = "3", name = "그룹 PT"),
                ),
            onBackClick = {},
            onSubmit = {},
        )
    }
}
