package com.classitda.feature.instructor.classsession.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal class ClassSessionDetailViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionDetailUiState>(ClassSessionDetailUiState.Loading)
    val uiState: StateFlow<ClassSessionDetailUiState> = _uiState.asStateFlow()

    fun load(sessionId: String) {
        _uiState.value = ClassSessionDetailUiState.Loading
        viewModelScope.launch {
            runCatching { repository.getSessions().firstOrNull { it.id == sessionId } }
                .onSuccess { session ->
                    _uiState.value =
                        session?.let { ClassSessionDetailUiState.Success(it.toDetailUiModel()) }
                            ?: ClassSessionDetailUiState.Error("수업 정보를 찾을 수 없어요")
                }.onFailure { error ->
                    _uiState.value = ClassSessionDetailUiState.Error(error.message)
                }
        }
    }
}

private fun ClassSession.toDetailUiModel(): ClassSessionDetailUiModel =
    ClassSessionDetailUiModel(
        id = id,
        dateText = startAt.date.toInstructorDateText(),
        tags = tags,
        title = title,
        timeText = "${startAt.time.toAmPmText()} ~ ${endAt.time.toPlainText()}",
        reservedCount = reservedCount,
        capacity = capacity,
        description = "체어룸에서 할 예정",
        location = "체어룸",
        status = status,
        members = demoMembers,
    )

private val demoMembers =
    listOf(
        ClassSessionMemberUiModel(id = "member-1", name = "김민지"),
        ClassSessionMemberUiModel(id = "member-2", name = "이서윤"),
        ClassSessionMemberUiModel(id = "member-3", name = "박지수", isTemporary = true),
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
