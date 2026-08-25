package com.classitda.feature.instructor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.feature.instructor.session.toClassSession
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal class InstructorHomeViewModel(
    private val repository: InstructorSessionRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorHomeUiState>(InstructorHomeUiState.Loading)
    val uiState: StateFlow<InstructorHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        _uiState.value = InstructorHomeUiState.Loading
        viewModelScope.launch {
            runCatching {
                val studio = studioContext.getSelectedStudio()
                val today = Clock.System.todayIn(TimeZone.of("Asia/Seoul"))
                coroutineScope {
                    (0..6)
                        .map { offset ->
                            async {
                                repository
                                    .getDailySessions(studio.id, today.plus(DatePeriod(days = offset)))
                                    .map { it.toClassSession() }
                            }
                        }.awaitAll()
                        .flatten()
                }
            }
                .onSuccess { sessions ->
                    _uiState.value = InstructorHomeUiState.Success(sessions.sortedBy { it.startAt })
                }.onFailure { error ->
                    _uiState.value = InstructorHomeUiState.Error(error.message)
                }
        }
    }
}

internal sealed interface InstructorHomeUiState {
    data object Loading : InstructorHomeUiState

    data class Success(
        val sessions: List<ClassSession>,
    ) : InstructorHomeUiState

    data class Error(
        val message: String?,
    ) : InstructorHomeUiState
}
