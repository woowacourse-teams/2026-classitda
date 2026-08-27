package com.classitda.data.repository.home

import com.classitda.domain.model.home.Pass
import com.classitda.domain.repository.home.PassRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
class FakePassRepository : PassRepository {
    private val timeZone = TimeZone.currentSystemDefault()

    override suspend fun getPrimaryPass(): Pass {
        delay(300)
        val expireDate = Clock.System.todayIn(timeZone).plus(45, DateTimeUnit.DAY)
        return Pass(
            id = "pass-1",
            name = "리포머 20회권",
            expireDate = expireDate,
            totalRemainingCount = 8,
            reservableCount = 5,
            cancellableCount = 2,
        )
    }
}
