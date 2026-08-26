package com.classitda.feature.instructor.mypage.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.feature.instructor.mypage.contract.StudioImageUiError
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.isStudioRegistrationValid
import com.classitda.feature.instructor.mypage.contract.studioRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.toStudioRegistrationDraft
import com.classitda.feature.instructor.mypage.toStudioRegistrationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StudioRegistrationViewModel(
    private val repository: InstructorStudioRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudioRegistrationUiState>(editing(StudioInputUiModel()))
    val uiState: StateFlow<StudioRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: StudioRegistrationAction) {
        when (action) {
            is StudioRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is StudioRegistrationAction.AddressChanged -> {
                update { copy(address = address.copy(roadAddress = action.address)) }
            }

            is StudioRegistrationAction.DetailAddressChanged -> {
                update { copy(address = address.copy(detailAddress = action.detailAddress)) }
            }

            is StudioRegistrationAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            is StudioRegistrationAction.OpeningTimeChanged -> {
                update { copy(openingTime = action.openingTime) }
            }

            is StudioRegistrationAction.ClosingTimeChanged -> {
                update { copy(closingTime = action.closingTime) }
            }

            is StudioRegistrationAction.DescriptionChanged -> {
                update { copy(description = action.description) }
            }

            is StudioRegistrationAction.ImageSelected -> {
                update { copy(image = action.image) }
            }

            StudioRegistrationAction.RemoveImage -> {
                update { copy(image = null) }
            }

            is StudioRegistrationAction.ImagePickerFailed -> {
                val state = _uiState.value as? StudioRegistrationUiState.Editing ?: return
                _uiState.value = state.copy(imageError = action.reason)
            }

            is StudioRegistrationAction.AddressSelected -> {
                update { copy(address = action.address.copy(detailAddress = "")) }
            }

            StudioRegistrationAction.Submit -> {
                submit()
            }

            StudioRegistrationAction.Retry -> {
                (_uiState.value as? StudioRegistrationUiState.Error)?.let { error ->
                    _uiState.value = editing(error.draft)
                    submit()
                }
            }

            else -> {
                Unit
            }
        }
    }

    private fun update(change: StudioInputUiModel.() -> StudioInputUiModel) {
        val state =
            _uiState.value as? StudioRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: StudioInputUiModel) =
        StudioRegistrationUiState.Editing(
            draft,
            draft.isStudioRegistrationValid(),
        )

    private fun submit() {
        val state =
            _uiState.value as? StudioRegistrationUiState.Editing ?: return
        val fieldErrors = studioRegistrationFieldErrors(state.draft)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
            return
        }
        _uiState.value =
            StudioRegistrationUiState.Submitting
        val domainDraft = state.draft.toStudioRegistrationDraft()
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerStudio(domainDraft)) {
                    is InstructorMyPageResult.Success -> {
                        StudioRegistrationUiState.Success
                    }

                    is InstructorMyPageResult.Failure -> {
                        StudioRegistrationUiState.Error(
                            state.draft,
                            result.reason.toStudioRegistrationError(),
                        )
                    }
                }
        }
    }
}
