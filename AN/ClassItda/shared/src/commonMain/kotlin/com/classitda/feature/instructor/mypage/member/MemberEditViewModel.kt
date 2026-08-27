package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.repository.instructor.membership.InstructorMembershipRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.MemberEditAction
import com.classitda.feature.instructor.mypage.contract.MemberEditUiState
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.isMemberEditValid
import com.classitda.feature.instructor.mypage.contract.memberEditFieldErrors
import com.classitda.feature.instructor.mypage.toMemberEditError
import com.classitda.feature.instructor.mypage.toMemberInputUiModel
import com.classitda.feature.instructor.mypage.toMemberRegistrationDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberEditViewModel(
    private val repository: InstructorMembershipRepository,
    private val studioContext: InstructorStudioContext,
    private val memberId: InstructorMemberId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberEditUiState>(MemberEditUiState.Loading)
    val uiState: StateFlow<MemberEditUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: MemberEditAction) {
        when (action) {
            is MemberEditAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is MemberEditAction.PhoneNumberChanged -> {
                val state = _uiState.value as? MemberEditUiState.Editing ?: return
                if (state.draft.phoneNumberEditable) {
                    update { copy(phoneNumber = action.phoneNumber) }
                }
            }

            MemberEditAction.Submit -> {
                submit()
            }

            is MemberEditAction.SuccessAcknowledged -> {
                Unit
            }

            MemberEditAction.Retry -> {
                when (val current = _uiState.value) {
                    is MemberEditUiState.Error -> {
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

            MemberEditAction.Back -> {
                Unit
            }
        }
    }

    fun refresh() {
        _uiState.value = MemberEditUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = getMembership()) {
                    is InstructorMyPageResult.Success -> {
                        editing(
                            result.value.toMemberInputUiModel(),
                        )
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberEditUiState.Error(
                            memberId = memberId,
                            draft = MemberInputUiModel(),
                            reason = result.reason.toMemberEditError(),
                        )
                    }
                }
        }
    }

    private fun update(change: MemberInputUiModel.() -> MemberInputUiModel) {
        val state = _uiState.value as? MemberEditUiState.Editing ?: return
        _uiState.value = editing(state.draft.change()).copy(fieldErrors = emptySet())
    }

    private fun editing(draft: MemberInputUiModel) =
        MemberEditUiState.Editing(
            memberId = memberId,
            draft = draft,
            canSubmit = draft.isMemberEditValid(),
        )

    private fun submit() {
        val state = _uiState.value as? MemberEditUiState.Editing ?: return
        val fieldErrors = memberEditFieldErrors(state.draft)
        if (fieldErrors.isNotEmpty()) {
            _uiState.value = state.copy(canSubmit = false, fieldErrors = fieldErrors)
            return
        }
        val domainDraft = state.draft.toMemberRegistrationDraft()
        _uiState.value = MemberEditUiState.Submitting(memberId, state.draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = updateMembership(domainDraft)) {
                    is InstructorMyPageResult.Success -> {
                        MemberEditUiState.Success(memberId)
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberEditUiState.Error(
                            memberId = memberId,
                            draft = state.draft,
                            reason = result.reason.toMemberEditError(),
                            isSubmitFailure = true,
                        )
                    }
                }
        }
    }

    private suspend fun getMembership(): InstructorMyPageResult<ManagedMember> =
        try {
            repository.getMembership(studioContext.getSelectedStudio().id, memberId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNKNOWN)
        }

    private suspend fun updateMembership(draft: MemberRegistrationDraft): InstructorMyPageResult<Unit> =
        try {
            repository.updateStudent(studioContext.getSelectedStudio().id, memberId, draft)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNKNOWN)
        }
}
