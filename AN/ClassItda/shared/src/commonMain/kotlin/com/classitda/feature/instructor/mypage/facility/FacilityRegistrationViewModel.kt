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
import com.classitda.feature.instructor.mypage.toFacilityRegistrationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityRegistrationViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityRegistrationUiState>(editing(FacilityRegistrationDraft()))
    val uiState: StateFlow<FacilityRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: FacilityRegistrationAction) {
        when (action) {
            is FacilityRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is FacilityRegistrationAction.AddressChanged -> {
                update { copy(address = action.address) }
            }

            is FacilityRegistrationAction.DetailAddressChanged -> {
                update { copy(detailAddress = action.detailAddress) }
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

            is FacilityRegistrationAction.ImagesSelected -> {
                update { copy(images = action.images.take(FacilityRegistrationDraft.MAX_IMAGE_COUNT)) }
            }

            is FacilityRegistrationAction.AddressSelected -> {
                update {
                    copy(
                        address = action.address,
                        detailAddress = action.detailAddress.ifBlank { detailAddress },
                    )
                }
            }

            FacilityRegistrationAction.Submit -> {
                submit()
            }

            FacilityRegistrationAction.Retry -> {
                (_uiState.value as? FacilityRegistrationUiState.Error)?.let {
                    _uiState.value =
                        editing(it.draft)
                }
            }

            else -> {
                Unit
            }
        }
    }

    private fun update(change: FacilityRegistrationDraft.() -> FacilityRegistrationDraft) {
        val state =
            _uiState.value as? FacilityRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: FacilityRegistrationDraft) =
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
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerFacility(state.draft)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityRegistrationUiState.Success(result.value)
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
