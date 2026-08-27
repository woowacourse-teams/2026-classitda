package com.classitda.feature.student.myschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.student.myschedule.MyScheduleFailureReason
import com.classitda.domain.repository.student.myschedule.MyScheduleRepository
import com.classitda.domain.repository.student.myschedule.MyScheduleResult
import com.classitda.feature.student.myschedule.contract.MyScheduleAction
import com.classitda.feature.student.myschedule.contract.MyScheduleListErrorUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleRefreshState
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleTabState
import com.classitda.feature.student.myschedule.contract.UsageHistoryTabState
import com.classitda.feature.student.myschedule.mapper.MyScheduleUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MyScheduleViewModel(
    private val repository: MyScheduleRepository,
    private val mapper: MyScheduleUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyScheduleUiState())
    val uiState: StateFlow<MyScheduleUiState> = _uiState.asStateFlow()

    init {
        loadUpcomingSchedules()
    }

    fun onAction(action: MyScheduleAction) {
        when (action) {
            is MyScheduleAction.SelectTab -> selectTab(action.tab)
            is MyScheduleAction.Retry -> retry(action.tab)
        }
    }

    private fun selectTab(tab: MyScheduleTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.update { state -> state.copy(selectedTab = tab) }
        when (tab) {
            MyScheduleTab.UPCOMING -> {
                if (_uiState.value.upcoming is UpcomingScheduleTabState.NotLoaded) {
                    loadUpcomingSchedules()
                }
            }

            MyScheduleTab.HISTORY -> {
                if (_uiState.value.usageHistory is UsageHistoryTabState.NotLoaded) {
                    loadUsageHistory()
                }
            }
        }
    }

    private fun retry(tab: MyScheduleTab) {
        when (tab) {
            MyScheduleTab.UPCOMING -> loadUpcomingSchedules()
            MyScheduleTab.HISTORY -> loadUsageHistory()
        }
    }

    private fun loadUpcomingSchedules() {
        val previousContent = _uiState.value.upcoming as? UpcomingScheduleTabState.Content
        if (_uiState.value.upcoming.isLoading()) return

        _uiState.update { state ->
            state.copy(
                upcoming =
                    previousContent
                        ?.copy(refreshState = MyScheduleRefreshState.Refreshing)
                        ?: UpcomingScheduleTabState.Loading,
            )
        }
        viewModelScope.launch {
            when (val result = repository.getUpcomingSchedules()) {
                is MyScheduleResult.Success -> {
                    val sections = mapper.mapUpcomingSchedules(result.value)
                    _uiState.update { state ->
                        state.copy(
                            upcoming =
                                if (sections.isEmpty()) {
                                    UpcomingScheduleTabState.Empty
                                } else {
                                    UpcomingScheduleTabState.Content(sections)
                                },
                        )
                    }
                }

                is MyScheduleResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            upcoming =
                                previousContent
                                    ?.copy(
                                        refreshState =
                                            MyScheduleRefreshState.Failed(
                                                result.reason.toListErrorUiModel(),
                                            ),
                                    )
                                    ?: UpcomingScheduleTabState.Error(result.reason.toListErrorUiModel()),
                        )
                    }
                }
            }
        }
    }

    private fun loadUsageHistory() {
        val previousContent = _uiState.value.usageHistory as? UsageHistoryTabState.Content
        if (_uiState.value.usageHistory.isLoading()) return

        _uiState.update { state ->
            state.copy(
                usageHistory =
                    previousContent
                        ?.copy(refreshState = MyScheduleRefreshState.Refreshing)
                        ?: UsageHistoryTabState.Loading,
            )
        }
        viewModelScope.launch {
            when (val result = repository.getUsageHistory()) {
                is MyScheduleResult.Success -> {
                    val sections = mapper.mapUsageHistory(result.value)
                    _uiState.update { state ->
                        state.copy(
                            usageHistory =
                                if (sections.isEmpty()) {
                                    UsageHistoryTabState.Empty
                                } else {
                                    UsageHistoryTabState.Content(sections)
                                },
                        )
                    }
                }

                is MyScheduleResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            usageHistory =
                                previousContent
                                    ?.copy(
                                        refreshState =
                                            MyScheduleRefreshState.Failed(
                                                result.reason.toListErrorUiModel(),
                                            ),
                                    )
                                    ?: UsageHistoryTabState.Error(result.reason.toListErrorUiModel()),
                        )
                    }
                }
            }
        }
    }

    private fun UpcomingScheduleTabState.isLoading(): Boolean =
        this is UpcomingScheduleTabState.Loading ||
            (this is UpcomingScheduleTabState.Content && refreshState is MyScheduleRefreshState.Refreshing)

    private fun UsageHistoryTabState.isLoading(): Boolean =
        this is UsageHistoryTabState.Loading ||
            (this is UsageHistoryTabState.Content && refreshState is MyScheduleRefreshState.Refreshing)

    private fun MyScheduleFailureReason.toListErrorUiModel(): MyScheduleListErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                MyScheduleListErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.NOT_FOUND,
            MyScheduleFailureReason.CONFLICT,
            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED,
            MyScheduleFailureReason.APPROVAL_NOT_ALLOWED,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                MyScheduleListErrorUiModel.UNKNOWN
            }
        }
}
