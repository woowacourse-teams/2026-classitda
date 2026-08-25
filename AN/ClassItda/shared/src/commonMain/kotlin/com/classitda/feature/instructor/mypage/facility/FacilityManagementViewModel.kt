package com.classitda.feature.instructor.mypage.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.toFacilityError
import com.classitda.feature.instructor.mypage.toFacilityListUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityManagementViewModel(
    private val repository: InstructorFacilityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityManagementUiState>(FacilityManagementUiState.Loading)
    val uiState: StateFlow<FacilityManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: FacilityManagementAction) {
        if (action == FacilityManagementAction.Retry) load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        _uiState.value = FacilityManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getFacilities()) {
                    is InstructorMyPageResult.Success -> {
                        if (result.value.facilities.isEmpty()) {
                            FacilityManagementUiState.Empty
                        } else {
                            FacilityManagementUiState
                                .Content(
                                    result.value.toFacilityListUiModel(),
                                )
                        }
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityManagementUiState.Error(result.reason.toFacilityError())
                    }
                }
        }
    }
}
