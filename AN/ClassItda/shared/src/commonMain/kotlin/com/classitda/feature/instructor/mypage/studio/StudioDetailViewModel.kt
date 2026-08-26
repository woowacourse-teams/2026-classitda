package com.classitda.feature.instructor.mypage.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.feature.instructor.mypage.contract.StudioDetailAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiError
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiState
import com.classitda.feature.instructor.mypage.toStudioDetailError
import com.classitda.feature.instructor.mypage.toStudioUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StudioDetailViewModel(
    private val repository: InstructorStudioRepository,
    private val studioId: InstructorStudioId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudioDetailUiState>(StudioDetailUiState.Loading)
    val uiState: StateFlow<StudioDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: StudioDetailAction) {
        when (action) {
            StudioDetailAction.RequestDelete,
            is StudioDetailAction.DeleteNameChanged,
            StudioDetailAction.CancelDelete,
            StudioDetailAction.ConfirmDelete,
            is StudioDetailAction.DeleteAcknowledged,
            -> {
                Unit
            }

            StudioDetailAction.Retry -> {
                refresh()
            }

            StudioDetailAction.Back,
            StudioDetailAction.OpenEdit,
            -> {
                Unit
            }
        }
    }

    fun refresh() {
        _uiState.value = StudioDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getStudio(studioId)) {
                    is InstructorMyPageResult.Success -> {
                        StudioDetailUiState.Content(result.value.toStudioUiModel())
                    }

                    is InstructorMyPageResult.Failure -> {
                        StudioDetailUiState.Error(
                            result.reason.toStudioDetailError(),
                        )
                    }
                }
        }
    }

    private fun updateContent(transform: StudioDetailUiState.Content.() -> StudioDetailUiState.Content) {
        val content = _uiState.value as? StudioDetailUiState.Content ?: return
        _uiState.value = content.transform()
    }
}
