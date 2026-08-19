package com.classitda.feature.student.mypage.mapper

import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.notification_settings_benefit_title
import classitda.shared.generated.resources.notification_settings_chat_title
import classitda.shared.generated.resources.notification_settings_facility_notice_title
import classitda.shared.generated.resources.notification_settings_night_marketing_title
import classitda.shared.generated.resources.notification_settings_reservation_title
import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.FacilityId
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MyPageUiMapperTest {
    private val mapper = MyPageUiMapper()

    @Test
    fun `프로필 전화번호를 표시용으로 포맷하고 나머지 식별 정보는 보존한다`() {
        val profile =
            MemberProfile(
                id = MemberId("member-1"),
                name = "홍길동",
                phoneNumber = "01012345678",
                email = "hong@example.com",
                profileImageUrl = null,
            )

        val model = mapper.mapProfile(profile)

        assertEquals(MemberId("member-1"), model.id)
        assertEquals("010-1234-5678", model.phoneNumberLabel)
        assertEquals("hong@example.com", model.email)
    }

    @Test
    fun `전화번호 형식이 예상과 다르면 서버 값을 임의로 검증하지 않고 그대로 표시한다`() {
        val profile =
            MemberProfile(
                id = MemberId("member-2"),
                name = "회원",
                phoneNumber = "+82 10 1234",
                email = "member@example.com",
                profileImageUrl = null,
            )

        assertEquals("+82 10 1234", mapper.mapProfile(profile).phoneNumberLabel)
    }

    @Test
    fun `시설 연결일을 결정적인 점 표기 문자열로 변환하고 ID를 유지한다`() {
        val facilities =
            mapper.mapConnectedFacilities(
                listOf(
                    ConnectedFacility(
                        id = FacilityId("facility-1"),
                        name = "스튜디오",
                        connectedOn = LocalDate(2026, 2, 5),
                    ),
                ),
            )

        assertEquals(FacilityId("facility-1"), facilities.single().id)
        assertEquals("2026.02.05", facilities.single().connectedOnLabel)
    }

    @Test
    fun `알림 설정은 다섯 타입의 순서와 타입별 리소스 및 상태를 매핑한다`() {
        val preferences =
            NotificationPreferences(
                isReservationAndScheduleEnabled = true,
                isFacilityNoticeEnabled = false,
                isChatAndMessageEnabled = true,
                isBenefitAndEventEnabled = false,
                isNightMarketingEnabled = true,
            )

        val model = mapper.mapNotificationSettings(DeviceNotificationPermission.BLOCKED, preferences)

        assertEquals(DeviceNotificationPermission.BLOCKED, model.permission)
        assertContentEquals(
            listOf(
                NotificationSettingType.RESERVATION_AND_SCHEDULE,
                NotificationSettingType.FACILITY_NOTICE,
                NotificationSettingType.CHAT_AND_MESSAGE,
                NotificationSettingType.BENEFIT_AND_EVENT,
                NotificationSettingType.NIGHT_MARKETING,
            ),
            model.items.map { it.type },
        )
        assertContentEquals(listOf(true, false, true, false, true), model.items.map { it.enabled })
        assertEquals(Res.string.notification_settings_reservation_title, model.items[0].title)
        assertEquals(Res.string.notification_settings_facility_notice_title, model.items[1].title)
        assertEquals(Res.string.notification_settings_chat_title, model.items[2].title)
        assertEquals(Res.string.notification_settings_benefit_title, model.items[3].title)
        assertEquals(Res.string.notification_settings_night_marketing_title, model.items[4].title)
    }

    @Test
    fun `요약과 프로필 매핑은 같은 프로필 UiModel 값을 만든다`() {
        val profile =
            MemberProfile(
                id = MemberId("member-3"),
                name = "변경된 이름",
                phoneNumber = "01098765432",
                email = "changed@example.com",
                profileImageUrl = null,
            )

        val profileModel = mapper.mapProfile(profile)
        val summaryModel = mapper.mapSummary(MyPageSummary(profile, isInstructorSignupBannerVisible = false))

        assertEquals(profileModel, summaryModel.profile)
        assertEquals(false, summaryModel.isInstructorSignupBannerVisible)
    }
}
