package com.classitda.domain.model.instructor.mypage

import kotlin.jvm.JvmInline

@JvmInline
value class InstructorPhoneVerificationId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "강사 전화번호 인증 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class InstructorMemberId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "강사 회원 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class InstructorFacilityId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "강사 시설 ID는 비어 있을 수 없습니다." }
    }
}
