package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleCancellationFlowState {
    data object Idle : MyScheduleCancellationFlowState

    data object Confirming : MyScheduleCancellationFlowState

    data object Submitting : MyScheduleCancellationFlowState

    data object Failed : MyScheduleCancellationFlowState
}

sealed interface MyScheduleCancellationResultUiModel {
    data class Reservation(
        val item: ScheduleItemUiModel.ConfirmedReservation,
    ) : MyScheduleCancellationResultUiModel

    data class Waitlist(
        val item: ScheduleItemUiModel.Waitlist,
    ) : MyScheduleCancellationResultUiModel
}
