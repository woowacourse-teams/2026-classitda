package com.classitda.feature.instructor.mypage.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiError
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.toFacilityEditError
import com.classitda.feature.instructor.mypage.toFacilityInputUiModel
import com.classitda.feature.instructor.mypage.toFacilityRegistrationDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityEditViewModel(
    private val repository: InstructorFacilityRepository,
    private val facilityId: InstructorFacilityId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityEditUiState>(FacilityEditUiState.Loading)
    val uiState: StateFlow<FacilityEditUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: FacilityEditAction) {
        when (action) {
            is FacilityEditAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is FacilityEditAction.AddressChanged -> {
                update { copy(address = address.copy(roadAddress = action.address)) }
            }

            is FacilityEditAction.DetailAddressChanged -> {
                update { copy(address = address.copy(detailAddress = action.detailAddress)) }
            }

            is FacilityEditAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            is FacilityEditAction.OpeningTimeChanged -> {
                update { copy(openingTime = action.openingTime) }
            }

            is FacilityEditAction.ClosingTimeChanged -> {
                update { copy(closingTime = action.closingTime) }
            }

            is FacilityEditAction.DescriptionChanged -> {
                update { copy(description = action.description) }
            }

            is FacilityEditAction.ImageSelected -> {
                update { copy(image = action.image) }
            }

            FacilityEditAction.RemoveImage -> {
                update { copy(image = null) }
            }

            is FacilityEditAction.AddressSelected -> {
                update { copy(address = action.address.copy(detailAddress = "")) }
            }

            FacilityEditAction.Submit -> {
                submit()
            }

            is FacilityEditAction.SuccessAcknowledged -> {
                Unit
            }

            FacilityEditAction.Retry -> {
                when (val current = _uiState.value) {
                    is FacilityEditUiState.Error -> {
                        if (current.isSubmitFailure) {
                            _uiState.value = editing(current.draft)
                            submit()
                        } else {
                            refresh()
                        }
                    }

                    else -> {
                        refresh()
                    }
                }
            }

            FacilityEditAction.Back,
            FacilityEditAction.RequestImageSource,
            FacilityEditAction.RequestAddressSearch,
            -> {
                Unit
            }
        }
    }

    fun refresh() {
        _uiState.value = FacilityEditUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getFacility(facilityId)) {
                    is InstructorMyPageResult.Success -> {
                        editing(result.value.toFacilityInputUiModel())
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityEditUiState.Error(
                            facilityId = facilityId,
                            draft = FacilityInputUiModel(),
                            reason = result.reason.toFacilityEditError(),
                        )
                    }
                }
        }
    }

    private fun update(change: FacilityInputUiModel.() -> FacilityInputUiModel) {
        val state = _uiState.value as? FacilityEditUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: FacilityInputUiModel) =
        FacilityEditUiState.Editing(
            facilityId = facilityId,
            draft = draft,
            canSubmit = draft.isFacilityRegistrationValid(),
        )

    private fun submit() {
        val state = _uiState.value as? FacilityEditUiState.Editing ?: return
        val fieldErrors = facilityRegistrationFieldErrors(state.draft)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
            return
        }
        val domainDraft = state.draft.toFacilityRegistrationDraft()
        _uiState.value = FacilityEditUiState.Submitting(facilityId, state.draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateFacility(facilityId, domainDraft)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityEditUiState.Success(facilityId)
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityEditUiState.Error(
                            facilityId = facilityId,
                            draft = state.draft,
                            reason = result.reason.toFacilityEditError(),
                            isSubmitFailure = true,
                        )
                    }
                }
        }
    }
}
