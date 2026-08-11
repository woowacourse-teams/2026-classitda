package com.classitda.feature.student.myschedule.contract

data class CompletedClassDetailUiModel(
    val id: ScheduleItemId,
    val title: String,
    val instructor: CompletedClassInstructorUiModel,
    val dateTime: ReservationDetailDateTimeUiModel,
    val durationMinutes: Int,
    val location: CompletedClassLocationUiModel,
    val ticket: CompletedClassTicketUiModel,
) {
    init {
        require(durationMinutes > 0) { "수업 시간은 1분 이상이어야 합니다." }
    }
}

data class CompletedClassInstructorUiModel(
    val name: String,
    val specialtyLabel: String,
)

data class CompletedClassLocationUiModel(
    val name: String,
    val detail: String,
)

data class CompletedClassTicketUiModel(
    val name: String,
    val remainingCount: Int,
    val totalCount: Int,
) {
    init {
        require(remainingCount >= 0) { "잔여 횟수는 0 이상이어야 합니다." }
        require(totalCount > 0) { "전체 횟수는 1 이상이어야 합니다." }
        require(remainingCount <= totalCount) { "잔여 횟수는 전체 횟수를 넘을 수 없습니다." }
    }
}
