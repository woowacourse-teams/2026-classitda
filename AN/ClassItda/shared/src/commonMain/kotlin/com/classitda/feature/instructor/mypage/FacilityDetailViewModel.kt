package com.classitda.feature.instructor.mypage

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class FacilityDetailViewModel(
    private val repository: InstructorMyPageRepository,
    private val facilityId: InstructorFacilityId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityDetailUiState>(FacilityDetailUiState.Loading)
    val uiState: StateFlow<FacilityDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: FacilityDetailAction) {
        when (action) {
            FacilityDetailAction.RequestDelete -> {
                updateContent { copy(deleteState = FacilityDeleteState.Confirming()) }
            }

            is FacilityDetailAction.DeleteNameChanged -> {
                updateContent {
                    val deleteState = deleteState
                    when (deleteState) {
                        is FacilityDeleteState.Confirming -> {
                            copy(deleteState = deleteState.copy(typedName = action.name, error = null))
                        }

                        is FacilityDeleteState.Failed -> {
                            copy(deleteState = FacilityDeleteState.Confirming(action.name))
                        }

                        else -> {
                            this
                        }
                    }
                }
            }

            FacilityDetailAction.CancelDelete -> {
                updateContent { copy(deleteState = FacilityDeleteState.Hidden) }
            }

            FacilityDetailAction.ConfirmDelete -> {
                confirmDelete()
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
                        FacilityDetailUiState.Content(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityDetailUiState.Error(
                            result.reason.toFacilityDetailError(),
                        )
                    }
                }
        }
    }

    private fun confirmDelete() {
        val content = _uiState.value as? FacilityDetailUiState.Content ?: return
        val typedName =
            when (val deleteState = content.deleteState) {
                is FacilityDeleteState.Confirming -> deleteState.typedName
                is FacilityDeleteState.Failed -> deleteState.typedName
                else -> return
            }
        if (typedName != content.facility.name) {
            _uiState.value =
                content.copy(
                    deleteState =
                        FacilityDeleteState.Confirming(
                            typedName = typedName,
                            error = FacilityDeleteError.NAME_MISMATCH,
                        ),
                )
            return
        }
        _uiState.value = content.copy(deleteState = FacilityDeleteState.Submitting)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.deleteFacility(facilityId)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityDetailUiState.Deleted(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        content.copy(
                            deleteState =
                                FacilityDeleteState.Failed(
                                    typedName = typedName,
                                    reason = result.reason.toFacilityDeleteError(),
                                ),
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
