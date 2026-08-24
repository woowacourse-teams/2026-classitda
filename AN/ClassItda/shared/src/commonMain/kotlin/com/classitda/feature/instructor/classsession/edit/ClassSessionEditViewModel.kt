package com.classitda.feature.instructor.classsession.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel
import com.classitda.feature.instructor.management.lesson.create.model.ClassType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

internal class ClassSessionEditViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionEditUiState>(ClassSessionEditUiState.Loading)
    val uiState: StateFlow<ClassSessionEditUiState> = _uiState.asStateFlow()

    fun load(sessionId: String) {
        _uiState.value = ClassSessionEditUiState.Loading
        viewModelScope.launch {
            runCatching { repository.getSessions().firstOrNull { it.id == sessionId } }
                .onSuccess { session ->
                    _uiState.value =
                        session?.let { ClassSessionEditUiState.Success(it.toEditForm()) }
                            ?: ClassSessionEditUiState.Error("수업 정보를 찾을 수 없어요")
                }.onFailure { error ->
                    _uiState.value = ClassSessionEditUiState.Error(error.message)
                }
        }
    }
}

private fun ClassSession.toEditForm(): ClassSessionEditFormUiModel {
    val classType = ClassType.entries.firstOrNull { it.label in tags } ?: ClassType.GROUP
    val categories = tags.filter { it !in ClassType.entries.map(ClassType::label) }
    val durationMinutes = durationBetween(startAt.time, endAt.time)

    return ClassSessionEditFormUiModel(
        id = id,
        classType = classType,
        categories = categories,
        title = title,
        capacity = capacity,
        reservedCount = reservedCount,
        durationMinutes = durationMinutes,
        startTime = startAt.time,
        sessionDate = startAt.date,
        description = "체어룸에서 할 예정",
    )
}

private fun durationBetween(
    start: LocalTime,
    end: LocalTime,
): Int {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute
    return (endMinutes - startMinutes).takeIf { it > 0 } ?: 50
}
