package com.classitda.feature.instructor.classsession.member.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionMember
import com.classitda.domain.repository.instructor.management.ClassManagementRepository
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import com.classitda.feature.instructor.classsession.member.edit.model.ClassSessionMemberEditUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal class ClassSessionMemberEditViewModel(
    private val repository: ClassManagementRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionMemberEditUiState>(ClassSessionMemberEditUiState.Loading)
    val uiState: StateFlow<ClassSessionMemberEditUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    fun load(sessionId: String) {
        loadJob?.cancel()
        _uiState.value = ClassSessionMemberEditUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val session = repository.getSessions().firstOrNull { it.id == sessionId }
                    _uiState.value =
                        session?.let { ClassSessionMemberEditUiState.Success(it.toMemberEditUiModel()) }
                            ?: ClassSessionMemberEditUiState.Error("수업 정보를 찾을 수 없어요")
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value = ClassSessionMemberEditUiState.Error(exception.message)
                }
            }
    }

    fun saveMembers(
        sessionId: String,
        members: List<ClassSessionMemberUiModel>,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                repository.updateSessionMembers(
                    sessionId = sessionId,
                    members =
                        members.map { member ->
                            ClassSessionMember(
                                id = member.id,
                                name = member.name,
                                isTemporary = member.isTemporary,
                            )
                        },
                )
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = ClassSessionMemberEditUiState.Error(exception.message)
            }
        }
    }
}

private fun ClassSession.toMemberEditUiModel(): ClassSessionMemberEditUiModel {
    val bookedMembers =
        members.map { member ->
            ClassSessionMemberUiModel(
                id = member.id,
                name = member.name,
                isTemporary = member.isTemporary,
            )
        }
    val detail =
        ClassSessionDetailUiModel(
            id = id,
            dateText = startAt.date.toInstructorDateText(),
            tags = tags,
            title = title,
            timeText = "${startAt.time.toAmPmText()} ~ ${endAt.time.toPlainText()}",
            reservedCount = bookedMembers.size,
            capacity = capacity,
            description = "체어룸에서 할 예정",
            location = "체어룸",
            status = status,
            members = bookedMembers,
        )
    return ClassSessionMemberEditUiModel(
        detail = detail,
        availableMembers =
            listOf(
                ClassSessionMemberUiModel(id = "member-4", name = "최유진"),
                ClassSessionMemberUiModel(id = "member-5", name = "정하늘"),
                ClassSessionMemberUiModel(id = "member-6", name = "김서연"),
            ),
    )
}

private fun kotlinx.datetime.LocalDate.toInstructorDateText(): String {
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
