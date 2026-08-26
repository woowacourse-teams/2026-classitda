package com.classitda.feature.instructor.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import com.classitda.domain.repository.member.MemberRepository
import com.classitda.feature.instructor.session.toClassSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorHomeUiState>(InstructorHomeUiState.Loading)
    val uiState: StateFlow<InstructorHomeUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = InstructorHomeUiState.Loading
        loadJob =
            viewModelScope.launch {
                try {
                    val studio = studioContext.getSelectedStudio()
                    val instructorName =
                        runCatching { memberRepository.getMe().name.takeIf(String::isNotBlank) }
                            .getOrDefault("강사")
                            ?: "강사"
                    val today = Clock.System.todayIn(TimeZone.of("Asia/Seoul"))
                    val sessions =
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
                    _uiState.value =
                        InstructorHomeUiState.Success(
                            sessions = sessions.sortedBy { it.startAt },
                            instructorName = instructorName,
                        )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.value = InstructorHomeUiState.Error(exception.message)
                }
            }
    }
}

internal sealed interface InstructorHomeUiState {
    data object Loading : InstructorHomeUiState

    data class Success(
        val sessions: List<ClassSession>,
        val instructorName: String,
    ) : InstructorHomeUiState

    data class Error(
        val message: String?,
    ) : InstructorHomeUiState
}
