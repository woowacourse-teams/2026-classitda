package com.classitda.feature.instructor.management.`class`.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.instructor.management.`class`.create.component.CategoryChipSelector
import com.classitda.feature.instructor.management.`class`.create.component.ClassStartTimeField
import com.classitda.feature.instructor.management.`class`.create.component.ClassTimePickerDialog
import com.classitda.feature.instructor.management.`class`.create.component.CreateTextField
import com.classitda.feature.instructor.management.`class`.create.component.DatePickerField
import com.classitda.feature.instructor.management.`class`.create.component.DropdownField
import com.classitda.feature.instructor.management.`class`.create.component.OutlinedSegmentedToggle
import com.classitda.feature.instructor.management.`class`.create.component.TemplateOverwriteConfirmDialog
import com.classitda.feature.instructor.management.`class`.create.component.WeekdaySelector
import com.classitda.feature.instructor.management.`class`.create.model.ClassSessionDraftUiModel
import com.classitda.feature.instructor.management.`class`.create.model.ClassType
import com.classitda.feature.instructor.management.`class`.create.util.categoriesOrEmpty
import com.classitda.feature.instructor.management.`class`.create.util.classTypeOrNull
import com.classitda.feature.instructor.management.`class`.create.util.digitsOnly
import com.classitda.feature.instructor.management.`class`.model.ClassTemplateUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

@Composable
internal fun ClassSessionCreateScreen(
    templates: List<ClassTemplateUiModel>,
    categories: List<String>,
    onBackClick: () -> Unit,
    onSubmit: (ClassSessionDraftUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTemplate by remember { mutableStateOf<ClassTemplateUiModel?>(null) }
    var pendingTemplate by remember { mutableStateOf<ClassTemplateUiModel?>(null) }
    var isOverwriteConfirmVisible by remember { mutableStateOf(false) }
    var isTimePickerVisible by remember { mutableStateOf(false) }

    var classType by remember { mutableStateOf(ClassType.GROUP) }
    var selectedCategories by remember { mutableStateOf(emptyList<String>()) }
    var title by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("8") }
    var durationMinutesText by remember { mutableStateOf("50") }
    var isRepeating by remember { mutableStateOf(false) }
    var selectedDays by remember { mutableStateOf(emptySet<DayOfWeek>()) }
    var startTime by remember { mutableStateOf(LocalTime(10, 0)) }
    var repeatStartDate by remember { mutableStateOf(LocalDate(2026, 8, 13)) }
    var repeatEndDate by remember { mutableStateOf(LocalDate(2026, 9, 7)) }
    var sessionDate by remember { mutableStateOf(LocalDate(2026, 8, 8)) }
    var description by remember { mutableStateOf("") }

    val durationMinutes = durationMinutesText.toIntOrNull() ?: 0
    val endTime = remember(startTime, durationMinutes) { startTime.plusMinutesClamped(durationMinutes) }
    val isFormDirty =
        title.isNotBlank() ||
            description.isNotBlank() ||
            selectedCategories.isNotEmpty() ||
            capacityText != "8" ||
            durationMinutesText != "50"

    fun applyTemplate(template: ClassTemplateUiModel) {
        selectedTemplate = template
        template.tags.classTypeOrNull()?.let { classType = it }
        selectedCategories = template.tags.categoriesOrEmpty()
        title = template.title
        template.capacityText.digitsOnly().takeIf { it.isNotEmpty() }?.let { capacityText = it }
        template.durationText.digitsOnly().takeIf { it.isNotEmpty() }?.let { durationMinutesText = it }
    }

    Scaffold(
        modifier = modifier,
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
        ) {
            DropdownField(
                label = "수업 템플릿",
                placeholder = "수업 템플릿 선택",
                options = templates.map { it.title },
                selectedOption = selectedTemplate?.title,
                onOptionSelected = { selectedTitle ->
                    val template = templates.firstOrNull { it.title == selectedTitle }
                    if (template != null) {
                        if (isFormDirty && selectedTemplate?.id != template.id) {
                            pendingTemplate = template
                            isOverwriteConfirmVisible = true
                        } else {
                            applyTemplate(template)
                        }
                    }
                },
            )

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
                allCategories = categories,
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

            ClassStartTimeField(
                label = "수업 시간 *",
                startTimeText = formatAmPmTime(startTime),
                endTimeText = formatAmPmTime(endTime),
                onStartTimeClick = { isTimePickerVisible = true },
            )

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
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    SectionLabel(text = "반복 기간 *")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            SubFieldLabel(text = "시작일")
                            DatePickerField(dateText = formatDateDot(repeatStartDate), onClick = {})
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            SubFieldLabel(text = "종료일")
                            DatePickerField(dateText = formatDateDot(repeatEndDate), onClick = {})
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    SectionLabel(text = "수업일 *")
                    DatePickerField(dateText = formatDateWithDayOfWeek(sessionDate), onClick = {})
                }
            }

            CreateTextField(
                label = "상세 설명",
                value = description,
                placeholder = "예: 리포머룸, 숙련자 추천, 준비물 - 수건",
                onValueChange = { description = it },
                singleLine = false,
                minLines = 3,
            )

            PrimaryButton(
                text = "등록 완료",
                onClick = {
                    onSubmit(
                        ClassSessionDraftUiModel(
                            templateId = selectedTemplate?.id,
                            classType = classType,
                            categories = selectedCategories,
                            title = title,
                            capacity = capacityText.toIntOrNull() ?: 0,
                            durationMinutes = durationMinutes,
                            startTime = startTime,
                            isRepeating = isRepeating,
                            repeatDays = if (isRepeating) selectedDays else emptySet(),
                            repeatStartDate = if (isRepeating) repeatStartDate else null,
                            repeatEndDate = if (isRepeating) repeatEndDate else null,
                            sessionDate = if (isRepeating) null else sessionDate,
                            description = description,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (isOverwriteConfirmVisible) {
        TemplateOverwriteConfirmDialog(
            onDismissRequest = {
                isOverwriteConfirmVisible = false
                pendingTemplate = null
            },
            onConfirm = {
                pendingTemplate?.let { applyTemplate(it) }
                isOverwriteConfirmVisible = false
                pendingTemplate = null
            },
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

@Composable
private fun SubFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = InsColors.TextSecondary,
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

private fun formatDateDot(date: LocalDate): String =
    "${date.year}.${date.month.number.toString().padStart(2, '0')}.${date.day.toString().padStart(2, '0')}"

private fun formatDateWithDayOfWeek(date: LocalDate): String = "${formatDateDot(date)} (${date.dayOfWeek.toKoreanShort()})"

private fun DayOfWeek.toKoreanShort(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

@Composable
@Preview
private fun ClassSessionCreateScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionCreateScreen(
            templates =
                listOf(
                    ClassTemplateUiModel(
                        id = "1",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "리포머 밸런스",
                        durationText = "50분",
                        capacityText = "8명",
                        schedule = null,
                    ),
                ),
            categories = listOf("필라테스", "요가", "그룹 PT"),
            onBackClick = {},
            onSubmit = {},
        )
    }
}
