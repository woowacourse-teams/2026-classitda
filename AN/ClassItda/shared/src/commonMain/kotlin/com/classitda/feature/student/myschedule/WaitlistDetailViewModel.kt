package com.classitda.feature.student.myschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.domain.repository.student.myschedule.MyScheduleFailureReason
import com.classitda.domain.repository.student.myschedule.MyScheduleRepository
import com.classitda.domain.repository.student.myschedule.MyScheduleResult
import com.classitda.feature.student.myschedule.contract.WaitlistApprovalDialogUiState
import com.classitda.feature.student.myschedule.contract.WaitlistApprovalErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailAction
import com.classitda.feature.student.myschedule.contract.WaitlistDetailErrorUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailStatusUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiState
import com.classitda.feature.student.myschedule.contract.canDismiss
import com.classitda.feature.student.myschedule.mapper.MyScheduleUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class WaitlistDetailViewModel(
    private val waitlistId: WaitlistId,
    private val repository: MyScheduleRepository,
    private val mapper: MyScheduleUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WaitlistDetailUiState>(WaitlistDetailUiState.Loading)
    val uiState: StateFlow<WaitlistDetailUiState> = _uiState.asStateFlow()

    private var isDetailLoading = false

    init {
        loadDetail()
    }

    fun onAction(action: WaitlistDetailAction) {
        when (action) {
            WaitlistDetailAction.Retry -> {
                retryDetail()
            }

            is WaitlistDetailAction.CancelWaitlist -> {
                openCancellationDialog(action.waitlistId)
            }

            is WaitlistDetailAction.ApproveWaitlist -> {
                openApprovalDialog(action.waitlistId)
            }

            WaitlistDetailAction.DismissCancellation -> {
                dismissCancellationDialog()
            }

            WaitlistDetailAction.DismissApproval -> {
                dismissApprovalDialog()
            }

            is WaitlistDetailAction.ConfirmCancellation -> {
                submitCancellation(
                    waitlistId = action.waitlistId,
                    expectedDialogState = WaitlistCancellationDialogUiState.Waiting,
                )
            }

            is WaitlistDetailAction.RetryCancellation -> {
                submitCancellation(
                    waitlistId = action.waitlistId,
                    expectedDialogState = null,
                )
            }

            is WaitlistDetailAction.ConfirmApproval -> {
                submitApproval(
                    waitlistId = action.waitlistId,
                    expectedDialogState = WaitlistApprovalDialogUiState.Waiting,
                )
            }

            is WaitlistDetailAction.RetryApproval -> {
                submitApproval(
                    waitlistId = action.waitlistId,
                    expectedDialogState = null,
                )
            }
        }
    }

    private fun retryDetail() {
        if (_uiState.value !is WaitlistDetailUiState.Error) return
        loadDetail()
    }

    private fun loadDetail() {
        if (isDetailLoading) return
        isDetailLoading = true
        _uiState.value = WaitlistDetailUiState.Loading

        viewModelScope.launch {
            val result = repository.getWaitlistDetail(waitlistId)
            isDetailLoading = false
            _uiState.value =
                when (result) {
                    is MyScheduleResult.Success -> {
                        WaitlistDetailUiState.Content(
                            detail = mapper.mapWaitlistDetail(result.value),
                        )
                    }

                    is MyScheduleResult.Failure -> {
                        WaitlistDetailUiState.Error(result.reason.toDetailErrorUiModel())
                    }
                }
        }
    }

    private fun openCancellationDialog(requestedId: WaitlistId) {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        if (
            requestedId != waitlistId ||
            content.detail.waitlistId != waitlistId ||
            content.detail.cancellation !is WaitlistCancellationAvailabilityUiModel.Available ||
            content.cancellationDialog != null
        ) {
            return
        }

        _uiState.value =
            content.copy(
                cancellationDialog = WaitlistCancellationDialogUiState.Waiting,
            )
    }

    private fun dismissCancellationDialog() {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        val dialog = content.cancellationDialog ?: return
        if (!dialog.canDismiss) return

        _uiState.value = content.copy(cancellationDialog = null)
    }

    private fun submitCancellation(
        waitlistId: WaitlistId,
        expectedDialogState: WaitlistCancellationDialogUiState?,
    ) {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        val dialog = content.cancellationDialog ?: return
        val canSubmit =
            when {
                waitlistId != this.waitlistId -> false
                content.detail.waitlistId != this.waitlistId -> false
                dialog is WaitlistCancellationDialogUiState.Submitting -> false
                expectedDialogState != null -> dialog == expectedDialogState
                else -> dialog is WaitlistCancellationDialogUiState.Failed
            }
        if (!canSubmit) return

        _uiState.value =
            content.copy(
                cancellationDialog = WaitlistCancellationDialogUiState.Submitting,
            )
        viewModelScope.launch {
            when (val result = repository.cancelWaitlist(waitlistId)) {
                is MyScheduleResult.Success -> {
                    if (result.value.waitlistId == this@WaitlistDetailViewModel.waitlistId) {
                        _uiState.value =
                            WaitlistDetailUiState.CancellationCompleted(
                                mapper.mapWaitlistCancellationReceipt(result.value),
                            )
                    } else {
                        updateCancellationFailure(WaitlistCancellationErrorUiModel.UNKNOWN)
                    }
                }

                is MyScheduleResult.Failure -> {
                    updateCancellationFailure(result.reason.toCancellationErrorUiModel())
                }
            }
        }
    }

    private fun openApprovalDialog(requestedId: WaitlistId) {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        if (
            requestedId != waitlistId ||
            content.detail.waitlistId != waitlistId ||
            content.detail.status != WaitlistDetailStatusUiModel.APPROVAL_REQUIRED ||
            content.approvalDialog != null
        ) {
            return
        }

        _uiState.value =
            content.copy(
                approvalDialog = WaitlistApprovalDialogUiState.Waiting,
            )
    }

    private fun dismissApprovalDialog() {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        val dialog = content.approvalDialog ?: return
        if (!dialog.canDismiss) return

        _uiState.value = content.copy(approvalDialog = null)
    }

    private fun submitApproval(
        waitlistId: WaitlistId,
        expectedDialogState: WaitlistApprovalDialogUiState?,
    ) {
        val content = _uiState.value as? WaitlistDetailUiState.Content ?: return
        val dialog = content.approvalDialog ?: return
        val canSubmit =
            when {
                waitlistId != this.waitlistId -> false
                content.detail.waitlistId != this.waitlistId -> false
                dialog is WaitlistApprovalDialogUiState.Submitting -> false
                expectedDialogState != null -> dialog == expectedDialogState
                else -> dialog is WaitlistApprovalDialogUiState.Failed
            }
        if (!canSubmit) return

        _uiState.value =
            content.copy(
                approvalDialog = WaitlistApprovalDialogUiState.Submitting,
            )
        viewModelScope.launch {
            when (val result = repository.approveWaitlist(waitlistId)) {
                is MyScheduleResult.Success -> {
                    _uiState.value = WaitlistDetailUiState.ApprovalCompleted
                }

                is MyScheduleResult.Failure -> {
                    updateApprovalFailure(result.reason.toApprovalErrorUiModel())
                }
            }
        }
    }

    private fun updateCancellationFailure(error: WaitlistCancellationErrorUiModel) {
        _uiState.update { state ->
            val content = state as? WaitlistDetailUiState.Content ?: return@update state
            if (content.cancellationDialog !is WaitlistCancellationDialogUiState.Submitting) {
                return@update state
            }
            content.copy(
                cancellationDialog = WaitlistCancellationDialogUiState.Failed(error),
            )
        }
    }

    private fun MyScheduleFailureReason.toDetailErrorUiModel(): WaitlistDetailErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                WaitlistDetailErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.NOT_FOUND -> {
                WaitlistDetailErrorUiModel.NOT_FOUND
            }

            MyScheduleFailureReason.CONFLICT,
            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED,
            MyScheduleFailureReason.APPROVAL_NOT_ALLOWED,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                WaitlistDetailErrorUiModel.UNKNOWN
            }
        }

    private fun MyScheduleFailureReason.toCancellationErrorUiModel(): WaitlistCancellationErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                WaitlistCancellationErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.CONFLICT -> {
                WaitlistCancellationErrorUiModel.CONFLICT
            }

            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED -> {
                WaitlistCancellationErrorUiModel.CANCELLATION_NOT_ALLOWED
            }

            MyScheduleFailureReason.NOT_FOUND,
            MyScheduleFailureReason.APPROVAL_NOT_ALLOWED,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                WaitlistCancellationErrorUiModel.UNKNOWN
            }
        }

    private fun updateApprovalFailure(error: WaitlistApprovalErrorUiModel) {
        _uiState.update { state ->
            val content = state as? WaitlistDetailUiState.Content ?: return@update state
            if (content.approvalDialog !is WaitlistApprovalDialogUiState.Submitting) {
                return@update state
            }
            content.copy(
                approvalDialog = WaitlistApprovalDialogUiState.Failed(error),
            )
        }
    }

    private fun MyScheduleFailureReason.toApprovalErrorUiModel(): WaitlistApprovalErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                WaitlistApprovalErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.CONFLICT -> {
                WaitlistApprovalErrorUiModel.CONFLICT
            }

            MyScheduleFailureReason.APPROVAL_NOT_ALLOWED -> {
                WaitlistApprovalErrorUiModel.APPROVAL_NOT_ALLOWED
            }

            MyScheduleFailureReason.NOT_FOUND,
            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                WaitlistApprovalErrorUiModel.UNKNOWN
            }
        }
}
