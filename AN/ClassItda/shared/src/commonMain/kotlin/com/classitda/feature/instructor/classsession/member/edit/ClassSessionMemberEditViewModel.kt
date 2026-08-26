package com.classitda.feature.instructor.classsession.member.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.member.MembershipStatus
import com.classitda.domain.repository.instructor.member.InstructorMemberRepository
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import com.classitda.feature.instructor.classsession.member.edit.model.ClassSessionMemberEditUiModel
import com.classitda.feature.instructor.session.toUiStatus
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
    private val repository: InstructorSessionRepository,
    private val memberRepository: InstructorMemberRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ClassSessionMemberEditUiState>(ClassSessionMemberEditUiState.Loading)
    val uiState: StateFlow<ClassSessionMemberEditUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var initialMembers: List<ClassSessionMemberUiModel> = emptyList()

    fun load(sessionId: String) {
        loadJob?.cancel()
        _uiState.value = ClassSessionMemberEditUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val session =
                        repository.getSession(studio.id, sessionId)
                    val availableMembers =
                        memberRepository
                            .getStudents(studio.id)
                            .items
                            .filter { it.status == MembershipStatus.ACTIVE }
                            .map { member ->
                                ClassSessionMemberUiModel(
                                    id = member.id,
                                    name = member.name,
                                )
                            }
                    val bookedMembers =
                        session.reservedMembers.map { member ->
                            ClassSessionMemberUiModel(
                                id = member.membershipId,
                                name = member.name,
                                enrollmentId = member.enrollmentId,
                            )
                        }
                    initialMembers = bookedMembers
                    _uiState.value =
                        ClassSessionMemberEditUiState.Success(
                            session.toMemberEditUiModel(availableMembers, bookedMembers),
                        )
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
                val studio = studioContext.getSelectedStudio()
                val currentMemberIds = members.map { it.id }.toSet()
                initialMembers
                    .filter { it.id !in currentMemberIds }
                    .forEach { member ->
                        member.enrollmentId?.let { enrollmentId ->
                            memberRepository.cancelEnrollment(studio.id, sessionId, enrollmentId)
                        }
                    }
                members
                    .filter { current -> initialMembers.none { it.id == current.id } }
                    .forEach { member ->
                        memberRepository.enrollStudent(studio.id, sessionId, member.id)
                    }
                initialMembers = members
                onSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = ClassSessionMemberEditUiState.Error(exception.message)
            }
        }
    }
}

private fun com.classitda.domain.model.instructor.session.InstructorSessionDetail.toMemberEditUiModel(
    availableMembers: List<ClassSessionMemberUiModel>,
    bookedMembers: List<ClassSessionMemberUiModel>,
): ClassSessionMemberEditUiModel {
    val detail =
        ClassSessionDetailUiModel(
            id = id,
            dateText = startAt.date.toInstructorDateText(),
            tags = listOf(classType.name),
            title = className,
            timeText = "${startAt.time.toAmPmText()} ~ ${endAt.time.toPlainText()}",
            reservedCount = bookedMembers.size,
            capacity = capacity,
            description = description.orEmpty(),
            status = sessionPhase.toUiStatus(),
            members = bookedMembers,
        )
    return ClassSessionMemberEditUiModel(
        detail = detail,
        availableMembers = availableMembers,
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
