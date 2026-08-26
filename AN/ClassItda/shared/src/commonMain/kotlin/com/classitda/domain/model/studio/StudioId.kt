package com.classitda.domain.model.studio

import kotlin.jvm.JvmInline

@JvmInline
value class StudioId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "시설 ID는 비어 있을 수 없습니다." }
    }
}
