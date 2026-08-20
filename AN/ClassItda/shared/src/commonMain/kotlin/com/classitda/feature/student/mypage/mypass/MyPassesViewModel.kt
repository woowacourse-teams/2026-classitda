package com.classitda.feature.student.mypage.mypass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.mypage.MyPass
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

    // TODO: repository가 사용중/만료·종료 API로 나뉘면 이 캐시는 필요 없어진다.
    private var cachedPasses: List<MyPass>? = null

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
            MyPassTab.IN_USE -> loadInUsePasses(forceRefresh = true)
            MyPassTab.EXPIRED -> loadExpiredPasses(forceRefresh = true)
        }
    }

    private fun loadInUsePasses(forceRefresh: Boolean = false) {
        loadTab(
            forceRefresh = forceRefresh,
            currentState = _uiState.value.inUse,
            filter = { it.status == DomainMyPassStatus.IN_USE },
            updateState = { state -> _uiState.update { it.copy(inUse = state) } },
        )
    }

    private fun loadExpiredPasses(forceRefresh: Boolean = false) {
        loadTab(
            forceRefresh = forceRefresh,
            currentState = _uiState.value.expired,
            filter = { it.status != DomainMyPassStatus.IN_USE },
            updateState = { state -> _uiState.update { it.copy(expired = state) } },
        )
    }

    private fun loadTab(
        forceRefresh: Boolean,
        currentState: MyPassTabState,
        filter: (MyPass) -> Boolean,
        updateState: (MyPassTabState) -> Unit,
    ) {
        if (currentState is MyPassTabState.Loading) return
        val previousPasses = (currentState as? MyPassTabState.Content)?.passes

        updateState(MyPassTabState.Loading(previousPasses))
        viewModelScope.launch {
            runCatching { fetchPasses(forceRefresh) }
                .onSuccess { passes ->
                    updateState(MyPassTabState.Content(passes.filter(filter).map { it.toUiModel() }))
                }.onFailure { error ->
                    updateState(MyPassTabState.Error(error.message))
                }
        }
    }

    // 사용중/만료·종료 탭이 같은 목록을 나눠 보여줄 뿐이라, 강제 새로고침이 아니면 캐시를 공유해
    // 탭을 처음 전환할 때마다 동일한 데이터를 중복 조회하지 않도록 한다.
    private suspend fun fetchPasses(forceRefresh: Boolean): List<MyPass> {
        val cached = cachedPasses
        if (!forceRefresh && cached != null) return cached
        return repository.getMyPasses().also { cachedPasses = it }
    }
}
