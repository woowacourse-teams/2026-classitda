package com.classitda.feature.common.profile.preview

import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileViewUiState

internal object ProfileBoundaryPreviewFixture {
    private const val LONG_NAME = "김민지알렉산드라테오도라매우긴회원이름"
    private const val LONG_PHONE_NUMBER = "01012345678901234567"
    private const val LONG_EMAIL = "very.long.member.email.address.for.profile.boundary@example.classitda.com"

    private val profile =
        MemberProfileUiModel(
            name = LONG_NAME,
            phoneNumberLabel = LONG_PHONE_NUMBER,
            email = LONG_EMAIL,
            profileImageUrl = null,
        )

    val profileViewState = ProfileViewUiState.Content(profile)

    val profileEditState =
        ProfileEditUiState.Editing(
            profile = profile,
            phoneNumber = LONG_PHONE_NUMBER,
            draftName = "$LONG_NAME 수정 중",
            canSave = true,
        )

    val phoneNumberChangeState =
        PhoneNumberChangeUiState.CodeEntry(
            phoneNumber = LONG_PHONE_NUMBER,
            verificationCode = "12345",
            remainingSeconds = 180,
        )
}
