package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.repository.instructor.membership.InstructorMembershipRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.isMemberRegistrationValid
import com.classitda.feature.instructor.mypage.contract.memberRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.toMemberRegistrationDraft
import com.classitda.feature.instructor.mypage.toMemberRegistrationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberRegistrationViewModel(
    private val repository: InstructorMembershipRepository,
    private val studioContext: InstructorStudioContext,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberRegistrationUiState>(editing(MemberInputUiModel()))
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

    private fun update(change: MemberInputUiModel.() -> MemberInputUiModel) {
        val state = _uiState.value as? MemberRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: MemberInputUiModel) =
        MemberRegistrationUiState.Editing(
            draft,
            draft.isMemberRegistrationValid(),
        )

    private fun submit(draft: MemberInputUiModel) {
        val domainDraft = draft.toMemberRegistrationDraft()
        _uiState.value = MemberRegistrationUiState.Submitting(draft)
        viewModelScope.launch {
            val result =
                try {
                    repository.registerStudent(studioContext.getSelectedStudio().id, domainDraft)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Throwable) {
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNKNOWN)
                }
            _uiState.value =
                when (result) {
                    is InstructorMyPageResult.Success -> {
                        MemberRegistrationUiState.Success
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
