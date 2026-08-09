package com.classitda.feature.student.myschedule.contract

sealed interface ScheduleCancellationAvailabilityUiModel {
    val policy: ScheduleCancellationPolicyUiModel

    data class Available(
        override val policy: ScheduleCancellationPolicyUiModel,
    ) : ScheduleCancellationAvailabilityUiModel

    data class Unavailable(
        override val policy: ScheduleCancellationPolicyUiModel,
        val reason: ScheduleCancellationUnavailableReasonUiModel,
    ) : ScheduleCancellationAvailabilityUiModel
}

sealed interface ScheduleCancellationUnavailableReasonUiModel {
    data object DeadlinePassed : ScheduleCancellationUnavailableReasonUiModel
}

sealed interface ScheduleCancellationPolicyUiModel {
    data class Reservation(
        val deadlineHoursBeforeStart: Int,
        val ticketRestoration: ScheduleTicketRestorationUiModel,
    ) : ScheduleCancellationPolicyUiModel {
        init {
            require(deadlineHoursBeforeStart > 0) { "취소 기한은 1시간 이상이어야 합니다." }
        }
    }

    data class Waitlist(
        val reapplicationRule: ScheduleWaitlistReapplicationUiModel,
    ) : ScheduleCancellationPolicyUiModel
}

sealed interface ScheduleTicketRestorationUiModel {
    data object AccordingToFacilityPolicy : ScheduleTicketRestorationUiModel

    data object NoDeduction : ScheduleTicketRestorationUiModel
}

sealed interface ScheduleWaitlistReapplicationUiModel {
    data object LastPosition : ScheduleWaitlistReapplicationUiModel
}
