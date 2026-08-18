package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.WaitlistId

data class WaitlistDetailUiModel(
    val waitlistId: WaitlistId,
    val title: String,
    val appliedAtLabel: String,
    val currentPosition: Int,
    val classInfo: WaitlistClassInfoUiModel,
    val pass: WaitlistPassAvailabilityUiModel,
    val cancellation: WaitlistCancellationAvailabilityUiModel,
) {
    init {
        require(title.isNotBlank()) { "수업명 표시는 비어 있을 수 없습니다." }
        require(appliedAtLabel.isNotBlank()) { "대기 일시 표시는 비어 있을 수 없습니다." }
        require(currentPosition >= 1) { "현재 대기 순번은 1 이상이어야 합니다." }
    }
}

data class WaitlistClassInfoUiModel(
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

data class WaitlistPassAvailabilityUiModel(
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

sealed interface WaitlistCancellationAvailabilityUiModel {
    data object Available : WaitlistCancellationAvailabilityUiModel

    data class Unavailable(
        val reason: WaitlistCancellationUnavailableReasonUiModel,
    ) : WaitlistCancellationAvailabilityUiModel
}

enum class WaitlistCancellationUnavailableReasonUiModel {
    DEADLINE_PASSED,
    NO_REMAINING_CANCELLATION,
    ALREADY_CANCELLED,
    UNKNOWN,
}

sealed interface WaitlistDetailUiState {
    data object Loading : WaitlistDetailUiState

    data class Content(
        val detail: WaitlistDetailUiModel,
    ) : WaitlistDetailUiState

    data class Error(
        val error: WaitlistDetailErrorUiModel,
    ) : WaitlistDetailUiState
}

enum class WaitlistDetailErrorUiModel {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface WaitlistDetailAction {
    data object Retry : WaitlistDetailAction

    data class CancelWaitlist(
        val waitlistId: WaitlistId,
    ) : WaitlistDetailAction
}

internal fun WaitlistDetailUiModel.cancellationActionOrNull(): WaitlistDetailAction.CancelWaitlist? =
    if (cancellation is WaitlistCancellationAvailabilityUiModel.Available) {
        WaitlistDetailAction.CancelWaitlist(waitlistId)
    } else {
        null
    }
