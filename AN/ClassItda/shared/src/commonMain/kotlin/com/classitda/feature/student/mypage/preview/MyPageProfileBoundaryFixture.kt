package com.classitda.feature.student.mypage.preview

import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.PhoneVerificationId
import com.classitda.feature.student.mypage.contract.PhoneNumberChangeUiState
import com.classitda.feature.student.mypage.contract.ProfileEditUiState
import com.classitda.feature.student.mypage.contract.ProfileViewUiState

internal object MyPageProfileBoundaryFixture {
    private const val LONG_NAME = "김민지알렉산드라테오도라매우긴회원이름"
    private const val LONG_PHONE_NUMBER = "01012345678901234567"
    private const val LONG_EMAIL = "very.long.member.email.address.for.profile.boundary@example.classitda.com"

    private val profile =
        MemberProfile(
            id = MemberId("member-profile-boundary-preview"),
            name = LONG_NAME,
            phoneNumber = LONG_PHONE_NUMBER,
            email = LONG_EMAIL,
            profileImageUrl = null,
        )

    val profileViewState = ProfileViewUiState.Content(profile)

    val profileEditState =
        ProfileEditUiState.Editing(
            profile = profile,
            draftName = "$LONG_NAME 수정 중",
            canSave = true,
        )

    val phoneNumberChangeState =
        PhoneNumberChangeUiState.CodeEntry(
            phoneNumber = LONG_PHONE_NUMBER,
            verificationCode = "12345",
            verificationId = PhoneVerificationId("verification-boundary-preview"),
            remainingSeconds = 180,
        )
}
