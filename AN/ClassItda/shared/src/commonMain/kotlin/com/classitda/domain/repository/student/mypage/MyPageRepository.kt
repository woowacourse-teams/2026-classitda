package com.classitda.domain.repository.student.mypage

import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.model.student.mypage.FacilityId
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.model.student.mypage.PhoneVerificationId

interface MyPageRepository {
    suspend fun getSummary(): MyPageResult<MyPageSummary>

    suspend fun getProfile(): MyPageResult<MemberProfile>

    suspend fun updateProfileName(
        memberId: MemberId,
        name: String,
    ): MyPageResult<MemberProfile>

    suspend fun getConnectedFacilities(): MyPageResult<List<ConnectedFacility>>

    suspend fun getNotificationPreferences(): MyPageResult<NotificationPreferences>

    suspend fun updateNotificationSetting(
        type: NotificationSettingType,
        enabled: Boolean,
    ): MyPageResult<NotificationPreferences>

    suspend fun requestPhoneVerification(phoneNumber: String): MyPageResult<PhoneVerificationChallenge>

    suspend fun verifyPhoneNumber(
        verificationId: PhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ): MyPageResult<MemberProfile>
}

data class PhoneVerificationChallenge(
    val verificationId: PhoneVerificationId,
)
