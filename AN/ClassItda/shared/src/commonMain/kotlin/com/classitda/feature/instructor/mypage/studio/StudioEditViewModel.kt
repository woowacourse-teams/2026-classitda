package com.classitda.feature.instructor.mypage.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.feature.instructor.mypage.contract.StudioEditAction
import com.classitda.feature.instructor.mypage.contract.StudioEditUiError
import com.classitda.feature.instructor.mypage.contract.StudioEditUiState
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.isStudioRegistrationValid
import com.classitda.feature.instructor.mypage.contract.studioRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.toStudioEditError
import com.classitda.feature.instructor.mypage.toStudioInputUiModel
import com.classitda.feature.instructor.mypage.toStudioRegistrationDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class StudioEditViewModel(
    private val repository: InstructorStudioRepository,
    private val studioId: InstructorStudioId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<StudioEditUiState>(StudioEditUiState.Loading)
    val uiState: StateFlow<StudioEditUiState> = _uiState.asStateFlow()
    private var originalStudio: ManagedStudio? = null

    init {
        refresh()
    }

    fun onAction(action: StudioEditAction) {
        when (action) {
            is StudioEditAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is StudioEditAction.AddressChanged -> {
                update { copy(address = address.copy(roadAddress = action.address)) }
            }

            is StudioEditAction.DetailAddressChanged -> {
                update { copy(address = address.copy(detailAddress = action.detailAddress)) }
            }

            is StudioEditAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            is StudioEditAction.OpeningTimeChanged -> {
                update { copy(openingTime = action.openingTime) }
            }

            is StudioEditAction.ClosingTimeChanged -> {
                update { copy(closingTime = action.closingTime) }
            }

            is StudioEditAction.DescriptionChanged -> {
                update { copy(description = action.description) }
            }

            is StudioEditAction.ImageSelected -> {
                update { copy(image = action.image) }
            }

            StudioEditAction.RemoveImage -> {
                update { copy(image = null) }
            }

            is StudioEditAction.ImagePickerFailed -> {
                val state = _uiState.value as? StudioEditUiState.Editing ?: return
                _uiState.value = state.copy(imageError = action.reason)
            }

            is StudioEditAction.AddressSelected -> {
                update { copy(address = action.address.copy(detailAddress = "")) }
            }

            StudioEditAction.Submit -> {
                submit()
            }

            is StudioEditAction.SuccessAcknowledged -> {
                Unit
            }

            StudioEditAction.Retry -> {
                when (val current = _uiState.value) {
                    is StudioEditUiState.Error -> {
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

            StudioEditAction.Back,
            StudioEditAction.RequestImageSource,
            StudioEditAction.RequestAddressSearch,
            -> {
                Unit
            }
        }
    }

    fun refresh() {
        originalStudio = null
        _uiState.value = StudioEditUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getStudio(studioId)) {
                    is InstructorMyPageResult.Success -> {
                        originalStudio = result.value
                        editing(result.value.toStudioInputUiModel())
                    }

                    is InstructorMyPageResult.Failure -> {
                        StudioEditUiState.Error(
                            studioId = studioId,
                            draft = StudioInputUiModel(),
                            reason = result.reason.toStudioEditError(),
                        )
                    }
                }
        }
    }

    private fun update(change: StudioInputUiModel.() -> StudioInputUiModel) {
        val state = _uiState.value as? StudioEditUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: StudioInputUiModel) =
        StudioEditUiState.Editing(
            studioId = studioId,
            draft = draft,
            canSubmit = draft.isStudioRegistrationValid(),
        )

    private fun submit() {
        val state = _uiState.value as? StudioEditUiState.Editing ?: return
        val fieldErrors = studioRegistrationFieldErrors(state.draft)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
            return
        }
        val domainDraft = state.draft.toStudioRegistrationDraft()
        val original = originalStudio ?: return
        val imageMutation = original.imageMutationFor(domainDraft.image)
        if (imageMutation == null) {
            _uiState.value =
                state.copy(
                    canSubmit = false,
                    fieldErrors =
                        state.fieldErrors +
                            com.classitda.feature.instructor.mypage.contract.StudioRegistrationField.IMAGE,
                )
            return
        }
        _uiState.value = StudioEditUiState.Submitting(studioId, state.draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateStudio(studioId, original, domainDraft, imageMutation)) {
                    is InstructorMyPageResult.Success -> {
                        StudioEditUiState.Success(studioId)
                    }

                    is InstructorMyPageResult.Failure -> {
                        StudioEditUiState.Error(
                            studioId = studioId,
                            draft = state.draft,
                            reason = result.reason.toStudioEditError(),
                            isSubmitFailure = true,
                            completedOperations = result.completedStudioUpdateOperations,
                        )
                    }
                }
        }
    }
}

private fun ManagedStudio.imageMutationFor(selection: StudioImageSelection?): StudioImageMutation? =
    when (selection) {
        null -> if (image == null) StudioImageMutation.Unchanged else StudioImageMutation.Remove
        is StudioImageSelection.Local -> StudioImageMutation.Replace(selection)
        is StudioImageSelection.Remote -> if (image == selection) StudioImageMutation.Unchanged else null
    }
