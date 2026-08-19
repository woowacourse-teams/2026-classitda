package com.classitda.domain.model.student.mypage

import kotlinx.datetime.LocalDate

data class ConnectedFacility(
    val id: FacilityId,
    val name: String,
    val connectedOn: LocalDate,
) {
    init {
        require(name.isNotBlank()) { "시설 이름은 비어 있을 수 없습니다." }
    }
}
