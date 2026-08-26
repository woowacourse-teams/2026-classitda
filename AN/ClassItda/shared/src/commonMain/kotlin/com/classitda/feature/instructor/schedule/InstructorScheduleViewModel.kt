package com.classitda.feature.instructor.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.session.InstructorCalendarDay
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.session.toClassSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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

    fun retry() {
        load(lastRequestedDate ?: Clock.System.todayIn(TimeZone.of("Asia/Seoul")))
    }

    fun load(date: LocalDate = Clock.System.todayIn(TimeZone.of("Asia/Seoul"))) {
        lastRequestedDate = date
        loadJob?.cancel()
        _uiState.value = InstructorScheduleUiState.Loading
        loadJob = viewModelScope.launch {
            try {
                val studio = studioContext.getSelectedStudio()
                val calendarDays =
                    repository.getCalendar(
                        studioId = studio.id,
                        from = date.minus(DatePeriod(days = 20)),
                        to = date.plus(DatePeriod(days = 20)),
                    )
                val sessions = repository.getDailySessions(studio.id, date).map { it.toClassSession() }
                _uiState.value = InstructorScheduleUiState.Success(sessions, calendarDays)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = InstructorScheduleUiState.Error(exception.message)
            }
        }
    }
}

internal sealed interface InstructorScheduleUiState {
    data object Loading : InstructorScheduleUiState

    data class Success(
        val sessions: List<ClassSession>,
        val calendarDays: List<InstructorCalendarDay>,
    ) : InstructorScheduleUiState

    data class Error(
        val message: String?,
    ) : InstructorScheduleUiState
}
