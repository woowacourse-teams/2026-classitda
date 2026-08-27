package com.classitda.feature.instructor.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorMyPageViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorMyPageUiState>(InstructorMyPageUiState.Loading)
    val uiState: StateFlow<InstructorMyPageUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: InstructorMyPageAction) {
        if (action == InstructorMyPageAction.Retry) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getSummary()) {
                    is InstructorMyPageResult.Success -> {
                        InstructorMyPageUiState.Content(
                            result.value.profile.toUiModel(),
                        )
                    }

                    is InstructorMyPageResult.Failure -> {
                        InstructorMyPageUiState.Error(result.reason.toMyPageError())
                    }
                }
        }
    }
}
