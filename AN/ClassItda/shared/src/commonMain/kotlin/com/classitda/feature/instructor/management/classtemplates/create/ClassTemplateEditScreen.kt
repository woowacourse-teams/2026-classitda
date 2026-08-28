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
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.model.ClassFormOption
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
internal fun ClassTemplateEditScreen(
    classTypes: List<ClassType>,
    initialValues: ClassTemplateFormValues?,
    onBackClick: () -> Unit,
    onSubmit: (ClassTemplateDraftUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClassTemplateForm(
        classTypes = classTypes,
        initialValues = initialValues,
        submitButtonText = "수정 완료",
        onSubmit = onSubmit,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 템플릿 수정하기",
            )
        },
        modifier = modifier,
    )
}

@Composable
@Preview
private fun ClassTemplateEditScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateEditScreen(
            classTypes =
                listOf(
                    ClassType(id = "1", name = "필라테스"),
                    ClassType(id = "2", name = "요가"),
                    ClassType(id = "3", name = "그룹 PT"),
                ),
            initialValues =
                ClassTemplateFormValues(
                    classType = ClassFormOption.GROUP,
                    category = ClassType(id = "1", name = "필라테스"),
                    title = "리포머 밸런스",
                    capacity = 8,
                    durationMinutes = 50,
                    isRepeating = false,
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    startTime = LocalTime(10, 0),
                    description = "",
                ),
            onBackClick = {},
            onSubmit = {},
        )
    }
}
