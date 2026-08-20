package com.classitda.data.repository.mypage

import com.classitda.domain.model.mypage.MyPass
import com.classitda.domain.model.mypage.MyPassHistoryEntry
import com.classitda.domain.model.mypage.MyPassHistoryEntryType
import com.classitda.domain.model.mypage.MyPassHoldingCalculator
import com.classitda.domain.model.mypage.MyPassHoldingReceipt
import com.classitda.domain.model.mypage.MyPassStatus
import com.classitda.domain.repository.mypage.MyPassRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
class FakeMyPassRepository : MyPassRepository {
    private val passes =
        listOf(
            MyPass(
                id = "pass-reformer-20",
                name = "리포머 20회권",
                studioName = "밸런스 필라테스 성수점",
                status = MyPassStatus.IN_USE,
                totalRemainingCount = 8,
                reservableCount = 5,
                cancellableCount = 2,
                validFrom = LocalDate(2026, 3, 31),
                validUntil = LocalDate(2026, 9, 30),
            ),
            MyPass(
                id = "pass-pilates-1month",
                name = "1개월 필라테스 수강권",
                studioName = "밸런스 필라테스 성수점",
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
                studioName = "밸런스 필라테스 성수점",
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
                studioName = "밸런스 필라테스 성수점",
                status = MyPassStatus.TERMINATED,
                totalRemainingCount = 0,
                reservableCount = 0,
                cancellableCount = 0,
                validFrom = LocalDate(2025, 5, 1),
                validUntil = LocalDate(2025, 8, 15),
            ),
        )

    private val historyByPassId =
        mapOf(
            "pass-reformer-20" to
                listOf(
                    MyPassHistoryEntry(
                        id = "history-1",
                        type = MyPassHistoryEntryType.DEDUCTION,
                        title = "수업 예약 차감",
                        value = 1,
                        description = "리포머 밸런스 (이지은 강사)",
                        occurredAt = LocalDateTime(2026, 8, 5, 14, 20),
                    ),
                    MyPassHistoryEntry(
                        id = "history-2",
                        type = MyPassHistoryEntryType.RESTORATION,
                        title = "취소 복구",
                        value = 1,
                        description = "체어 베이직 (박소연 강사)",
                        occurredAt = LocalDateTime(2026, 8, 3, 9, 12),
                    ),
                    MyPassHistoryEntry(
                        id = "history-3",
                        type = MyPassHistoryEntryType.HOLD,
                        title = "홀딩 적용",
                        value = 7,
                        description = "개인 사정으로 인한 일시 정지",
                        occurredAt = LocalDateTime(2026, 7, 15, 0, 0),
                        occurredUntil = LocalDateTime(2026, 7, 21, 0, 0),
                    ),
                    MyPassHistoryEntry(
                        id = "history-4",
                        type = MyPassHistoryEntryType.ISSUANCE,
                        title = "수강권 발급",
                        value = 20,
                        description = "리포머 20회권 결제 완료",
                        occurredAt = LocalDateTime(2026, 3, 31, 11, 0),
                    ),
                ),
        )

    override suspend fun getMyPasses(): List<MyPass> {
        delay(300)
        return passes
    }

    override suspend fun getMyPass(passId: String): MyPass? {
        delay(300)
        return passes.firstOrNull { it.id == passId }
    }

    override suspend fun getMyPassHistory(passId: String): List<MyPassHistoryEntry> {
        delay(300)
        return historyByPassId[passId].orEmpty()
    }

    override suspend fun requestHolding(
        passId: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): MyPassHoldingReceipt {
        delay(300)
        val pass = passes.firstOrNull { it.id == passId }
        val previousExpireDate = requireNotNull(pass?.validUntil) { "홀딩 신청 대상 수강권을 찾을 수 없습니다." }
        return MyPassHoldingCalculator.calculate(
            startDate = startDate,
            endDate = endDate,
            previousExpireDate = previousExpireDate,
        )
    }
}
