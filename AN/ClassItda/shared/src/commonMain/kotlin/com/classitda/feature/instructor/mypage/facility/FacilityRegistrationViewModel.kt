package com.classitda.feature.instructor.mypage.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.toFacilityRegistrationDraft
import com.classitda.feature.instructor.mypage.toFacilityRegistrationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityRegistrationViewModel(
    private val repository: InstructorFacilityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityRegistrationUiState>(editing(FacilityInputUiModel()))
    val uiState: StateFlow<FacilityRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: FacilityRegistrationAction) {
        when (action) {
            is FacilityRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is FacilityRegistrationAction.AddressChanged -> {
                update { copy(address = address.copy(roadAddress = action.address)) }
            }

            is FacilityRegistrationAction.DetailAddressChanged -> {
                update { copy(address = address.copy(detailAddress = action.detailAddress)) }
            }

            is FacilityRegistrationAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            is FacilityRegistrationAction.OpeningTimeChanged -> {
                update { copy(openingTime = action.openingTime) }
            }

            is FacilityRegistrationAction.ClosingTimeChanged -> {
                update { copy(closingTime = action.closingTime) }
            }

            is FacilityRegistrationAction.DescriptionChanged -> {
                update { copy(description = action.description) }
            }

            is FacilityRegistrationAction.ImageSelected -> {
                update { copy(image = action.image) }
            }

            FacilityRegistrationAction.RemoveImage -> {
                update { copy(image = null) }
            }

            is FacilityRegistrationAction.AddressSelected -> {
                update { copy(address = action.address.copy(detailAddress = "")) }
            }

            FacilityRegistrationAction.Submit -> {
                submit()
            }

            FacilityRegistrationAction.Retry -> {
                (_uiState.value as? FacilityRegistrationUiState.Error)?.let { error ->
                    _uiState.value = editing(error.draft)
                    submit()
                }
            }

            else -> {
                Unit
            }
        }
    }

    private fun update(change: FacilityInputUiModel.() -> FacilityInputUiModel) {
        val state =
            _uiState.value as? FacilityRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: FacilityInputUiModel) =
        FacilityRegistrationUiState.Editing(
            draft,
            draft.isFacilityRegistrationValid(),
        )

    private fun submit() {
        val state =
            _uiState.value as? FacilityRegistrationUiState.Editing ?: return
        val fieldErrors = facilityRegistrationFieldErrors(state.draft)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
            return
        }
        _uiState.value =
            FacilityRegistrationUiState.Submitting
        val domainDraft = state.draft.toFacilityRegistrationDraft()
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerFacility(domainDraft)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityRegistrationUiState.Success
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityRegistrationUiState.Error(
                            state.draft,
                            result.reason.toFacilityRegistrationError(),
                        )
                    }
                }
        }
    }
}
