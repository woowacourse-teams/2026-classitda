package com.classitda.feature.instructor.classsession.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.session.InstructorSessionDetail
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import com.classitda.feature.instructor.session.toUiStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal class ClassSessionDetailViewModel(
    private val repository: InstructorSessionRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionDetailUiState>(ClassSessionDetailUiState.Loading)
    val uiState: StateFlow<ClassSessionDetailUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun load(sessionId: String) {
        loadJob?.cancel()
        _uiState.value = ClassSessionDetailUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val session = repository.getSession(studio.id, sessionId)
                    _uiState.value = ClassSessionDetailUiState.Success(session.toDetailUiModel())
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value = ClassSessionDetailUiState.Error(exception.message)
                }
            }
    }
}

private fun InstructorSessionDetail.toDetailUiModel(): ClassSessionDetailUiModel =
    ClassSessionDetailUiModel(
        id = id,
        dateText = startAt.date.toInstructorDateText(),
        tags = listOf(classType.name),
        title = className,
        timeText = "${startAt.time.toAmPmText()} ~ ${endAt.time.toPlainText()}",
        reservedCount = 0,
        capacity = capacity,
        description = description.orEmpty(),
        location = "장소 정보 없음",
        status = sessionPhase.toUiStatus(),
        members = emptyList<ClassSessionMemberUiModel>(),
    )

private fun LocalDate.toInstructorDateText(): String {
    val monthText = month.number.toString().padStart(2, '0')
    val dayText = day.toString().padStart(2, '0')
    return "$year.$monthText.$dayText (${dayOfWeek.toKoreanShort()})"
}

private fun LocalTime.toAmPmText(): String =
    "${if (hour < 12) "오전" else "오후"} ${to12HourText()}:${minute.toString().padStart(2, '0')}"

private fun LocalTime.toPlainText(): String = "${to12HourText()}:${minute.toString().padStart(2, '0')}"

private fun LocalTime.to12HourText(): String =
    when {
        hour == 0 -> "12"
        hour > 12 -> (hour - 12).toString()
        else -> hour.toString()
    }

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
