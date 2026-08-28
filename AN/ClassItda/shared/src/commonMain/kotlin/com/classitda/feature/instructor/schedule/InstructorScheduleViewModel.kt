package com.classitda.feature.instructor.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.session.InstructorCalendarDay
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.session.toClassSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal class InstructorScheduleViewModel(
    private val repository: InstructorSessionRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorScheduleUiState>(InstructorScheduleUiState.Loading)
    val uiState: StateFlow<InstructorScheduleUiState> = _uiState.asStateFlow()
    private var lastRequestedDate: LocalDate? = null
    private var loadJob: Job? = null
    private var calendarRangeStart: LocalDate? = null
    private var calendarRangeEnd: LocalDate? = null

    fun retry() {
        load(lastRequestedDate ?: Clock.System.todayIn(TimeZone.of("Asia/Seoul")))
    }

    fun load(date: LocalDate = Clock.System.todayIn(TimeZone.of("Asia/Seoul"))) {
        lastRequestedDate = date
        loadJob?.cancel()
        val current = _uiState.value as? InstructorScheduleUiState.Success
        if (current == null) {
            loadAll(date)
        } else if (isInLoadedCalendarRange(date)) {
            loadSessions(date, current)
        } else {
            loadAll(date, current)
        }
    }

    fun refresh(date: LocalDate = lastRequestedDate ?: Clock.System.todayIn(TimeZone.of("Asia/Seoul"))) {
        lastRequestedDate = date
        loadJob?.cancel()
        loadAll(date, _uiState.value as? InstructorScheduleUiState.Success)
    }

    private fun loadAll(
        date: LocalDate,
        current: InstructorScheduleUiState.Success? = null,
    ) {
        _uiState.value = current?.copy(sessionList = InstructorScheduleListUiState.Loading)
            ?: InstructorScheduleUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val calendarDays =
                        repository.getCalendar(
                            studioId = studio.id,
                            from = date.minus(DatePeriod(days = 20)),
                            to = date.plus(DatePeriod(days = 20)),
                        )
                    val sessions = repository.getDailySessions(studio.id, date).map { it.toClassSession() }
                    calendarRangeStart = date.minus(DatePeriod(days = 20))
                    calendarRangeEnd = date.plus(DatePeriod(days = 20))
                    _uiState.value =
                        InstructorScheduleUiState.Success(
                            calendarDays = calendarDays,
                            sessionList = InstructorScheduleListUiState.Content(sessions),
                        )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value =
                        current?.copy(sessionList = InstructorScheduleListUiState.Error(exception.message))
                            ?: InstructorScheduleUiState.Error(exception.message)
                }
            }
    }

    private fun loadSessions(
        date: LocalDate,
        current: InstructorScheduleUiState.Success,
    ) {
        _uiState.value = current.copy(sessionList = InstructorScheduleListUiState.Loading)
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val sessions = repository.getDailySessions(studio.id, date).map { it.toClassSession() }
                    _uiState.value = current.copy(sessionList = InstructorScheduleListUiState.Content(sessions))
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value = current.copy(sessionList = InstructorScheduleListUiState.Error(exception.message))
                }
            }
    }

    private fun isInLoadedCalendarRange(date: LocalDate): Boolean {
        val start = calendarRangeStart ?: return false
        val end = calendarRangeEnd ?: return false
        return date >= start && date <= end
    }
}

internal sealed interface InstructorScheduleUiState {
    data object Loading : InstructorScheduleUiState

    data class Success(
        val calendarDays: List<InstructorCalendarDay>,
        val sessionList: InstructorScheduleListUiState,
    ) : InstructorScheduleUiState

    data class Error(
        val message: String?,
    ) : InstructorScheduleUiState
}

internal sealed interface InstructorScheduleListUiState {
    data object Loading : InstructorScheduleListUiState

    data class Content(
        val sessions: List<ClassSession>,
    ) : InstructorScheduleListUiState

    data class Error(
        val message: String?,
    ) : InstructorScheduleListUiState
}
