package com.classitda.feature.instructor.mypage.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.instructor.mypage.contract.MemberDeleteError
import com.classitda.feature.instructor.mypage.contract.MemberDeleteState
import com.classitda.feature.instructor.mypage.contract.MemberDetailAction
import com.classitda.feature.instructor.mypage.contract.MemberDetailUiState
import com.classitda.feature.instructor.mypage.toMemberDeleteError
import com.classitda.feature.instructor.mypage.toMemberDetailError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MemberDetailViewModel(
    private val repository: InstructorMyPageRepository,
    private val memberId: InstructorMemberId,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberDetailUiState>(MemberDetailUiState.Loading)
    val uiState: StateFlow<MemberDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: MemberDetailAction) {
        when (action) {
            MemberDetailAction.RequestDelete -> {
                updateContent {
                    copy(deleteState = MemberDeleteState.Confirming())
                }
            }

            is MemberDetailAction.DeleteNameChanged -> {
                updateContent {
                    when (val state = deleteState) {
                        is MemberDeleteState.Confirming -> {
                            copy(
                                deleteState = state.copy(typedName = action.name, error = null),
                            )
                        }

                        is MemberDeleteState.Failed -> {
                            copy(
                                deleteState = MemberDeleteState.Confirming(action.name),
                            )
                        }

                        else -> {
                            this
                        }
                    }
                }
            }

            MemberDetailAction.CancelDelete -> {
                updateContent {
                    copy(deleteState = MemberDeleteState.Hidden)
                }
            }

            MemberDetailAction.ConfirmDelete -> {
                confirmDelete()
            }

            MemberDetailAction.Retry -> {
                refresh()
            }

            MemberDetailAction.Back,
            MemberDetailAction.OpenEdit,
            -> {
                Unit
            }
        }
    }

    fun refresh() {
        _uiState.value = MemberDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getMember(memberId)) {
                    is InstructorMyPageResult.Success -> MemberDetailUiState.Content(result.value)
                    is InstructorMyPageResult.Failure -> MemberDetailUiState.Error(result.reason.toMemberDetailError())
                }
        }
    }

    private fun confirmDelete() {
        val content = _uiState.value as? MemberDetailUiState.Content ?: return
        val deleteState = content.deleteState
        val typedName =
            when (deleteState) {
                is MemberDeleteState.Confirming -> deleteState.typedName
                is MemberDeleteState.Failed -> deleteState.typedName
                else -> return
            }
        if (typedName != content.member.name) {
            _uiState.value =
                content.copy(
                    deleteState =
                        MemberDeleteState.Confirming(
                            typedName = typedName,
                            error = MemberDeleteError.NAME_MISMATCH,
                        ),
                )
            return
        }
        _uiState.value = content.copy(deleteState = MemberDeleteState.Submitting)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.deleteMember(memberId)) {
                    is InstructorMyPageResult.Success -> {
                        MemberDetailUiState.Deleted(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        content.copy(
                            deleteState =
                                MemberDeleteState.Failed(
                                    typedName = typedName,
                                    reason = result.reason.toMemberDeleteError(),
                                ),
                        )
                    }
                }
        }
    }

    private fun updateContent(transform: MemberDetailUiState.Content.() -> MemberDetailUiState.Content) {
        val content = _uiState.value as? MemberDetailUiState.Content ?: return
        _uiState.value = content.transform()
    }
}
