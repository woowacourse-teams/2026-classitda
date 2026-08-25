package com.classitda.feature.instructor.mypage.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiError
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.toFacilityDetailError
import com.classitda.feature.instructor.mypage.toFacilityUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityDetailViewModel(
    private val repository: InstructorFacilityRepository,
    private val facilityId: InstructorFacilityId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityDetailUiState>(FacilityDetailUiState.Loading)
    val uiState: StateFlow<FacilityDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: FacilityDetailAction) {
        when (action) {
            FacilityDetailAction.RequestDelete,
            is FacilityDetailAction.DeleteNameChanged,
            FacilityDetailAction.CancelDelete,
            FacilityDetailAction.ConfirmDelete,
            is FacilityDetailAction.DeleteAcknowledged,
            -> {
                Unit
            }

            FacilityDetailAction.Retry -> {
                refresh()
            }

            FacilityDetailAction.Back,
            FacilityDetailAction.OpenEdit,
            -> {
                Unit
            }
        }
    }

    fun refresh() {
        _uiState.value = FacilityDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getFacility(facilityId)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityDetailUiState.Content(result.value.toFacilityUiModel())
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityDetailUiState.Error(
                            result.reason.toFacilityDetailError(),
                        )
                    }
                }
        }
    }

    private fun updateContent(transform: FacilityDetailUiState.Content.() -> FacilityDetailUiState.Content) {
        val content = _uiState.value as? FacilityDetailUiState.Content ?: return
        _uiState.value = content.transform()
    }
}
