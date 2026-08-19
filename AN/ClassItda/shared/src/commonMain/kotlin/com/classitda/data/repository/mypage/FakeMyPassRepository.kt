package com.classitda.data.repository.mypage

import com.classitda.domain.model.mypage.MyPass
import com.classitda.domain.model.mypage.MyPassStatus
import com.classitda.domain.repository.mypage.MyPassRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
class FakeMyPassRepository : MyPassRepository {
    override suspend fun getMyPasses(): List<MyPass> {
        delay(300)
        return listOf(
            MyPass(
                id = "pass-reformer-20",
                name = "리포머 20회권",
                status = MyPassStatus.IN_USE,
                totalRemainingCount = 8,
                reservableCount = 5,
                cancellableCount = 2,
            ),
            MyPass(
                id = "pass-pilates-1month",
                name = "1개월 필라테스 수강권",
                status = MyPassStatus.IN_USE,
                totalRemainingCount = 8,
                reservableCount = 5,
                cancellableCount = 2,
                validFrom = LocalDate(2026, 7, 21),
                validUntil = LocalDate(2026, 8, 20),
            ),
            MyPass(
                id = "pass-expired-gigu",
                name = "기구 필라테스 10회권",
                status = MyPassStatus.EXPIRED,
                totalRemainingCount = 8,
                reservableCount = 0,
                cancellableCount = 0,
                validFrom = LocalDate(2025, 9, 30),
                validUntil = LocalDate(2025, 12, 30),
            ),
            MyPass(
                id = "pass-terminated-open-event",
                name = "오픈 기념 이벤트 20회권",
                status = MyPassStatus.TERMINATED,
                totalRemainingCount = 0,
                reservableCount = 0,
                cancellableCount = 0,
                validFrom = LocalDate(2025, 5, 1),
                validUntil = LocalDate(2025, 8, 15),
            ),
        )
    }
}
