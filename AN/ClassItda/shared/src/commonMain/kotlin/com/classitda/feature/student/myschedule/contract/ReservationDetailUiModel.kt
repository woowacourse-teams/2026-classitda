package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId

sealed interface ReservationDetailUiModel {
    val reservationId: ReservationId
    val title: String
    val classInfo: ReservationClassInfoUiModel

    data class Confirmed(
        override val reservationId: ReservationId,
        override val title: String,
        override val classInfo: ReservationClassInfoUiModel,
        val reservedAtLabel: String,
        val pass: ReservationPassAvailabilityUiModel,
        val cancellationDeadlineHoursBeforeStart: Int,
        val cancellation: ReservationCancellationAvailabilityUiModel,
    ) : ReservationDetailUiModel {
        init {
            require(reservedAtLabel.isNotBlank()) { "예약 일시 표시는 비어 있을 수 없습니다." }
            require(cancellationDeadlineHoursBeforeStart >= 0) {
                "예약 취소 가능 기준 시간은 0 이상이어야 합니다."
            }
        }
    }

    data class Cancelled(
        override val reservationId: ReservationId,
        override val title: String,
        override val classInfo: ReservationClassInfoUiModel,
        val cancelledAtLabel: String,
    ) : ReservationDetailUiModel {
        init {
            require(cancelledAtLabel.isNotBlank()) { "예약 취소 일시 표시는 비어 있을 수 없습니다." }
        }
    }

    data class Attended(
        override val reservationId: ReservationId,
        override val title: String,
        override val classInfo: ReservationClassInfoUiModel,
        val checkedInAtLabel: String,
        val usedPass: ReservationUsedPassUiModel,
    ) : ReservationDetailUiModel {
        init {
            require(checkedInAtLabel.isNotBlank()) { "출석 일시 표시는 비어 있을 수 없습니다." }
        }
    }

    data class Absent(
        override val reservationId: ReservationId,
        override val title: String,
        override val classInfo: ReservationClassInfoUiModel,
        val attendanceTimePlaceholder: String,
        val usedPass: ReservationUsedPassUiModel,
    ) : ReservationDetailUiModel {
        init {
            require(attendanceTimePlaceholder.isNotBlank()) {
                "결석 출석 일시 표시는 비어 있을 수 없습니다."
            }
        }
    }
}

data class ReservationClassInfoUiModel(
    val dateLabel: String,
    val timeRangeLabel: String,
    val memo: String?,
    val instructorName: String,
    val facilityName: String,
) {
    init {
        require(dateLabel.isNotBlank()) { "수업 날짜 표시는 비어 있을 수 없습니다." }
        require(timeRangeLabel.isNotBlank()) { "수업 시간 표시는 비어 있을 수 없습니다." }
        require(memo == null || memo.isNotBlank()) { "수업 메모는 null이거나 비어 있지 않아야 합니다." }
        require(instructorName.isNotBlank()) { "강사 이름 표시는 비어 있을 수 없습니다." }
        require(facilityName.isNotBlank()) { "시설 이름 표시는 비어 있을 수 없습니다." }
    }
}

data class ReservationPassAvailabilityUiModel(
    val name: String,
    val validityLabel: String,
    val remainingUses: Int,
    val reservableUses: Int,
    val cancellableUses: Int,
) {
    init {
        require(name.isNotBlank()) { "수강권 이름은 비어 있을 수 없습니다." }
        require(validityLabel.isNotBlank()) { "수강권 유효기간 표시는 비어 있을 수 없습니다." }
        require(remainingUses >= 0) { "수강권 잔여 횟수는 0 이상이어야 합니다." }
        require(reservableUses >= 0) { "수강권 예약 가능 횟수는 0 이상이어야 합니다." }
        require(cancellableUses >= 0) { "수강권 취소 가능 횟수는 0 이상이어야 합니다." }
    }
}

data class ReservationUsedPassUiModel(
    val name: String,
    val validityLabel: String,
) {
    init {
        require(name.isNotBlank()) { "사용 수강권 이름은 비어 있을 수 없습니다." }
        require(validityLabel.isNotBlank()) { "사용 수강권 유효기간 표시는 비어 있을 수 없습니다." }
    }
}

