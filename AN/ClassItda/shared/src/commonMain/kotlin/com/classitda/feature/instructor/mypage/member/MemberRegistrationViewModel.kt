package com.classitda.feature.instructor.mypage.member

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
import com.classitda.feature.instructor.mypage.toMemberRegistrationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberRegistrationViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberRegistrationUiState>(editing(MemberRegistrationDraft()))
    val uiState: StateFlow<MemberRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: MemberRegistrationAction) {
        when (action) {
            is MemberRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is MemberRegistrationAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            MemberRegistrationAction.OpenConfirmation -> {
                val state = _uiState.value as? MemberRegistrationUiState.Editing ?: return
                val fieldErrors = memberRegistrationFieldErrors(state.draft)
                if (fieldErrors.isEmpty()) {
                    _uiState.value = MemberRegistrationUiState.Confirmation(state.draft)
                } else {
                    _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
                }
            }

            MemberRegistrationAction.CancelConfirmation -> {
                when (val state = _uiState.value) {
                    is MemberRegistrationUiState.Confirmation -> {
                        _uiState.value = editing(state.draft)
                    }

                    is MemberRegistrationUiState.Error -> {
                        _uiState.value = editing(state.draft)
                    }

                    else -> {}
                }
            }

            MemberRegistrationAction.ConfirmRegistration -> {
                val state = _uiState.value as? MemberRegistrationUiState.Confirmation ?: return
                submit(state.draft)
            }

            MemberRegistrationAction.Retry -> {
                val state = _uiState.value as? MemberRegistrationUiState.Error ?: return
                submit(state.draft)
            }

            else -> {}
        }
    }

    private fun update(change: MemberRegistrationDraft.() -> MemberRegistrationDraft) {
        val state = _uiState.value as? MemberRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: MemberRegistrationDraft) =
        MemberRegistrationUiState.Editing(
            draft,
            draft.isMemberRegistrationValid(),
        )

    private fun submit(draft: MemberRegistrationDraft) {
        _uiState.value = MemberRegistrationUiState.Submitting(draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerMember(draft)) {
                    is InstructorMyPageResult.Success -> {
                        MemberRegistrationUiState.Success(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberRegistrationUiState.Error(
                            draft,
                            result.reason.toMemberRegistrationError(),
                        )
                    }
                }
        }
    }
}
