package com.classitda.feature.instructor.classsession.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.feature.instructor.classsession.edit.component.ClassSessionCapacityChangeDialog
import com.classitda.feature.instructor.classsession.edit.component.ClassSessionDatePickerDialog
import com.classitda.feature.instructor.classsession.edit.component.ClassSessionDeleteConfirmDialog
import com.classitda.feature.instructor.classsession.edit.component.ClassSessionEditExitDialog
import com.classitda.feature.instructor.classsession.edit.component.EditCategoryChip
import com.classitda.feature.instructor.classsession.edit.component.EditClassStartTimeField
import com.classitda.feature.instructor.classsession.edit.component.EditClassTimePickerDialog
import com.classitda.feature.instructor.classsession.edit.component.EditDatePickerField
import com.classitda.feature.instructor.classsession.edit.component.EditFieldDefaults
import com.classitda.feature.instructor.classsession.edit.component.EditOutlinedSegmentedToggle
import com.classitda.feature.instructor.classsession.edit.component.EditTextField
import com.classitda.feature.instructor.classsession.edit.component.EditUnitTextField
import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel
import com.classitda.feature.instructor.management.model.ClassFormOption
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import org.koin.compose.viewmodel.koinViewModel
import com.classitda.domain.model.instructor.management.ClassType as DomainClassType

