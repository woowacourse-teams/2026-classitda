package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.MemberEditAction
import com.classitda.feature.instructor.mypage.contract.MemberEditUiState
import com.classitda.feature.instructor.mypage.contract.isMemberEditValid
import com.classitda.feature.instructor.mypage.contract.memberEditFieldErrors
import com.classitda.feature.instructor.mypage.toMemberEditError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberEditViewModel(
    private val repository: InstructorMyPageRepository,
    private val memberId: InstructorMemberId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberEditUiState>(MemberEditUiState.Loading)
    val uiState: StateFlow<MemberEditUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: MemberEditAction) {
        when (action) {
            is MemberEditAction.NameChanged -> update { copy(name = action.name) }
            is MemberEditAction.PhoneNumberChanged -> update { copy(phoneNumber = action.phoneNumber) }
            MemberEditAction.Submit -> submit()
            is MemberEditAction.SuccessAcknowledged -> Unit
            MemberEditAction.Retry -> refresh()
            MemberEditAction.Back -> Unit
        }
    }

    fun refresh() {
        _uiState.value = MemberEditUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getMember(memberId)) {
                    is InstructorMyPageResult.Success -> {
                        editing(
                            MemberRegistrationDraft(result.value.name, result.value.phoneNumber),
                        )
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberEditUiState.Error(
                            memberId = memberId,
                            draft = MemberRegistrationDraft(),
                            reason = result.reason.toMemberEditError(),
                        )
                    }
                }
        }
    }

    private fun update(change: MemberRegistrationDraft.() -> MemberRegistrationDraft) {
        val state = _uiState.value as? MemberEditUiState.Editing ?: return
        _uiState.value = editing(state.draft.change()).copy(fieldErrors = emptySet())
    }

    private fun editing(draft: MemberRegistrationDraft) =
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
        _uiState.value = MemberEditUiState.Submitting(memberId, state.draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateMember(memberId, state.draft)) {
                    is InstructorMyPageResult.Success -> {
                        MemberEditUiState.Success(result.value.id)
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberEditUiState.Error(
                            memberId = memberId,
                            draft = state.draft,
                            reason = result.reason.toMemberEditError(),
                        )
                    }
                }
        }
    }
}
