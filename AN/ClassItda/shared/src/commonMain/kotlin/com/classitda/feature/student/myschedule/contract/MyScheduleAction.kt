package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleAction {
    data class SelectTab(
        val tab: MyScheduleTab,
    ) : MyScheduleAction

    data class OpenScheduleDetail(
        val itemId: ScheduleItemId,
    ) : MyScheduleAction

    data object CloseScheduleDetail : MyScheduleAction

    data object StartCancellation : MyScheduleAction

    data object ConfirmCancellation : MyScheduleAction

    data object DismissCancellation : MyScheduleAction

    data object RetryCancellation : MyScheduleAction

    data object RetryScheduleLoad : MyScheduleAction

    data object GoHome : MyScheduleAction

    data object ReturnToScheduleList : MyScheduleAction

    data object BookAnotherClass : MyScheduleAction

    data class ContactSupport(
        val itemId: ScheduleItemId,
    ) : MyScheduleAction
}
