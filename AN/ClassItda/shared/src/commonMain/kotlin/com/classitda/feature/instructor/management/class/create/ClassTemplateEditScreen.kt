package com.classitda.feature.instructor.management.`class`.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.feature.instructor.management.`class`.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.`class`.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.`class`.create.model.ClassType

@Composable
internal fun ClassTemplateEditScreen(
    categories: List<String>,
    initialValues: ClassTemplateFormValues?,
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
                title = "수업 템플릿 수정하기",
            )
        },
    ) { contentPadding ->
        ClassTemplateForm(
            categories = categories,
            initialValues = initialValues,
            submitButtonText = "수정 완료",
            onSubmit = onSubmit,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
@Preview
private fun ClassTemplateEditScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateEditScreen(
            categories = listOf("필라테스", "요가", "그룹 PT"),
            initialValues =
                ClassTemplateFormValues(
                    classType = ClassType.GROUP,
                    categories = listOf("필라테스"),
                    title = "리포머 밸런스",
                    capacity = 8,
                    durationMinutes = 50,
                    isRepeating = true,
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    startTime = LocalTime(10, 0),
                    description = "",
                ),
            onBackClick = {},
            onSubmit = {},
        )
    }
}
