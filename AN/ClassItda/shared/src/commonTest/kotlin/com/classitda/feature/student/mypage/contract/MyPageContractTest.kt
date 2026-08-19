package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.model.student.mypage.PhoneVerificationId
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
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
        val action =
            NotificationSettingsAction.Toggle(
                type = NotificationSettingType.CHAT_AND_MESSAGE,
                enabled = true,
            )

        assertEquals(NotificationSettingType.CHAT_AND_MESSAGE, action.type)
        assertEquals(true, action.enabled)
    }

    @Test
    fun `프로필 편집 action은 입력한 이름을 보존한다`() {
        val action = ProfileEditAction.NameChanged(name = "  김민지  ")

        assertEquals("  김민지  ", action.name)
    }

    @Test
    fun `프로필 편집 action은 사진 번호 저장 행동을 서로 다르게 구분한다`() {
        val actions =
            setOf(
                ProfileEditAction.RequestPhotoChange,
                ProfileEditAction.OpenPhoneNumberChange,
                ProfileEditAction.Save,
            )

        assertEquals(3, actions.size)
        assertIs<ProfileEditAction.RequestPhotoChange>(ProfileEditAction.RequestPhotoChange)
        assertIs<ProfileEditAction.OpenPhoneNumberChange>(ProfileEditAction.OpenPhoneNumberChange)
        assertIs<ProfileEditAction.Save>(ProfileEditAction.Save)
    }

    @Test
    fun `프로필 조회 action은 편집 로그아웃 회원 탈퇴를 서로 다르게 구분한다`() {
        val actions =
            setOf(
                ProfileViewAction.OpenEdit,
                ProfileViewAction.RequestLogout,
                ProfileViewAction.RequestWithdrawal,
            )

        assertEquals(3, actions.size)
        assertIs<ProfileViewAction.OpenEdit>(ProfileViewAction.OpenEdit)
        assertIs<ProfileViewAction.RequestLogout>(ProfileViewAction.RequestLogout)
        assertIs<ProfileViewAction.RequestWithdrawal>(ProfileViewAction.RequestWithdrawal)
    }

    @Test
    fun `전화번호 오류 상태는 인증 ID와 도메인 실패 이유를 함께 보존한다`() {
        val verificationId = PhoneVerificationId("verification-1")
        val state =
            PhoneNumberChangeUiState.Error(
                phoneNumber = "01012345678",
                verificationCode = "123456",
                verificationId = verificationId,
                reason = MyPageFailureReason.VERIFICATION_FAILED,
                remainingSeconds = 97,
            )

        assertEquals(verificationId, state.verificationId)
        assertEquals(MyPageFailureReason.VERIFICATION_FAILED, state.reason)
        assertEquals(97, state.remainingSeconds)
    }

    @Test
    fun `전화번호 변경 action은 닫기 요청 코드 변경 완료를 서로 다르게 구분한다`() {
        val codeAction = PhoneNumberChangeAction.VerificationCodeChanged("123456")
        val actions =
            setOf(
                PhoneNumberChangeAction.Back,
                PhoneNumberChangeAction.RequestVerification,
                codeAction,
                PhoneNumberChangeAction.Complete,
            )

        assertEquals(4, actions.size)
        assertEquals("123456", codeAction.verificationCode)
        assertIs<PhoneNumberChangeAction.Back>(PhoneNumberChangeAction.Back)
        assertIs<PhoneNumberChangeAction.RequestVerification>(PhoneNumberChangeAction.RequestVerification)
        assertIs<PhoneNumberChangeAction.Complete>(PhoneNumberChangeAction.Complete)
    }

    @Test
    fun `전화번호 인증 성공 상태는 입력한 인증번호를 보존한다`() {
        val state =
            PhoneNumberChangeUiState.Verified(
                phoneNumber = "01012345678",
                verificationCode = "123456",
            )

        assertEquals("123456", state.verificationCode)
    }

    @Test
    fun `저장 중과 저장 실패는 ProfileEditUiState의 배타적인 상태다`() {
        assertIs<ProfileEditUiState.Saving>(
            ProfileEditUiState.Saving(
                profile = TestFixtures.profile,
                draftName = "김민지",
            ),
        )
        assertIs<ProfileEditUiState.SaveFailed>(
            ProfileEditUiState.SaveFailed(
                profile = TestFixtures.profile,
                draftName = "김민지",
                reason = MyPageFailureReason.CONFLICT,
            ),
        )
    }

    private object TestFixtures {
        val profile =
            MemberProfile(
                id = MemberId("member-1"),
                name = "김민지",
                phoneNumber = "010-1234-5678",
                email = "class12345@gmail.com",
                profileImageUrl = null,
            )
    }
}
