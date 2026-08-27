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
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.domain.repository.student.mypage.PhoneVerificationChallenge
import kotlinx.datetime.LocalDate

internal data class DemoMyPageRepositoryFailures(
    val summary: MyPageFailureReason? = null,
    val profile: MyPageFailureReason? = null,
    val updateProfileName: MyPageFailureReason? = null,
    val connectedFacilities: MyPageFailureReason? = null,
    val notificationPreferences: MyPageFailureReason? = null,
    val updateNotificationSetting: MyPageFailureReason? = null,
    val requestPhoneVerification: MyPageFailureReason? = null,
    val verifyPhoneNumber: MyPageFailureReason? = null,
)

internal class DemoMyPageRepository(
    profile: MemberProfile = defaultProfile(),
    isInstructorSignupBannerVisible: Boolean = true,
    connectedFacilities: List<ConnectedFacility> = defaultConnectedFacilities(),
    notificationPreferences: NotificationPreferences = defaultNotificationPreferences(),
    failures: DemoMyPageRepositoryFailures = DemoMyPageRepositoryFailures(),
    private val demoVerificationId: PhoneVerificationId = PhoneVerificationId("demo-phone-verification"),
) : MyPageRepository {
    private var storedProfile = profile
    private val storedFacilities = connectedFacilities.toMutableList()
    private var storedPreferences = notificationPreferences
    private val bannerVisible = isInstructorSignupBannerVisible

    var failures: DemoMyPageRepositoryFailures = failures

    private var pendingPhoneNumber: String? = null
    private var pendingVerificationId: PhoneVerificationId? = null

    val profileSnapshot: MemberProfile
        get() = storedProfile

    val summarySnapshot: MyPageSummary
        get() = MyPageSummary(storedProfile, bannerVisible)

    val connectedFacilitiesSnapshot: List<ConnectedFacility>
        get() = storedFacilities.toList()

    val notificationPreferencesSnapshot: NotificationPreferences
        get() = storedPreferences

    override suspend fun getSummary(): MyPageResult<MyPageSummary> =
        failures.summary.toFailureOrNull()
            ?: MyPageResult.Success(summarySnapshot)

    override suspend fun getProfile(): MyPageResult<MemberProfile> =
        failures.profile.toFailureOrNull()
            ?: MyPageResult.Success(profileSnapshot)

    override suspend fun updateProfileName(
        memberId: MemberId,
        name: String,
    ): MyPageResult<MemberProfile> {
        failures.updateProfileName.toFailureOrNull()?.let { return it }
        if (memberId != storedProfile.id) {
            return MyPageResult.Failure(MyPageFailureReason.NOT_FOUND)
        }

        val updatedProfile = storedProfile.copy(name = name)
        storedProfile = updatedProfile
        return MyPageResult.Success(updatedProfile)
    }

    override suspend fun getConnectedFacilities(): MyPageResult<List<ConnectedFacility>> =
        failures.connectedFacilities.toFailureOrNull()
            ?: MyPageResult.Success(connectedFacilitiesSnapshot)

    override suspend fun getNotificationPreferences(): MyPageResult<NotificationPreferences> =
        failures.notificationPreferences.toFailureOrNull()
            ?: MyPageResult.Success(notificationPreferencesSnapshot)

    override suspend fun updateNotificationSetting(
        type: NotificationSettingType,
        enabled: Boolean,
    ): MyPageResult<NotificationPreferences> {
        failures.updateNotificationSetting.toFailureOrNull()?.let { return it }

        val updatedPreferences = storedPreferences.withSetting(type, enabled)
        storedPreferences = updatedPreferences
        return MyPageResult.Success(updatedPreferences)
    }

    override suspend fun requestPhoneVerification(phoneNumber: String): MyPageResult<PhoneVerificationChallenge> {
        failures.requestPhoneVerification.toFailureOrNull()?.let { return it }

        pendingPhoneNumber = phoneNumber
        pendingVerificationId = demoVerificationId
        return MyPageResult.Success(PhoneVerificationChallenge(demoVerificationId))
    }

    override suspend fun verifyPhoneNumber(
        verificationId: PhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ): MyPageResult<MemberProfile> {
        failures.verifyPhoneNumber.toFailureOrNull()?.let { return it }

        val isValid =
            verificationId == pendingVerificationId &&
                phoneNumber == pendingPhoneNumber &&
                verificationCode == DEMO_VERIFICATION_CODE
        if (!isValid) {
            return MyPageResult.Failure(MyPageFailureReason.VERIFICATION_FAILED)
        }

        val updatedProfile = storedProfile.copy(phoneNumber = phoneNumber)
        storedProfile = updatedProfile
        pendingPhoneNumber = null
        pendingVerificationId = null
        return MyPageResult.Success(updatedProfile)
    }

    private fun NotificationPreferences.withSetting(
        type: NotificationSettingType,
        enabled: Boolean,
    ): NotificationPreferences =
        when (type) {
            NotificationSettingType.RESERVATION_AND_SCHEDULE -> copy(isReservationAndScheduleEnabled = enabled)
            NotificationSettingType.FACILITY_NOTICE -> copy(isFacilityNoticeEnabled = enabled)
            NotificationSettingType.CHAT_AND_MESSAGE -> copy(isChatAndMessageEnabled = enabled)
            NotificationSettingType.BENEFIT_AND_EVENT -> copy(isBenefitAndEventEnabled = enabled)
            NotificationSettingType.NIGHT_MARKETING -> copy(isNightMarketingEnabled = enabled)
        }

    private fun MyPageFailureReason?.toFailureOrNull(): MyPageResult.Failure? =
        this?.let { reason -> MyPageResult.Failure(reason) }

    private companion object {
        const val DEMO_VERIFICATION_CODE = "123456"

        fun defaultProfile() =
            MemberProfile(
                id = MemberId("demo-member"),
                name = "클래스잇다 회원",
                phoneNumber = "01012345678",
                email = "member@classitda.com",
                profileImageUrl = null,
            )

        fun defaultConnectedFacilities() =
            listOf(
                ConnectedFacility(
                    id = FacilityId("demo-facility-1"),
                    name = "클래스잇다 스튜디오",
                    connectedOn = LocalDate(2026, 1, 15),
                ),
            )

        fun defaultNotificationPreferences() =
            NotificationPreferences(
                isReservationAndScheduleEnabled = true,
                isFacilityNoticeEnabled = true,
                isChatAndMessageEnabled = true,
                isBenefitAndEventEnabled = false,
                isNightMarketingEnabled = false,
            )
    }
}
