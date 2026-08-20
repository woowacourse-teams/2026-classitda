package com.classitda.feature.student.mypage.mapper

import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.notification_settings_benefit_description
import classitda.shared.generated.resources.notification_settings_benefit_title
import classitda.shared.generated.resources.notification_settings_chat_description
import classitda.shared.generated.resources.notification_settings_chat_title
import classitda.shared.generated.resources.notification_settings_facility_notice_description
import classitda.shared.generated.resources.notification_settings_facility_notice_title
import classitda.shared.generated.resources.notification_settings_night_marketing_description
import classitda.shared.generated.resources.notification_settings_night_marketing_title
import classitda.shared.generated.resources.notification_settings_reservation_description
import classitda.shared.generated.resources.notification_settings_reservation_title
import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.feature.student.mypage.contract.ConnectedFacilityUiModel
import com.classitda.feature.student.mypage.contract.MemberProfileUiModel
import com.classitda.feature.student.mypage.contract.MyPageSummaryUiModel
import com.classitda.feature.student.mypage.contract.NotificationSettingUiModel
import com.classitda.feature.student.mypage.contract.NotificationSettingsUiModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.StringResource

internal class MyPageUiMapper {
    fun mapSummary(summary: MyPageSummary): MyPageSummaryUiModel =
        MyPageSummaryUiModel(
            profile = mapProfile(summary.profile),
            isInstructorSignupBannerVisible = summary.isInstructorSignupBannerVisible,
        )

    fun mapProfile(profile: MemberProfile): MemberProfileUiModel =
        MemberProfileUiModel(
            id = profile.id,
            name = profile.name,
            phoneNumberLabel = formatPhoneNumber(profile.phoneNumber),
            email = profile.email,
            profileImageUrl = profile.profileImageUrl,
        )

    fun mapConnectedFacilities(facilities: List<ConnectedFacility>): List<ConnectedFacilityUiModel> =
        facilities.map { facility ->
            ConnectedFacilityUiModel(
                id = facility.id,
                name = facility.name,
                connectedOnLabel = facility.connectedOn.toDisplayDate(),
            )
        }

    fun mapNotificationSettings(
        permission: DeviceNotificationPermission,
        preferences: NotificationPreferences,
    ): NotificationSettingsUiModel =
        NotificationSettingsUiModel(
            permission = permission,
            items =
                notificationSettingTypes.map { type ->
                    NotificationSettingUiModel(
                        type = type,
                        title = type.titleResource(),
                        description = type.descriptionResource(),
                        enabled = preferences.isEnabled(type),
                    )
                },
        )

    private fun formatPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter { it in '0'..'9' }
        return when {
            digits.length == 11 -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.takeLast(4)}"
            digits.length == 10 -> "${digits.take(3)}-${digits.substring(3, 6)}-${digits.takeLast(4)}"
            else -> phoneNumber
        }
    }

    private fun LocalDate.toDisplayDate(): String =
        "${year.toString().padStart(
            4,
            '0',
        )}.${month.number.toString().padStart(2, '0')}.${day.toString().padStart(2, '0')}"

    private fun NotificationPreferences.isEnabled(type: NotificationSettingType): Boolean =
        when (type) {
            NotificationSettingType.RESERVATION_AND_SCHEDULE -> isReservationAndScheduleEnabled
            NotificationSettingType.FACILITY_NOTICE -> isFacilityNoticeEnabled
            NotificationSettingType.CHAT_AND_MESSAGE -> isChatAndMessageEnabled
            NotificationSettingType.BENEFIT_AND_EVENT -> isBenefitAndEventEnabled
            NotificationSettingType.NIGHT_MARKETING -> isNightMarketingEnabled
        }

    private fun NotificationSettingType.titleResource(): StringResource =
        when (this) {
            NotificationSettingType.RESERVATION_AND_SCHEDULE -> Res.string.notification_settings_reservation_title
            NotificationSettingType.FACILITY_NOTICE -> Res.string.notification_settings_facility_notice_title
            NotificationSettingType.CHAT_AND_MESSAGE -> Res.string.notification_settings_chat_title
            NotificationSettingType.BENEFIT_AND_EVENT -> Res.string.notification_settings_benefit_title
            NotificationSettingType.NIGHT_MARKETING -> Res.string.notification_settings_night_marketing_title
        }

    private fun NotificationSettingType.descriptionResource(): StringResource =
        when (this) {
            NotificationSettingType.RESERVATION_AND_SCHEDULE -> Res.string.notification_settings_reservation_description
            NotificationSettingType.FACILITY_NOTICE -> Res.string.notification_settings_facility_notice_description
            NotificationSettingType.CHAT_AND_MESSAGE -> Res.string.notification_settings_chat_description
            NotificationSettingType.BENEFIT_AND_EVENT -> Res.string.notification_settings_benefit_description
            NotificationSettingType.NIGHT_MARKETING -> Res.string.notification_settings_night_marketing_description
        }

    private companion object {
        val notificationSettingTypes =
            listOf(
                NotificationSettingType.RESERVATION_AND_SCHEDULE,
                NotificationSettingType.FACILITY_NOTICE,
                NotificationSettingType.CHAT_AND_MESSAGE,
                NotificationSettingType.BENEFIT_AND_EVENT,
                NotificationSettingType.NIGHT_MARKETING,
            )
    }
}