sealed interface ReservationCancellationAvailabilityUiModel {
    data class Available(
        val hoursUntilStart: Int,
        val restoredPassUses: Int,
    ) : ReservationCancellationAvailabilityUiModel {
        init {
            require(hoursUntilStart >= 0) { "수업 시작까지 남은 시간은 0 이상이어야 합니다." }
            require(restoredPassUses >= 0) { "취소 시 복구 횟수는 0 이상이어야 합니다." }
        }
    }

    data class Unavailable(
        val reason: ReservationCancellationUnavailableReasonUiModel,
    ) : ReservationCancellationAvailabilityUiModel
}

enum class ReservationCancellationUnavailableReasonUiModel {
    DEADLINE_PASSED,
    NO_REMAINING_CANCELLATION,
    ALREADY_CANCELLED,
    UNKNOWN,
}

sealed interface ReservationDetailUiState {
    data object Loading : ReservationDetailUiState

    data class Content(
        val detail: ReservationDetailUiModel,
        val cancellationDialog: ReservationCancellationDialogUiState? = null,
    ) : ReservationDetailUiState {
        init {
            require(
                cancellationDialog == null ||
                    (
                        detail is ReservationDetailUiModel.Confirmed &&
                            detail.cancellation is ReservationCancellationAvailabilityUiModel.Available
                    ),
            ) {
                "예약 취소 확인 창은 취소 가능한 예약 완료 상세에서만 표시할 수 있습니다."
            }
        }
    }

    data class CancellationCompleted(
        val result: ReservationCancellationResultUiModel,
    ) : ReservationDetailUiState

    data class Error(
        val error: ReservationDetailErrorUiModel,
    ) : ReservationDetailUiState
}

enum class ReservationDetailErrorUiModel {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface ReservationDetailAction {
    data object Retry : ReservationDetailAction

    data class CancelReservation(
        val reservationId: ReservationId,
    ) : ReservationDetailAction

    data object DismissCancellation : ReservationDetailAction

    data class ConfirmCancellation(
        val reservationId: ReservationId,
    ) : ReservationDetailAction

    data class RetryCancellation(
        val reservationId: ReservationId,
    ) : ReservationDetailAction
}

sealed interface ReservationCancellationDialogUiState {
    data object Waiting : ReservationCancellationDialogUiState

    data object Submitting : ReservationCancellationDialogUiState

    data class Failed(
        val error: ReservationCancellationErrorUiModel,
    ) : ReservationCancellationDialogUiState
}

enum class ReservationCancellationErrorUiModel {
    NETWORK,
    CONFLICT,
    CANCELLATION_NOT_ALLOWED,
    UNKNOWN,
}

data class ReservationCancellationResultUiModel(
    val reservationId: ReservationId,
    val title: String,
    val classInfo: ReservationClassInfoUiModel,
    val cancelledAtLabel: String,
    val restoredPassUses: Int,
) {
    init {
        require(title.isNotBlank()) { "취소 결과 수업명은 비어 있을 수 없습니다." }
        require(cancelledAtLabel.isNotBlank()) { "취소 결과 일시 표시는 비어 있을 수 없습니다." }
        require(restoredPassUses >= 0) { "취소 결과 복구 횟수는 0 이상이어야 합니다." }
    }
}

internal val ReservationCancellationDialogUiState.canDismiss: Boolean
    get() = this !is ReservationCancellationDialogUiState.Submitting

internal fun ReservationDetailUiModel.cancellationActionOrNull(): ReservationDetailAction.CancelReservation? =
    when (this) {
        is ReservationDetailUiModel.Confirmed -> {
            if (cancellation is ReservationCancellationAvailabilityUiModel.Available) {
                ReservationDetailAction.CancelReservation(reservationId)
            } else {
                null
            }
        }

        is ReservationDetailUiModel.Cancelled -> {
            null
        }

        is ReservationDetailUiModel.Attended -> {
            null
        }

        is ReservationDetailUiModel.Absent -> {
            null
        }
    }