@Composable
internal fun ClassSessionEditRoute(
    sessionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassSessionEditViewModel = koinViewModel(),
) {
    LaunchedEffect(sessionId) {
        viewModel.load(sessionId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        ClassSessionEditUiState.Loading -> {
            ClassSessionEditLoading(modifier)
        }

        is ClassSessionEditUiState.Error -> {
            ClassSessionEditError(
                message = state.message,
                onRetry = { viewModel.load(sessionId) },
                modifier = modifier,
            )
        }

        is ClassSessionEditUiState.Success -> {
            ClassSessionEditStateful(
                initialForm = state.form,
                categories = state.categories,
                onBackClick = onBackClick,
                onSave = { form -> viewModel.updateSession(form, onSaved) },
                onDelete = { viewModel.deleteSession(sessionId, onDeleted) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClassSessionEditStateful(
    initialForm: ClassSessionEditFormUiModel,
    categories: List<DomainClassType>,
    onBackClick: () -> Unit,
    onSave: (ClassSessionEditFormUiModel) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var classType by remember(initialForm.id) { mutableStateOf(initialForm.classType) }
    var selectedCategories by remember(initialForm.id) { mutableStateOf(initialForm.categories) }
    var title by remember(initialForm.id) { mutableStateOf(initialForm.title) }
    var capacityText by remember(initialForm.id) { mutableStateOf(initialForm.capacity.toString()) }
    var durationText by remember(initialForm.id) { mutableStateOf(initialForm.durationMinutes.toString()) }
    var startTime by remember(initialForm.id) { mutableStateOf(initialForm.startTime) }
    var sessionDate by remember(initialForm.id) { mutableStateOf(initialForm.sessionDate) }
    var description by remember(initialForm.id) { mutableStateOf(initialForm.description) }
    var isTimePickerVisible by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isExitDialogVisible by remember { mutableStateOf(false) }
    var isCapacityDialogVisible by remember { mutableStateOf(false) }
    var isDeleteDialogVisible by remember { mutableStateOf(false) }

    val durationMinutes = durationText.toIntOrNull() ?: 0
    val endTime = startTime.plusMinutesClamped(durationMinutes)
    val hasUnsavedChanges =
        classType != initialForm.classType ||
            selectedCategories != initialForm.categories ||
            title != initialForm.title ||
            capacityText != initialForm.capacity.toString() ||
            durationText != initialForm.durationMinutes.toString() ||
            startTime != initialForm.startTime ||
            sessionDate != initialForm.sessionDate ||
            description != initialForm.description

    fun requestBack() {
        if (hasUnsavedChanges) {
            isExitDialogVisible = true
        } else {
            onBackClick()
        }
    }

    fun requestSave() {
        val capacity = capacityText.toIntOrNull() ?: 0
        if (capacity < initialForm.reservedCount) {
            isCapacityDialogVisible = true
        } else {
            onSave(
                initialForm.copy(
                    classType = classType,
                    classTypeId =
                        categories.firstOrNull { it.name == selectedCategories.firstOrNull() }?.id
                            ?: initialForm.classTypeId,
                    categories = selectedCategories,
                    title = title,
                    capacity = capacity,
                    durationMinutes = durationMinutes,
                    startTime = startTime,
                    sessionDate = sessionDate,
                    description = description,
                ),
            )
        }
    }

    ClassSessionEditStateless(
        classType = classType,
        selectedCategories = selectedCategories,
        categories = categories,
        title = title,
        capacityText = capacityText,
        durationText = durationText,
        startTime = startTime,
        endTime = endTime,
        sessionDate = sessionDate,
        description = description,
        onBackClick = ::requestBack,
        onClassTypeChange = { classType = it },
        onCategoriesChange = { selectedCategories = it },
        onTitleChange = { title = it },
        onCapacityChange = { capacityText = it.filter(Char::isDigit) },
        onDurationChange = { durationText = it.filter(Char::isDigit) },
        onStartTimeClick = { isTimePickerVisible = true },
        onDateClick = { isDatePickerVisible = true },
        onDescriptionChange = { description = it },
        onDeleteClick = { isDeleteDialogVisible = true },
        onSaveClick = ::requestSave,
        modifier = modifier,
    )

    if (isTimePickerVisible) {
        EditClassTimePickerDialog(
            initialTime = startTime,
            onDismissRequest = { isTimePickerVisible = false },
            onConfirm = {
                startTime = it
                isTimePickerVisible = false
            },
        )
    }
    if (isDatePickerVisible) {
        ClassSessionDatePickerDialog(
            initialDate = sessionDate,
            onDismissRequest = { isDatePickerVisible = false },
            onConfirm = {
                sessionDate = it
                isDatePickerVisible = false
            },
        )
    }
    if (isExitDialogVisible) {
        ClassSessionEditExitDialog(
            onDismissRequest = { isExitDialogVisible = false },
            onLeaveClick = {
                isExitDialogVisible = false
                onBackClick()
            },
        )
    }
    if (isCapacityDialogVisible) {
        ClassSessionCapacityChangeDialog(
            onConfirmClick = { isCapacityDialogVisible = false },
        )
    }
    if (isDeleteDialogVisible) {
        ClassSessionDeleteConfirmDialog(
            onDismissRequest = { isDeleteDialogVisible = false },
            onConfirmClick = {
                isDeleteDialogVisible = false
                onDelete()
            },
        )
    }
}

@Composable
internal fun ClassSessionEditStateless(
    classType: ClassFormOption,
    selectedCategories: List<String>,
    categories: List<DomainClassType>,
    title: String,
    capacityText: String,
    durationText: String,
    startTime: LocalTime,
    endTime: LocalTime,
    sessionDate: LocalDate,
    description: String,
    onBackClick: () -> Unit,
    onClassTypeChange: (ClassFormOption) -> Unit,
    onCategoriesChange: (List<String>) -> Unit,
    onTitleChange: (String) -> Unit,
    onCapacityChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onStartTimeClick: () -> Unit,
    onDateClick: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Background),
                title = "수업 수정",
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(EditFieldDefaults.labelFieldGap)) {
                EditSectionLabel(text = "수업 유형 *")
                EditOutlinedSegmentedToggle(
                    options = ClassFormOption.entries.map { it.label },
                    selectedIndex = ClassFormOption.entries.indexOf(classType),
                    onOptionSelected = { onClassTypeChange(ClassFormOption.entries[it]) },
                )
            }
            SingleCategorySelector(
                label = "카테고리 *",
                categories = categories,
                selectedCategories = selectedCategories,
                onSelectedCategoriesChanged = onCategoriesChange,
            )
            EditTextField(
                label = "수업명 *",
                value = title,
                placeholder = "수업명을 입력해 주세요",
                onValueChange = onTitleChange,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                EditUnitTextField(
                    label = "기본 정원 *",
                    value = capacityText,
                    placeholder = "0",
                    onValueChange = onCapacityChange,
                    keyboardType = KeyboardType.Number,
                    unit = "명",
                    modifier = Modifier.weight(1f),
                )
                EditUnitTextField(
                    label = "진행 시간 *",
                    value = durationText,
                    placeholder = "0",
                    onValueChange = onDurationChange,
                    keyboardType = KeyboardType.Number,
                    unit = "분",
                    modifier = Modifier.weight(1f),
                )
            }
            EditClassStartTimeField(
                label = "수업 시간 *",
                startTimeText = startTime.toAmPmText(),
                endTimeText = endTime.toAmPmText(),
                onStartTimeClick = onStartTimeClick,
            )
            Column(verticalArrangement = Arrangement.spacedBy(EditFieldDefaults.labelFieldGap)) {
                EditSectionLabel(text = "수업일 *")
                EditDatePickerField(
                    dateText = sessionDate.toDateText(),
                    onClick = onDateClick,
                )
            }
            EditTextField(
                label = "상세 설명",
                value = description,
                placeholder = "수업에 대한 설명을 입력해 주세요",
                onValueChange = onDescriptionChange,
                singleLine = false,
                minLines = 3,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    border = BorderStroke(1.dp, InsColors.Divider),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InsColors.Red),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("수업 삭제")
                }
                Button(
                    onClick = onSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("수정 완료")
                }
            }
        }
    }
}

