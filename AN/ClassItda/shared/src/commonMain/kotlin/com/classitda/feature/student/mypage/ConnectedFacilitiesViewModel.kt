package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesAction
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesUiState
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ConnectedFacilitiesViewModel(
    private val repository: MyPageRepository,
    private val mapper: MyPageUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ConnectedFacilitiesUiState>(ConnectedFacilitiesUiState.Loading)
    val uiState: StateFlow<ConnectedFacilitiesUiState> = _uiState.asStateFlow()

    private var isLoading = false

    init {
        loadFacilities()
    }

    fun onAction(action: ConnectedFacilitiesAction) {
        if (action == ConnectedFacilitiesAction.Retry) {
            loadFacilities()
        }
    }

    private fun loadFacilities() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ConnectedFacilitiesUiState.Loading

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getConnectedFacilities()) {
                    is MyPageResult.Success -> {
                        if (result.value.isEmpty()) {
                            ConnectedFacilitiesUiState.Empty
                        } else {
                            ConnectedFacilitiesUiState.Content(
                                facilities = result.value,
                                uiModels = mapper.mapConnectedFacilities(result.value),
                            )
                        }
                    }

                    is MyPageResult.Failure -> {
                        ConnectedFacilitiesUiState.Error(result.reason)
                    }
                }
            isLoading = false
        }
    }
}
