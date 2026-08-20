package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.MyPageAction
import com.classitda.feature.student.mypage.contract.MyPageContentState
import com.classitda.feature.student.mypage.contract.MyPageUiState
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MyPageViewModel(
    private val repository: MyPageRepository,
    private val mapper: MyPageUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private var isLoading = false

    init {
        loadSummary()
    }

    fun onAction(action: MyPageAction) {
        if (action == MyPageAction.Retry) {
            loadSummary()
        }
    }

    private fun loadSummary() {
        if (isLoading) return
        isLoading = true
        _uiState.value = MyPageUiState(content = MyPageContentState.Loading)

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getSummary()) {
                    is MyPageResult.Success -> {
                        MyPageContentState.Content(
                            summary = result.value,
                            uiModel = mapper.mapSummary(result.value),
                        )
                    }

                    is MyPageResult.Failure -> {
                        MyPageContentState.Error(result.reason)
                    }
                }.let(::MyPageUiState)
            isLoading = false
        }
    }
}
