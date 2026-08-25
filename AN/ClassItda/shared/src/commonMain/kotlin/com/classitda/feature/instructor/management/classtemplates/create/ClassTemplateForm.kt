package com.classitda.feature.instructor.management.classtemplates.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.component.CategoryChipSelector
import com.classitda.feature.instructor.management.component.ClassStartTimeField
import com.classitda.feature.instructor.management.component.ClassTimePickerDialog
import com.classitda.feature.instructor.management.component.CreateTextField
import com.classitda.feature.instructor.management.component.OutlinedSegmentedToggle
import com.classitda.feature.instructor.management.component.WeekdaySelector
import com.classitda.feature.instructor.management.model.ClassType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import com.classitda.domain.model.instructor.management.ClassType as DomainClassType

@Composable
internal fun ClassTemplateForm(
    classTypes: List<DomainClassType>,
    initialValues: ClassTemplateFormValues?,
    submitButtonText: String,
    onSubmit: (ClassTemplateDraftUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var classType by remember { mutableStateOf(initialValues?.classType ?: ClassType.GROUP) }
    var selectedCategories by remember { mutableStateOf(initialValues?.categories ?: emptyList()) }
    var title by remember { mutableStateOf(initialValues?.title.orEmpty()) }
    var capacityText by remember { mutableStateOf(initialValues?.capacity?.toString() ?: "8") }
    var durationMinutesText by remember { mutableStateOf(initialValues?.durationMinutes?.toString() ?: "50") }
    var isRepeating by remember { mutableStateOf(initialValues?.isRepeating ?: false) }
    var selectedDays by remember { mutableStateOf(initialValues?.repeatDays ?: emptySet()) }
    var startTime by remember { mutableStateOf(initialValues?.startTime ?: LocalTime(10, 0)) }
    var description by remember { mutableStateOf(initialValues?.description.orEmpty()) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    val durationMinutes = durationMinutesText.toIntOrNull() ?: 0
    val capacity = capacityText.toIntOrNull() ?: 0
    val endTime = remember(startTime, durationMinutes) { startTime.plusMinutesClamped(durationMinutes) }

    val isFormValid =
        selectedCategories.isNotEmpty() &&
            title.isNotBlank() &&
            capacity > 0 &&
            durationMinutes > 0 &&
            (!isRepeating || selectedDays.isNotEmpty())

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SectionLabel(text = "수업 유형 *")
            OutlinedSegmentedToggle(
                options = ClassType.entries.map { it.label },
                selectedIndex = ClassType.entries.indexOf(classType),
                onOptionSelected = { classType = ClassType.entries[it] },
            )
        }

        CategoryChipSelector(
            label = "카테고리 *",
            allCategories = classTypes.map { it.name },
            selectedCategories = selectedCategories,
            onSelectedCategoriesChanged = { selectedCategories = it },
        )

        CreateTextField(
            label = "수업명 *",
            value = title,
            placeholder = "예: 리포머 비기너 클래스",
            onValueChange = { title = it },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            CreateTextField(
                label = "기본 정원 *",
                value = capacityText,
                placeholder = "0",
                onValueChange = { capacityText = it.filter { c -> c.isDigit() } },
                keyboardType = KeyboardType.Number,
                trailingText = "명",
                modifier = Modifier.weight(1f),
            )
            CreateTextField(
                label = "진행 시간 *",
                value = durationMinutesText,
                placeholder = "0",
                onValueChange = { durationMinutesText = it.filter { c -> c.isDigit() } },
                keyboardType = KeyboardType.Number,
                trailingText = "분",
                modifier = Modifier.weight(1f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SectionLabel(text = "반복 여부 *")
            OutlinedSegmentedToggle(
                options = listOf("반복함", "반복 없음"),
                selectedIndex = if (isRepeating) 0 else 1,
                onOptionSelected = { isRepeating = it == 0 },
            )

            if (isRepeating) {
                WeekdaySelector(
                    selectedDays = selectedDays,
                    onDayToggled = { day ->
                        selectedDays =
                            if (day in selectedDays) selectedDays - day else selectedDays + day
                    },
                    modifier = Modifier.padding(top = AppSpacing.sm),
                )
            }
        }

        if (isRepeating) {
            ClassStartTimeField(
                label = "수업 시작 *",
                startTimeText = formatAmPmTime(startTime),
                endTimeText = formatAmPmTime(endTime),
                onStartTimeClick = { isTimePickerVisible = true },
            )
        }

        CreateTextField(
            label = "상세설명",
            value = description,
            placeholder = "예: 리포머룸, 숙련자 추천, 준비물 - 수건",
            onValueChange = { description = it },
            singleLine = false,
            minLines = 3,
        )

        PrimaryButton(
            text = submitButtonText,
            enabled = isFormValid,
            onClick = {
                onSubmit(
                    ClassTemplateDraftUiModel(
                        classType = classType,
                        categories = selectedCategories,
                        classTypeIds =
                            selectedCategories.mapNotNull { name ->
                                classTypes.find { it.name == name }?.id
                            },
                        title = title,
                        capacity = capacity,
                        durationMinutes = durationMinutes,
                        isRepeating = isRepeating,
                        repeatDays = if (isRepeating) selectedDays else emptySet(),
                        startTime = startTime,
                        description = description,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (isTimePickerVisible) {
        ClassTimePickerDialog(
            initialTime = startTime,
            onDismissRequest = { isTimePickerVisible = false },
            onConfirm = { newTime ->
                startTime = newTime
                isTimePickerVisible = false
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = InsColors.TextPrimary,
    )
}

private fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}

private fun formatAmPmTime(time: LocalTime): String {
    val amPm = if (time.hour < 12) "오전" else "오후"
    val hour12 =
        when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
    return "$amPm $hour12:${time.minute.toString().padStart(2, '0')}"
}
