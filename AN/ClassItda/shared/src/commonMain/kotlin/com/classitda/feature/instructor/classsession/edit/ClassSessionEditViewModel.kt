package com.classitda.feature.instructor.classsession.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel
import com.classitda.feature.instructor.management.model.ClassType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

internal class ClassSessionEditViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionEditUiState>(ClassSessionEditUiState.Loading)
    val uiState: StateFlow<ClassSessionEditUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var currentSession: ClassSession? = null

    fun load(sessionId: String) {
        loadJob?.cancel()
        _uiState.value = ClassSessionEditUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val session = repository.getSessions().firstOrNull { it.id == sessionId }
                    currentSession = session
                    _uiState.value =
                        session?.let { ClassSessionEditUiState.Success(it.toEditForm()) }
                            ?: ClassSessionEditUiState.Error("수업 정보를 찾을 수 없어요")
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value = ClassSessionEditUiState.Error(exception.message)
                }
            }
    }

    fun updateSession(
        form: ClassSessionEditFormUiModel,
        onSuccess: () -> Unit,
    ) {
        val session = currentSession ?: return
        viewModelScope.launch {
            try {
                repository.updateSession(session.toUpdatedSession(form))
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = ClassSessionEditUiState.Error(exception.message)
            }
        }
    }

    fun deleteSession(
        sessionId: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.deleteSession(sessionId)
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = ClassSessionEditUiState.Error(exception.message)
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

private fun ClassSession.toUpdatedSession(form: ClassSessionEditFormUiModel): ClassSession {
    val endTime = form.startTime.plusMinutesClamped(form.durationMinutes)
    return copy(
        tags = listOf(form.classType.label) + form.categories,
        title = form.title,
        startAt = LocalDateTime(form.sessionDate, form.startTime),
        endAt = LocalDateTime(form.sessionDate, endTime),
        capacity = form.capacity,
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

private fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}
