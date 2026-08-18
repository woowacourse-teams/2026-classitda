package com.classitda.data.repository.home

import com.classitda.domain.model.home.FacilityNotice
import com.classitda.domain.repository.home.NoticeRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
class FakeNoticeRepository : NoticeRepository {
    private val timeZone = TimeZone.currentSystemDefault()

    override suspend fun getLatestNotice(): FacilityNotice {
        delay(300)
        return FacilityNotice(
            id = "notice-1",
            title = "샤워실 이용 시간이 변경되었어요",
            description = "8월 10일부터 평일 샤워실은 오후 10시까지 운영됩니다.",
            postedDate = Clock.System.todayIn(timeZone),
        )
    }
}
