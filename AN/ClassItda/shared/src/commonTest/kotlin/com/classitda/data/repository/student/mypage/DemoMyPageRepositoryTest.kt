package com.classitda.data.repository.student.mypage

import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.model.student.mypage.FacilityId
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.model.student.mypage.PhoneVerificationId
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.domain.repository.student.mypage.PhoneVerificationChallenge
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DemoMyPageRepositoryTest {
    @Test
    fun `조회 실패를 화면별로 결정적으로 설정할 수 있다`() =
        runBlocking {
            val repository =
                DemoMyPageRepository(
                    failures =
                        DemoMyPageRepositoryFailures(
                            summary = MyPageFailureReason.NETWORK,
                            profile = MyPageFailureReason.NOT_FOUND,
                            connectedFacilities = MyPageFailureReason.UNKNOWN,
                            notificationPreferences = MyPageFailureReason.CONFLICT,
                            requestPhoneVerification = MyPageFailureReason.VERIFICATION_EXPIRED,
                        ),
                )

            assertEquals(
                MyPageResult.Failure(MyPageFailureReason.NETWORK),
                repository.getSummary(),
            )
            assertEquals(
                MyPageResult.Failure(MyPageFailureReason.NOT_FOUND),
                repository.getProfile(),
            )
            assertEquals(
                MyPageResult.Failure(MyPageFailureReason.UNKNOWN),
                repository.getConnectedFacilities(),
            )
            assertEquals(
                MyPageResult.Failure(MyPageFailureReason.CONFLICT),
                repository.getNotificationPreferences(),
            )
            assertEquals(
                MyPageResult.Failure(MyPageFailureReason.VERIFICATION_EXPIRED),
                repository.requestPhoneVerification("01011112222"),
            )
        }

    @Test
    fun `프로필 이름 저장 후 요약과 프로필 조회가 같은 이름을 공유한다`() =
        runBlocking {
            val repository =
                DemoMyPageRepository(
                    profile = profile(),
                )

            val result = repository.updateProfileName(MemberId("member-1"), "새 이름")

            assertEquals("새 이름", assertIs<MyPageResult.Success<MemberProfile>>(result).value.name)
            assertEquals("새 이름", assertIs<MyPageResult.Success<MemberProfile>>(repository.getProfile()).value.name)
            assertEquals(
                "새 이름",
                assertIs<MyPageResult.Success<MyPageSummary>>(repository.getSummary()).value.profile.name,
            )
        }

    @Test
    fun `프로필 저장 실패는 이전 값을 보존한다`() =
        runBlocking {
            val repository =
                DemoMyPageRepository(
                    profile = profile(),
                    failures = DemoMyPageRepositoryFailures(updateProfileName = MyPageFailureReason.CONFLICT),
                )
            val before = repository.profileSnapshot

            val result = repository.updateProfileName(before.id, "실패한 이름")

            assertEquals(MyPageResult.Failure(MyPageFailureReason.CONFLICT), result)
            assertEquals(before, repository.profileSnapshot)
        }

    @Test
    fun `시설의 ID와 개수는 빈 목록을 포함해 저장소 재조회에서 유지된다`() =
        runBlocking {
            val facilities =
                listOf(
                    ConnectedFacility(FacilityId("facility-a"), "A", LocalDate(2026, 1, 1)),
                    ConnectedFacility(FacilityId("facility-b"), "B", LocalDate(2026, 1, 2)),
                )
            val repository = DemoMyPageRepository(connectedFacilities = facilities)

            val result = assertIs<MyPageResult.Success<List<ConnectedFacility>>>(repository.getConnectedFacilities())

            assertEquals(2, result.value.size)
            assertContentEquals(listOf(FacilityId("facility-a"), FacilityId("facility-b")), result.value.map { it.id })

            val emptyRepository = DemoMyPageRepository(connectedFacilities = emptyList())
            assertEquals(
                emptyList(),
                assertIs<MyPageResult.Success<List<ConnectedFacility>>>(emptyRepository.getConnectedFacilities()).value,
            )
        }

    @Test
    fun `알림 토글 성공은 재진입 조회에 유지되고 실패는 이전 값으로 롤백된다`() =
        runBlocking {
            val repository = DemoMyPageRepository(notificationPreferences = preferences())

            val enabled =
                assertIs<MyPageResult.Success<NotificationPreferences>>(
                    repository.updateNotificationSetting(NotificationSettingType.NIGHT_MARKETING, true),
                ).value
            assertEquals(true, enabled.isNightMarketingEnabled)
            assertEquals(
                true,
                assertIs<MyPageResult.Success<NotificationPreferences>>(repository.getNotificationPreferences())
                    .value
                    .isNightMarketingEnabled,
            )

            repository.failures = DemoMyPageRepositoryFailures(updateNotificationSetting = MyPageFailureReason.NETWORK)
            val beforeFailure = repository.notificationPreferencesSnapshot
            val result = repository.updateNotificationSetting(NotificationSettingType.NIGHT_MARKETING, false)

            assertEquals(MyPageResult.Failure(MyPageFailureReason.NETWORK), result)
            assertEquals(beforeFailure, repository.notificationPreferencesSnapshot)
        }

    @Test
    fun `전화 인증 완료 후 프로필 재조회에 인증 번호가 공유된다`() =
        runBlocking {
            val repository = DemoMyPageRepository(profile = profile())
            val phoneNumber = "01011112222"
            val challenge =
                assertIs<MyPageResult.Success<PhoneVerificationChallenge>>(
                    repository.requestPhoneVerification(phoneNumber),
                ).value

            val result = repository.verifyPhoneNumber(challenge.verificationId, phoneNumber, "123456")

            assertEquals(phoneNumber, assertIs<MyPageResult.Success<MemberProfile>>(result).value.phoneNumber)
            assertEquals(
                phoneNumber,
                assertIs<MyPageResult.Success<MemberProfile>>(repository.getProfile()).value.phoneNumber,
            )
        }

    @Test
    fun `전화 인증 실패를 설정하면 번호가 변경되지 않는다`() =
        runBlocking {
            val repository =
                DemoMyPageRepository(
                    profile = profile(),
                    failures =
                        DemoMyPageRepositoryFailures(
                            verifyPhoneNumber = MyPageFailureReason.VERIFICATION_FAILED,
                        ),
                )
            val before = repository.profileSnapshot
            val result =
                repository.verifyPhoneNumber(
                    verificationId =
                        PhoneVerificationId("challenge"),
                    phoneNumber = "01011112222",
                    verificationCode = "123456",
                )

            assertEquals(MyPageResult.Failure(MyPageFailureReason.VERIFICATION_FAILED), result)
            assertEquals(before, repository.profileSnapshot)
        }

    private fun profile() =
        MemberProfile(
            id = MemberId("member-1"),
            name = "기본 이름",
            phoneNumber = "01012345678",
            email = "member@example.com",
            profileImageUrl = null,
        )

    private fun preferences() =
        NotificationPreferences(
            isReservationAndScheduleEnabled = true,
            isFacilityNoticeEnabled = true,
            isChatAndMessageEnabled = true,
            isBenefitAndEventEnabled = false,
            isNightMarketingEnabled = false,
        )
}
