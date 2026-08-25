package com.classitda.feature.instructor.classsession.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.session.InstructorClassForm
import com.classitda.domain.model.instructor.session.InstructorSessionDetail
import com.classitda.domain.model.instructor.session.InstructorSessionUpdate
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.classsession.edit.model.ClassSessionEditFormUiModel
import com.classitda.feature.instructor.management.lesson.create.model.ClassType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

internal class ClassSessionEditViewModel(
    private val repository: InstructorSessionRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionEditUiState>(ClassSessionEditUiState.Loading)
    val uiState: StateFlow<ClassSessionEditUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var currentSession: InstructorSessionDetail? = null

    fun load(sessionId: String) {
        loadJob?.cancel()
        _uiState.value = ClassSessionEditUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val session = repository.getSession(studio.id, sessionId)
                    currentSession = session
                    _uiState.value = ClassSessionEditUiState.Success(session.toEditForm())
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
                val studio = studioContext.getSelectedStudio()
                repository.updateSession(
                    studioId = studio.id,
                    sessionId = session.id,
                    update = form.toUpdate(),
                )
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
                val studio = studioContext.getSelectedStudio()
                repository.cancelSession(studio.id, sessionId)
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = ClassSessionEditUiState.Error(exception.message)
            }
        }
    }
}

private fun InstructorSessionDetail.toEditForm(): ClassSessionEditFormUiModel {
    val formClassType =
        when (classForm) {
            InstructorClassForm.INDIVIDUAL -> ClassType.PERSONAL
            InstructorClassForm.GROUP,
            InstructorClassForm.UNKNOWN,
            -> ClassType.GROUP
        }
    val durationMinutes = durationBetween(startAt.time, endAt.time)

    return ClassSessionEditFormUiModel(
        id = id,
        classTypeId = this.classType.id,
        classType = formClassType,
        categories = listOf(this.classType.name),
        title = className,
        capacity = capacity,
        reservedCount = 0,
        durationMinutes = durationMinutes,
        startTime = startAt.time,
        sessionDate = startAt.date,
        description = description.orEmpty(),
    )
}

private fun ClassSessionEditFormUiModel.toUpdate() =
    InstructorSessionUpdate(
        classForm =
            when (classType) {
                ClassType.GROUP -> InstructorClassForm.GROUP
                ClassType.PERSONAL -> InstructorClassForm.INDIVIDUAL
            },
        classTypeId = classTypeId,
        className = title,
        capacity = capacity,
        durationMinutes = durationMinutes,
        startAt = LocalDateTime(sessionDate, startTime),
        description = description,
    )

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
