package com.classitda.domain.model.student.myschedule

import kotlin.time.Instant

sealed interface ReservationCancellationAvailability {
    data class Available(
        val restoredPassUses: Int,
    ) : ReservationCancellationAvailability {
        init {
            require(restoredPassUses >= 0) { "예약 취소 시 복구될 수강권 횟수는 음수일 수 없습니다." }
        }
    }

    data class Unavailable(
        val reason: CancellationUnavailableReason,
    ) : ReservationCancellationAvailability
}

sealed interface WaitlistCancellationAvailability {
    data object Available : WaitlistCancellationAvailability

    data class Unavailable(
        val reason: CancellationUnavailableReason,
    ) : WaitlistCancellationAvailability
}

enum class CancellationUnavailableReason {
    DEADLINE_PASSED,
    NO_REMAINING_CANCELLATION,
    ALREADY_CANCELLED,
    UNKNOWN,
}

data class PassRestoration(
    val restoredUses: Int,
    val remainingUsesAfterCancellation: Int,
) {
    init {
        require(restoredUses >= 0) { "복구된 수강권 횟수는 음수일 수 없습니다." }
        require(remainingUsesAfterCancellation >= 0) { "취소 후 수강권 잔여 횟수는 음수일 수 없습니다." }
    }
}

data class ReservationCancellationReceipt(
    val reservationId: ReservationId,
    val session: ClassSession,
    val cancelledAt: Instant,
    val restoration: PassRestoration,
)

data class WaitlistCancellationReceipt(
    val waitlistId: WaitlistId,
    val session: ClassSession,
    val cancelledAt: Instant,
    val positionAtCancellation: Int,
) {
    init {
        require(positionAtCancellation >= 1) { "취소 당시 대기 순번은 1 이상이어야 합니다." }
    }
}
