package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
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
import com.classitda.feature.instructor.mypage.toDomain
import com.classitda.feature.instructor.mypage.toListError
import com.classitda.feature.instructor.mypage.toMemberListUiModel
import com.classitda.feature.instructor.mypage.toMemberManagementDeleteError
import com.classitda.feature.instructor.mypage.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class MemberManagementViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberManagementUiState>(MemberManagementUiState.Loading)
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()
    private var query = ""
    private var sort = MemberSortOrder.RECENTLY_REGISTERED
    private var queryJob: Job? = null

    init {
        load(showLoading = true)
    }

    fun onAction(action: MemberManagementAction) {
        when (action) {
            is MemberManagementAction.QueryChanged -> {
                query = action.query
                queryJob?.cancel()
                queryJob =
                    viewModelScope.launch {
                        delay(300)
                        load(showLoading = false)
                    }
            }

            is MemberManagementAction.SortOrderChanged -> {
                sort = action.sortOrder.toDomain()
                load(showLoading = false)
            }

            MemberManagementAction.Retry -> {
                load(showLoading = true)
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

            MemberManagementAction.DeleteAcknowledged -> {
                refresh()
            }

            else -> {
                Unit
            }
        }
    }

    fun refresh() {
        load(showLoading = false)
    }

    private fun load(showLoading: Boolean) {
        if (showLoading) {
            _uiState.value = MemberManagementUiState.Loading
        }
        viewModelScope.launch {
            _uiState.value = toUiState(repository.getMembers(query, sort))
        }
    }

    private fun toUiState(result: InstructorMyPageResult<com.classitda.domain.model.instructor.mypage.MemberListPage>): MemberManagementUiState =
        when (result) {
            is InstructorMyPageResult.Success -> {
                when {
                    result.value.totalCount == 0 && query.isBlank() -> MemberManagementUiState.Empty(sort.toUiModel())
                    result.value.members.isEmpty() -> MemberManagementUiState.SearchEmpty(query, sort.toUiModel())
                    else -> MemberManagementUiState.Content(result.value.toMemberListUiModel(), query, sort.toUiModel())
                }
            }

            is InstructorMyPageResult.Failure -> MemberManagementUiState.Error(result.reason.toListError())
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
                    when (val refreshed = repository.getMembers(query, sort)) {
                        is InstructorMyPageResult.Success -> {
                            _uiState.value =
                                MemberManagementUiState.Content(
                                    page = refreshed.value.toMemberListUiModel(),
                                    query = query,
                                    sortOrder = sort.toUiModel(),
                                    actionState = MemberManagementActionState.Deleted(memberId),
                                )
                        }

                        is InstructorMyPageResult.Failure -> {
                            _uiState.value = MemberManagementUiState.Error(refreshed.reason.toListError())
                        }
                    }
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
