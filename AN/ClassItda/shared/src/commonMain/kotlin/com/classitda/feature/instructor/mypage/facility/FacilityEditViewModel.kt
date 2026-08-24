package com.classitda.feature.instructor.mypage.facility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiError
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileUiError
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.common.profile.contract.ProfileViewUiState
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteError
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteState
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiError
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiError
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.contract.isMemberRegistrationValid
import com.classitda.feature.instructor.mypage.contract.memberRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.toDraft
import com.classitda.feature.instructor.mypage.toFacilityEditError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityEditViewModel(
    private val repository: InstructorMyPageRepository,
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
                update { copy(address = action.address) }
            }

            is FacilityEditAction.DetailAddressChanged -> {
                update { copy(detailAddress = action.detailAddress) }
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

            is FacilityEditAction.ImagesSelected -> {
                update { copy(images = action.images.take(FacilityRegistrationDraft.MAX_IMAGE_COUNT)) }
            }

            is FacilityEditAction.AddressSelected -> {
                update {
                    copy(
                        address = action.address,
                        detailAddress = action.detailAddress.ifBlank { detailAddress },
                    )
                }
            }

            FacilityEditAction.Submit -> {
                submit()
            }

            is FacilityEditAction.SuccessAcknowledged -> {
                Unit
            }

            FacilityEditAction.Retry -> {
                refresh()
            }

            FacilityEditAction.Back,
            FacilityEditAction.RequestImages,
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
                        editing(result.value.toDraft())
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityEditUiState.Error(
                            facilityId = facilityId,
                            draft = FacilityRegistrationDraft(),
                            reason = result.reason.toFacilityEditError(),
                        )
                    }
                }
        }
    }

    private fun update(change: FacilityRegistrationDraft.() -> FacilityRegistrationDraft) {
        val state = _uiState.value as? FacilityEditUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: FacilityRegistrationDraft) =
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
        _uiState.value = FacilityEditUiState.Submitting(facilityId, state.draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateFacility(facilityId, state.draft)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityEditUiState.Success(result.value.id)
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityEditUiState.Error(
                            facilityId = facilityId,
                            draft = state.draft,
                            reason = result.reason.toFacilityEditError(),
                        )
                    }
                }
        }
    }
}
