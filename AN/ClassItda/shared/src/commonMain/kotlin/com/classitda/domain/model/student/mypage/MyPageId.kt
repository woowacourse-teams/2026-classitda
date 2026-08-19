package com.classitda.domain.model.student.mypage

import kotlin.jvm.JvmInline

@JvmInline
value class MemberId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "회원 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class FacilityId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "시설 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class PhoneVerificationId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "전화번호 인증 ID는 비어 있을 수 없습니다." }
    }
}
