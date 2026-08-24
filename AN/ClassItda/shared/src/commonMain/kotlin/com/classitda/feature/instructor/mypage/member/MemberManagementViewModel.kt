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
import com.classitda.feature.instructor.mypage.contract.MemberManagementActionState
import com.classitda.feature.instructor.mypage.contract.MemberManagementDeleteError
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
import com.classitda.feature.instructor.mypage.toListError
import com.classitda.feature.instructor.mypage.toMemberManagementDeleteError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberManagementViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberManagementUiState>(MemberManagementUiState.Loading)
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()
    private var query = ""
    private var sort = MemberSortOrder.RECENTLY_REGISTERED

    init {
        load()
    }

    fun onAction(action: MemberManagementAction) {
        when (action) {
            is MemberManagementAction.QueryChanged -> {
                query = action.query
                load()
            }

            is MemberManagementAction.SortOrderChanged -> {
                sort = action.sortOrder
                load()
            }

            MemberManagementAction.Retry -> {
                load()
            }

            is MemberManagementAction.RequestDelete -> {
                updateContent { copy(actionState = MemberManagementActionState.Confirming(action.memberId)) }
            }

            is MemberManagementAction.DeleteNameChanged -> {
                updateDeleteState { state ->
                    when (state) {
                        is MemberManagementActionState.Confirming -> state.copy(typedName = action.name, error = null)
                        is MemberManagementActionState.Failed -> state.copy(typedName = action.name)
                        else -> state
                    }
                }
            }

            MemberManagementAction.CancelDelete -> {
                updateContent { copy(actionState = MemberManagementActionState.Hidden) }
            }

            MemberManagementAction.ConfirmDelete -> {
                confirmDelete()
            }

            else -> {
                Unit
            }
        }
    }

    fun refresh() {
        load()
    }

    private fun load() {
        _uiState.value =
            MemberManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getMembers(query, sort)) {
                    is InstructorMyPageResult.Success -> {
                        when {
                            result.value.totalCount == 0 && query.isBlank() -> MemberManagementUiState.Empty(sort)
                            result.value.members.isEmpty() -> MemberManagementUiState.SearchEmpty(query, sort)
                            else -> MemberManagementUiState.Content(result.value, query, sort)
                        }
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberManagementUiState.Error(result.reason.toListError())
                    }
                }
        }
    }

    private fun confirmDelete() {
        val content = _uiState.value as? MemberManagementUiState.Content ?: return
        val memberId: InstructorMemberId
        val typedName: String
        when (val actionState = content.actionState) {
            is MemberManagementActionState.Confirming -> {
                memberId = actionState.memberId
                typedName = actionState.typedName
            }

            is MemberManagementActionState.Failed -> {
                memberId = actionState.memberId
                typedName = actionState.typedName
            }

            else -> {
                return
            }
        }
        val member = content.page.members.firstOrNull { it.id == memberId } ?: return
        if (typedName != member.name) {
            updateContent {
                copy(
                    actionState =
                        MemberManagementActionState.Confirming(
                            memberId = memberId,
                            typedName = typedName,
                            error = MemberManagementDeleteError.NAME_MISMATCH,
                        ),
                )
            }
            return
        }
        updateContent {
            copy(
                actionState =
                    MemberManagementActionState.Submitting(
                        memberId = memberId,
                        typedName = typedName,
                    ),
            )
        }
        viewModelScope.launch {
            when (val result = repository.deleteMember(memberId)) {
                is InstructorMyPageResult.Success -> {
                    load()
                }

                is InstructorMyPageResult.Failure -> {
                    updateContent {
                        copy(
                            actionState =
                                MemberManagementActionState.Failed(
                                    memberId = memberId,
                                    typedName = typedName,
                                    reason = result.reason.toMemberManagementDeleteError(),
                                ),
                        )
                    }
                }
            }
        }
    }

    private fun updateContent(transform: MemberManagementUiState.Content.() -> MemberManagementUiState.Content) {
        val content = _uiState.value as? MemberManagementUiState.Content ?: return
        _uiState.value = content.transform()
    }

    private fun updateDeleteState(transform: (MemberManagementActionState) -> MemberManagementActionState) {
        updateContent { copy(actionState = transform(actionState)) }
    }
}
