package com.pheeeew.data.repository

import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.sigh.SighPin
import com.pheeeew.domain.repository.SighRepository

/**
 * 실제 서버 연동 구현체가 준비되기 전까지 화면을 연결해 두기 위한 임시 인메모리 구현체.
 * 서버 연동용 SighRepository 구현체가 생기면 이 클래스는 제거한다.
 */
class FakeSighRepository : SighRepository {
    private val sighs = mutableListOf<SighPin>()
    private var nextId = 1L

    override suspend fun getSighs(): List<SighPin> = sighs.toList()

    override suspend fun registerSigh(
        requestId: String,
        coordinate: Coordinate,
    ): SighPin {
        val sighPin = SighPin(id = nextId++, coordinate = coordinate)
        sighs += sighPin
        return sighPin
    }
}