@Composable
private fun SingleCategorySelector(
    label: String,
    categories: List<DomainClassType>,
    selectedCategories: List<String>,
    onSelectedCategoriesChanged: (List<String>) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(EditFieldDefaults.labelFieldGap)) {
        EditSectionLabel(text = label)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            categories.forEach { category ->
                EditCategoryChip(
                    text = category.name,
                    isSelected = category.name in selectedCategories,
                    onClick = {
                        onSelectedCategoriesChanged(listOf(category.name))
                    },
                )
            }
        }
    }
}

@Composable
private fun EditSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = InsColors.TextPrimary,
    )
}

@Composable
private fun ClassSessionEditLoading(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
    }
}

@Composable
private fun ClassSessionEditError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message ?: "수업 정보를 불러오지 못했어요", color = InsColors.TextSecondary)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            modifier = Modifier.padding(top = AppSpacing.lg),
        ) {
            Text("다시 시도")
        }
    }
}

@Preview(name = "수업 수정", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ClassSessionEditStatelessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionEditStateless(
            classType = ClassFormOption.GROUP,
            selectedCategories = listOf("필라테스"),
            categories =
                listOf(
                    DomainClassType(id = "1", name = "필라테스"),
                    DomainClassType(id = "2", name = "요가"),
                    DomainClassType(id = "3", name = "그룹 PT"),
                ),
            title = "리포머 밸런스",
            capacityText = "8",
            durationText = "50",
            startTime = LocalTime(19, 30),
            endTime = LocalTime(20, 20),
            sessionDate = LocalDate(2026, 8, 5),
            description = "체어룸에서 할 예정",
            onBackClick = {},
            onClassTypeChange = {},
            onCategoriesChange = {},
            onTitleChange = {},
            onCapacityChange = {},
            onDurationChange = {},
            onStartTimeClick = {},
            onDateClick = {},
            onDescriptionChange = {},
            onDeleteClick = {},
            onSaveClick = {},
        )
    }
}

private fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}

private fun LocalTime.toAmPmText(): String {
    val amPm = if (hour < 12) "오전" else "오후"
    val hour12 =
        when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    return "$amPm $hour12:${minute.toString().padStart(2, '0')}"
}

private fun LocalDate.toDateText(): String {
    val monthText = month.number.toString().padStart(2, '0')
    val dayText = day.toString().padStart(2, '0')
    return "$year.$monthText.$dayText (${dayOfWeek.toKoreanShort()})"
}

private fun kotlinx.datetime.DayOfWeek.toKoreanShort(): String =
    when (this) {
        kotlinx.datetime.DayOfWeek.MONDAY -> "월"
        kotlinx.datetime.DayOfWeek.TUESDAY -> "화"
        kotlinx.datetime.DayOfWeek.WEDNESDAY -> "수"
        kotlinx.datetime.DayOfWeek.THURSDAY -> "목"
        kotlinx.datetime.DayOfWeek.FRIDAY -> "금"
        kotlinx.datetime.DayOfWeek.SATURDAY -> "토"
        kotlinx.datetime.DayOfWeek.SUNDAY -> "일"
    }
