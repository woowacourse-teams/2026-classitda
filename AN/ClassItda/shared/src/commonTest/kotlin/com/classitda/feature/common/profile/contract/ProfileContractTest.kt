package com.classitda.feature.common.profile.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProfileContractTest {
    private val profile =
        MemberProfileUiModel(
            name = "김민지",
            phoneNumberLabel = "010-1234-5678",
            email = "member@example.com",
            profileImageUrl = null,
        )

    @Test
    fun `프로필 편집 action은 입력한 이름을 보존한다`() {
        val action = ProfileEditAction.NameChanged("  김민지  ")
        assertEquals("  김민지  ", action.name)
    }

    @Test
    fun `프로필 action은 사진 번호 저장과 외부 흐름을 구분한다`() {
        val actions =
            setOf(
                ProfileEditAction.RequestPhotoChange,
                ProfileEditAction.OpenPhoneNumberChange,
                ProfileEditAction.Save,
                ProfileViewAction.OpenEdit,
                ProfileViewAction.RequestLogout,
                ProfileViewAction.RequestWithdrawal,
            )
        assertEquals(6, actions.size)
        assertIs<ProfileEditAction.Save>(ProfileEditAction.Save)
        assertIs<ProfileViewAction.RequestWithdrawal>(ProfileViewAction.RequestWithdrawal)
    }

    @Test
    fun `전화번호 오류 상태는 역할 무관 오류와 입력 상태를 보존한다`() {
        val state =
            PhoneNumberChangeUiState.Error(
                phoneNumber = "01012345678",
                verificationCode = "123456",
                reason = PhoneNumberChangeUiError.VERIFICATION_FAILED,
                remainingSeconds = 97,
            )
        assertEquals(PhoneNumberChangeUiError.VERIFICATION_FAILED, state.reason)
        assertEquals("123456", state.verificationCode)
        assertEquals(97, state.remainingSeconds)
    }

    @Test
    fun `전화번호 인증 완료 상태는 입력한 인증번호를 보존한다`() {
        val state = PhoneNumberChangeUiState.Verified("01012345678", "123456")
        assertEquals("123456", state.verificationCode)
    }

    @Test
    fun `저장 중과 저장 실패는 공통 ProfileEditUiState의 배타적인 상태다`() {
        assertIs<ProfileEditUiState.Saving>(ProfileEditUiState.Saving(profile, "01012345678", "김민지"))
        assertIs<ProfileEditUiState.SaveFailed>(
            ProfileEditUiState.SaveFailed(profile, "01012345678", "김민지", ProfileUiError.CONFLICT),
        )
    }
}
