package com.classitda.domain.model.student.myschedule

import kotlinx.datetime.TimeZone
import kotlin.time.Instant

data class ClassPeriod(
    val startsAt: Instant,
    val endsAt: Instant,
    val timeZoneId: String,
) {
    init {
        require(startsAt < endsAt) { "수업 시작 시각은 종료 시각보다 빨라야 합니다." }
        require(timeZoneId.isNotBlank()) { "수업 시간대 ID는 비어 있을 수 없습니다." }
        TimeZone.of(timeZoneId)
    }
}

data class InstructorSummary(
    val id: InstructorId,
    val name: String,
    val profileImageUrl: String?,
) {
    init {
        require(name.isNotBlank()) { "강사 이름은 비어 있을 수 없습니다." }
    }
}

data class FacilitySummary(
    val id: FacilityId,
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "시설 이름은 비어 있을 수 없습니다." }
    }
}

data class ClassSession(
    val id: ClassSessionId,
    val title: String,
    val period: ClassPeriod,
    val instructor: InstructorSummary,
    val facility: FacilitySummary,
    val memo: String?,
) {
    init {
        require(title.isNotBlank()) { "수업 이름은 비어 있을 수 없습니다." }
        require(memo == null || memo.isNotBlank()) { "수업 메모는 null이거나 비어 있지 않아야 합니다." }
    }
}
