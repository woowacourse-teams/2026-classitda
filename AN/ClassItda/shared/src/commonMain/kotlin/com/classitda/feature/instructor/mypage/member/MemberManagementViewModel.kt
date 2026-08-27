package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.studio.StudioId
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
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

internal class MemberManagementViewModel(
    private val repository: InstructorMembershipRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow<MemberManagementUiState>(MemberManagementUiState.Loading)
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()
    private var allMembers: List<ManagedMember> = emptyList()
    private var cachedStudioId: StudioId? = null
    private var hasLoadedMembers = false
    private var query = ""
    private var loadJob: Job? = null

    private sealed interface StudioResolution {
        data class Selected(
            val id: StudioId,
        ) : StudioResolution

        data object NoStudio : StudioResolution

        data class Failed(
            val reason: InstructorMyPageFailureReason,
        ) : StudioResolution
    }

    init {
        load(showLoading = true)
    }

    fun onAction(action: MemberManagementAction) {
        when (action) {
            is MemberManagementAction.QueryChanged -> {
                query = action.query
                if (hasLoadedMembers) {
                    _uiState.value = toUiState(currentActionState())
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
        loadJob?.cancel()
        if (showLoading) _uiState.value = MemberManagementUiState.Loading
        loadJob =
            viewModelScope.launch {
                when (val studio = resolveStudio()) {
                    StudioResolution.NoStudio -> {
                        allMembers = emptyList()
                        cachedStudioId = null
                        hasLoadedMembers = false
                        _uiState.value = MemberManagementUiState.NoStudio
                        return@launch
                    }

                    is StudioResolution.Failed -> {
                        allMembers = emptyList()
                        cachedStudioId = null
                        hasLoadedMembers = false
                        _uiState.value = MemberManagementUiState.Error(studio.reason.toListError())
                        return@launch
                    }

                    is StudioResolution.Selected -> {
                        loadMembersForStudio(studio.id, actionState)
                    }
                }
            }
    }

    private suspend fun loadMembersForStudio(
        studioId: StudioId,
        actionState: MemberManagementActionState,
    ) {
        if (cachedStudioId != studioId) {
            allMembers = emptyList()
            hasLoadedMembers = false
            cachedStudioId = studioId
        }
        when (val result = loadAllMembers(studioId)) {
            is InstructorMyPageResult.Success -> {
                allMembers = result.value.members
                hasLoadedMembers = true
                _uiState.value = toUiState(actionState)
            }

            is InstructorMyPageResult.Failure -> {
                hasLoadedMembers = false
                _uiState.value = MemberManagementUiState.Error(result.reason.toListError())
            }
        }
    }

    private suspend fun resolveStudio(): StudioResolution =
        try {
            if (studioContext.getStudios().isEmpty()) {
                StudioResolution.NoStudio
            } else {
                StudioResolution.Selected(studioContext.getSelectedStudio().id)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ResponseException) {
            StudioResolution.Failed(exception.toStudioFailureReason())
        } catch (exception: Throwable) {
            StudioResolution.Failed(exception.toStudioFailureReason())
        }

    private suspend fun loadAllMembers(studioId: StudioId): InstructorMyPageResult<MemberListPage> {
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

    private fun toUiState(actionState: MemberManagementActionState): MemberManagementUiState {
        val filteredMembers = filterMembers(allMembers, query)
        val filteredPage = MemberListPage(allMembers.size, filteredMembers)
        return when {
            filteredMembers.isEmpty() && actionState is MemberManagementActionState.Deleted -> {
                MemberManagementUiState.Content(filteredPage.toMemberListUiModel(), query, actionState)
            }

            filteredMembers.isEmpty() && query.isBlank() && actionState == MemberManagementActionState.Hidden -> {
                MemberManagementUiState.Empty
            }

            else -> {
                MemberManagementUiState.Content(filteredPage.toMemberListUiModel(), query, actionState)
            }
        }
    }

    private fun currentActionState(): MemberManagementActionState =
        (_uiState.value as? MemberManagementUiState.Content)?.actionState
            ?: MemberManagementActionState.Hidden

    private fun filterMembers(
        members: List<ManagedMember>,
        query: String,
    ): List<ManagedMember> {
        val nameQuery = query.trim()
        val phoneQuery = query.filter(Char::isDigit)
        return members.filter { member ->
            nameQuery.isBlank() ||
                member.name.contains(nameQuery, ignoreCase = true) ||
                (phoneQuery.isNotBlank() && member.phoneNumber.filter(Char::isDigit).contains(phoneQuery))
        }
    }

    private fun ResponseException.toStudioFailureReason(): InstructorMyPageFailureReason =
        when (response.status.value) {
            400 -> InstructorMyPageFailureReason.INVALID_REQUEST
            401 -> InstructorMyPageFailureReason.UNAUTHORIZED
            403 -> InstructorMyPageFailureReason.FORBIDDEN
            404 -> InstructorMyPageFailureReason.NOT_FOUND
            in 500..599 -> InstructorMyPageFailureReason.SERVER
            else -> InstructorMyPageFailureReason.UNKNOWN
        }

    private fun Throwable.toStudioFailureReason(): InstructorMyPageFailureReason =
        when (this) {
            is JsonConvertException, is SerializationException -> InstructorMyPageFailureReason.CONTRACT
            is IOException -> InstructorMyPageFailureReason.NETWORK
            else -> InstructorMyPageFailureReason.UNKNOWN
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
