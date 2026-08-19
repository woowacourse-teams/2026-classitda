package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.ProfileViewAction
import com.classitda.feature.student.mypage.contract.ProfileViewUiState
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ProfileViewModel(
    private val repository: MyPageRepository,
    private val mapper: MyPageUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileViewUiState>(ProfileViewUiState.Loading)
    val uiState: StateFlow<ProfileViewUiState> = _uiState.asStateFlow()

    private var isLoading = false

    init {
        loadProfile()
    }

    fun onAction(action: ProfileViewAction) {
        if (action == ProfileViewAction.Retry) {
            loadProfile()
        }
    }

    private fun loadProfile() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ProfileViewUiState.Loading

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is MyPageResult.Success -> {
                        ProfileViewUiState.Content(
                            profile = result.value,
                            uiModel = mapper.mapProfile(result.value),
                        )
                    }

                    is MyPageResult.Failure -> {
                        ProfileViewUiState.Error(result.reason)
                    }
                }
            isLoading = false
        }
    }
}
