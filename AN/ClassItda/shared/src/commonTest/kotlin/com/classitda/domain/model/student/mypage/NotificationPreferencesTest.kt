package com.classitda.domain.model.student.mypage

import kotlin.test.Test
import kotlin.test.assertContentEquals

class NotificationPreferencesTest {
    @Test
    fun `알림 설정 타입은 F07의 다섯 의미를 안정적으로 구분한다`() {
        assertContentEquals(
            listOf(
                NotificationSettingType.RESERVATION_AND_SCHEDULE,
                NotificationSettingType.FACILITY_NOTICE,
                NotificationSettingType.CHAT_AND_MESSAGE,
                NotificationSettingType.BENEFIT_AND_EVENT,
                NotificationSettingType.NIGHT_MARKETING,
            ),
            NotificationSettingType.entries,
        )
    }
}
