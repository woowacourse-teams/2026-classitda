package com.classitda.feature.student.mypage.mypass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.mypage.MyPassRepository
import com.classitda.feature.student.mypage.mypass.model.MyPassTab
import com.classitda.feature.student.mypage.mypass.model.MyPassTabState
import com.classitda.feature.student.mypage.mypass.model.MyPassUiState
import com.classitda.feature.student.mypage.mypass.util.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.classitda.domain.model.mypage.MyPassStatus as DomainMyPassStatus

class MyPassesViewModel(
    private val repository: MyPassRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPassUiState())
    val uiState: StateFlow<MyPassUiState> = _uiState.asStateFlow()

    init {
        loadInUsePasses()
    }

    fun onTabSelected(tab: MyPassTab) {
        if (_uiState.value.selectedTab == tab) return

        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            MyPassTab.IN_USE -> {
                if (_uiState.value.inUse is MyPassTabState.Initial) loadInUsePasses()
            }

            MyPassTab.EXPIRED -> {
                if (_uiState.value.expired is MyPassTabState.Initial) loadExpiredPasses()
            }
        }
    }

    fun onRetry(tab: MyPassTab) {
        when (tab) {
            MyPassTab.IN_USE -> loadInUsePasses()
            MyPassTab.EXPIRED -> loadExpiredPasses()
        }
    }

    // TODO: repository가 사용중/만료·종료 API로 나뉘면 각각 전용 조회 메서드를 호출하도록 교체
    private fun loadInUsePasses() {
        val current = _uiState.value.inUse
        if (current is MyPassTabState.Loading) return
        val previousPasses = (current as? MyPassTabState.Content)?.passes

        _uiState.update { it.copy(inUse = MyPassTabState.Loading(previousPasses)) }
        viewModelScope.launch {
            runCatching { repository.getMyPasses() }
                .onSuccess { passes ->
                    val inUsePasses =
                        passes
                            .filter { it.status == DomainMyPassStatus.IN_USE }
                            .map { it.toUiModel() }
                    _uiState.update { it.copy(inUse = MyPassTabState.Content(inUsePasses)) }
                }.onFailure { error ->
                    _uiState.update { it.copy(inUse = MyPassTabState.Error(error.message)) }
                }
        }
    }

    private fun loadExpiredPasses() {
        val current = _uiState.value.expired
        if (current is MyPassTabState.Loading) return
        val previousPasses = (current as? MyPassTabState.Content)?.passes

        _uiState.update { it.copy(expired = MyPassTabState.Loading(previousPasses)) }
        viewModelScope.launch {
            runCatching { repository.getMyPasses() }
                .onSuccess { passes ->
                    val expiredPasses =
                        passes
                            .filter { it.status != DomainMyPassStatus.IN_USE }
                            .map { it.toUiModel() }
                    _uiState.update { it.copy(expired = MyPassTabState.Content(expiredPasses)) }
                }.onFailure { error ->
                    _uiState.update { it.copy(expired = MyPassTabState.Error(error.message)) }
                }
        }
    }
}
