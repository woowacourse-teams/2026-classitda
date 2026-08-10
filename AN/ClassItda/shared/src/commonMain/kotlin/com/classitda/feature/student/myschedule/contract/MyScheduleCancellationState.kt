package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleCancellationFlowState {
    data object Idle : MyScheduleCancellationFlowState

    data object Confirming : MyScheduleCancellationFlowState

    data object Submitting : MyScheduleCancellationFlowState

    data object Failed : MyScheduleCancellationFlowState
}

sealed interface MyScheduleCancellationResultUiModel {
    data class Reservation(
        val item: UpcomingScheduleItemUiModel.ConfirmedReservation,
    ) : MyScheduleCancellationResultUiModel

    data class Waitlist(
        val item: UpcomingScheduleItemUiModel.Waitlist,
    ) : MyScheduleCancellationResultUiModel
}
