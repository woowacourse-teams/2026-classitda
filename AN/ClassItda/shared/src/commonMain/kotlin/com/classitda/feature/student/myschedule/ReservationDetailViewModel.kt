package com.classitda.feature.student.myschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.repository.student.myschedule.MyScheduleFailureReason
import com.classitda.domain.repository.student.myschedule.MyScheduleRepository
import com.classitda.domain.repository.student.myschedule.MyScheduleResult
import com.classitda.feature.student.myschedule.contract.ReservationCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.ReservationCancellationErrorUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailAction
import com.classitda.feature.student.myschedule.contract.ReservationDetailErrorUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiState
import com.classitda.feature.student.myschedule.contract.canDismiss
import com.classitda.feature.student.myschedule.mapper.MyScheduleUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReservationDetailViewModel(
    private val reservationId: ReservationId,
    private val cancellationDeadlineHoursBeforeStart: Int,
    private val repository: MyScheduleRepository,
    private val mapper: MyScheduleUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReservationDetailUiState>(ReservationDetailUiState.Loading)
    val uiState: StateFlow<ReservationDetailUiState> = _uiState.asStateFlow()

    private var isDetailLoading = false

    init {
        require(cancellationDeadlineHoursBeforeStart >= 0) {
            "예약 취소 가능 기준 시간은 0 이상이어야 합니다."
        }
        loadDetail()
    }

    fun onAction(action: ReservationDetailAction) {
        when (action) {
            ReservationDetailAction.Retry -> {
                retryDetail()
            }

            is ReservationDetailAction.CancelReservation -> {
                openCancellationDialog(action.reservationId)
            }

            ReservationDetailAction.DismissCancellation -> {
                dismissCancellationDialog()
            }

            is ReservationDetailAction.ConfirmCancellation -> {
                submitCancellation(
                    reservationId = action.reservationId,
                    expectedDialogState = ReservationCancellationDialogUiState.Waiting,
                )
            }

            is ReservationDetailAction.RetryCancellation -> {
                submitCancellation(
                    reservationId = action.reservationId,
                    expectedDialogState = null,
                )
            }
        }
    }

    private fun retryDetail() {
        if (_uiState.value !is ReservationDetailUiState.Error) return
        loadDetail()
    }

    private fun loadDetail() {
        if (isDetailLoading) return
        isDetailLoading = true
        _uiState.value = ReservationDetailUiState.Loading

        viewModelScope.launch {
            val result = repository.getReservationDetail(reservationId)
            isDetailLoading = false
            _uiState.value =
                when (result) {
                    is MyScheduleResult.Success -> {
                        val cancellationDeadline = cancellationDeadlineHoursBeforeStart
                        val detail =
                            mapper.mapReservationDetail(
                                detail = result.value,
                                cancellationDeadlineHoursBeforeStart = cancellationDeadline,
                            )
                        ReservationDetailUiState.Content(
                            detail = detail,
                        )
                    }

                    is MyScheduleResult.Failure -> {
                        ReservationDetailUiState.Error(result.reason.toDetailErrorUiModel())
                    }
                }
        }
    }

    private fun openCancellationDialog(requestedId: ReservationId) {
        val content = _uiState.value as? ReservationDetailUiState.Content ?: return
        val confirmed = content.detail as? ReservationDetailUiModel.Confirmed ?: return
        if (
            requestedId != reservationId ||
            confirmed.reservationId != reservationId ||
            confirmed.cancellation !is ReservationCancellationAvailabilityUiModel.Available ||
            content.cancellationDialog != null
        ) {
            return
        }

        _uiState.value =
            content.copy(
                cancellationDialog = ReservationCancellationDialogUiState.Waiting,
            )
    }

    private fun dismissCancellationDialog() {
        val content = _uiState.value as? ReservationDetailUiState.Content ?: return
        val dialog = content.cancellationDialog ?: return
        if (!dialog.canDismiss) return

        _uiState.value = content.copy(cancellationDialog = null)
    }

    private fun submitCancellation(
        reservationId: ReservationId,
        expectedDialogState: ReservationCancellationDialogUiState?,
    ) {
        val content = _uiState.value as? ReservationDetailUiState.Content ?: return
        val confirmed = content.detail as? ReservationDetailUiModel.Confirmed ?: return
        val dialog = content.cancellationDialog ?: return
        val canSubmit =
            when {
                reservationId != this.reservationId -> false
                confirmed.reservationId != this.reservationId -> false
                dialog is ReservationCancellationDialogUiState.Submitting -> false
                expectedDialogState != null -> dialog == expectedDialogState
                else -> dialog is ReservationCancellationDialogUiState.Failed
            }
        if (!canSubmit) return

        _uiState.value =
            content.copy(
                cancellationDialog = ReservationCancellationDialogUiState.Submitting,
            )
        viewModelScope.launch {
            when (val result = repository.cancelReservation(reservationId)) {
                is MyScheduleResult.Success -> {
                    if (result.value.reservationId == this@ReservationDetailViewModel.reservationId) {
                        _uiState.value =
                            ReservationDetailUiState.CancellationCompleted(
                                mapper.mapReservationCancellationReceipt(result.value),
                            )
                    } else {
                        updateCancellationFailure(ReservationCancellationErrorUiModel.UNKNOWN)
                    }
                }

                is MyScheduleResult.Failure -> {
                    updateCancellationFailure(result.reason.toCancellationErrorUiModel())
                }
            }
        }
    }

    private fun updateCancellationFailure(error: ReservationCancellationErrorUiModel) {
        _uiState.update { state ->
            val content = state as? ReservationDetailUiState.Content ?: return@update state
            if (content.cancellationDialog !is ReservationCancellationDialogUiState.Submitting) {
                return@update state
            }
            content.copy(
                cancellationDialog = ReservationCancellationDialogUiState.Failed(error),
            )
        }
    }

    private fun MyScheduleFailureReason.toDetailErrorUiModel(): ReservationDetailErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                ReservationDetailErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.NOT_FOUND -> {
                ReservationDetailErrorUiModel.NOT_FOUND
            }

            MyScheduleFailureReason.CONFLICT,
            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                ReservationDetailErrorUiModel.UNKNOWN
            }
        }

    private fun MyScheduleFailureReason.toCancellationErrorUiModel(): ReservationCancellationErrorUiModel =
        when (this) {
            MyScheduleFailureReason.NETWORK -> {
                ReservationCancellationErrorUiModel.NETWORK
            }

            MyScheduleFailureReason.CONFLICT -> {
                ReservationCancellationErrorUiModel.CONFLICT
            }

            MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED -> {
                ReservationCancellationErrorUiModel.CANCELLATION_NOT_ALLOWED
            }

            MyScheduleFailureReason.NOT_FOUND,
            MyScheduleFailureReason.UNKNOWN,
            -> {
                ReservationCancellationErrorUiModel.UNKNOWN
            }
        }
}
