package com.classitda.domain.model.student.mypage

enum class NotificationSettingType {
    RESERVATION_AND_SCHEDULE,
    FACILITY_NOTICE,
    CHAT_AND_MESSAGE,
    BENEFIT_AND_EVENT,
    NIGHT_MARKETING,
}

data class NotificationPreferences(
    val isReservationAndScheduleEnabled: Boolean,
    val isFacilityNoticeEnabled: Boolean,
    val isChatAndMessageEnabled: Boolean,
    val isBenefitAndEventEnabled: Boolean,
    val isNightMarketingEnabled: Boolean,
)

enum class DeviceNotificationPermission {
    ALLOWED,
    BLOCKED,
    UNKNOWN,
}
