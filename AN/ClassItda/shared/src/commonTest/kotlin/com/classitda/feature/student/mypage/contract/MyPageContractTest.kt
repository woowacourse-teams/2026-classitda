package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.NotificationSettingType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MyPageContractTest {
    @Test
    fun `마이페이지 action은 외부 흐름을 서로 다른 의미로 구분한다`() {
        assertIs<MyPageAction.OpenProfile>(MyPageAction.OpenProfile)
        assertIs<MyPageAction.OpenPasses>(MyPageAction.OpenPasses)
        assertIs<MyPageAction.OpenConnectedFacilities>(MyPageAction.OpenConnectedFacilities)
        assertIs<MyPageAction.OpenNotificationSettings>(MyPageAction.OpenNotificationSettings)
        assertIs<MyPageAction.OpenPrivacyPolicy>(MyPageAction.OpenPrivacyPolicy)
        assertIs<MyPageAction.OpenInstructorSignup>(MyPageAction.OpenInstructorSignup)
        assertIs<MyPageAction.SwitchToInstructor>(MyPageAction.SwitchToInstructor)
    }

    @Test
    fun `알림 action은 안정적인 enum 타입과 요청 값을 전달한다`() {
        val action = NotificationSettingsAction.Toggle(NotificationSettingType.CHAT_AND_MESSAGE, true)
        assertEquals(NotificationSettingType.CHAT_AND_MESSAGE, action.type)
        assertEquals(true, action.enabled)
    }
}
