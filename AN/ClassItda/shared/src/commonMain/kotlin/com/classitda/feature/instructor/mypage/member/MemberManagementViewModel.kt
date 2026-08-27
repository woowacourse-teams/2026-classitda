package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.repository.instructor.membership.InstructorMembershipRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementActionState
import com.classitda.feature.instructor.mypage.contract.MemberManagementDeleteError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.toListError
import com.classitda.feature.instructor.mypage.toMemberListUiModel
import com.classitda.feature.instructor.mypage.toMemberManagementDeleteError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberManagementViewModel(
    private val repository: InstructorMembershipRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow<MemberManagementUiState>(MemberManagementUiState.Loading)
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()
    private var query = ""
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

    private fun load(
        showLoading: Boolean,
        actionState: MemberManagementActionState = MemberManagementActionState.Hidden,
    ) {
        if (showLoading) _uiState.value = MemberManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value = toUiState(loadAllMembers(), actionState)
        }
    }

    private suspend fun loadAllMembers(): InstructorMyPageResult<MemberListPage> {
        val studioId =
            try {
                studioContext.getSelectedStudio().id
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Throwable) {
                return InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNKNOWN)
            }
        val members = mutableListOf<ManagedMember>()
        var cursor: String? = null
        while (true) {
            when (val result = repository.getStudents(studioId, cursor, PAGE_SIZE)) {
                is InstructorMyPageResult.Failure -> {
                    return result
                }

                is InstructorMyPageResult.Success -> {
                    members += result.value.members
                    val nextCursor = result.value.nextPageCursor
                    if (nextCursor == null) break
                    if (nextCursor == cursor) {
                        return InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONTRACT)
                    }
                    cursor = nextCursor
                }
            }
        }
        return InstructorMyPageResult.Success(MemberListPage(members.size, members))
    }

    private fun toUiState(
        result: InstructorMyPageResult<MemberListPage>,
        actionState: MemberManagementActionState,
    ): MemberManagementUiState =
        when (result) {
            is InstructorMyPageResult.Success -> {
                val filteredMembers =
                    result.value.members.filter { member ->
                        query.isBlank() ||
                            member.name.contains(query, ignoreCase = true) ||
                            member.phoneNumber.contains(query, ignoreCase = true)
                    }
                val filteredPage = MemberListPage(filteredMembers.size, filteredMembers)
                when {
                    filteredMembers.isEmpty() && actionState is MemberManagementActionState.Deleted -> {
                        MemberManagementUiState.Content(filteredPage.toMemberListUiModel(), query, actionState)
                    }

                    filteredMembers.isEmpty() &&
                        query.isBlank() &&
                        actionState == MemberManagementActionState.Hidden -> {
                        MemberManagementUiState.Empty
                    }

                    filteredMembers.isEmpty() -> {
                        MemberManagementUiState.SearchEmpty(query)
                    }

                    else -> {
                        MemberManagementUiState.Content(filteredPage.toMemberListUiModel(), query, actionState)
                    }
                }
            }

            is InstructorMyPageResult.Failure -> {
                MemberManagementUiState.Error(result.reason.toListError())
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
        updateContent { copy(actionState = MemberManagementActionState.Submitting(memberId, typedName)) }
        viewModelScope.launch {
            when (val result = deleteMember(memberId)) {
                is InstructorMyPageResult.Success -> {
                    load(showLoading = false, actionState = MemberManagementActionState.Deleted(memberId))
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

    private suspend fun deleteMember(memberId: InstructorMemberId): InstructorMyPageResult<Unit> =
        try {
            repository.deleteMembership(studioContext.getSelectedStudio().id, memberId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNKNOWN)
        }

    private fun updateContent(transform: MemberManagementUiState.Content.() -> MemberManagementUiState.Content) {
        val content = _uiState.value as? MemberManagementUiState.Content ?: return
        _uiState.value = content.transform()
    }

    private fun updateDeleteState(transform: (MemberManagementActionState) -> MemberManagementActionState) {
        updateContent { copy(actionState = transform(actionState)) }
    }
}
