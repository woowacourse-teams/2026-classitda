package com.classitda.feature.student.mypage.preview

import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.FacilityId
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesUiState
import com.classitda.feature.student.mypage.contract.NotificationSettingsUiState
import kotlinx.datetime.LocalDate

internal object MyPageSettingsBoundaryFixture {
    val facilitiesEmpty = ConnectedFacilitiesUiState.Empty

    val facilitiesOne =
        ConnectedFacilitiesUiState.Content(
            facilities =
                listOf(
                    ConnectedFacility(
                        id = FacilityId("facility-boundary-one"),
                        name = "필라테스 더 밸런스 강남점",
                        connectedOn = LocalDate(2023, 12, 1),
                    ),
                ),
        )

    val facilitiesMany =
        ConnectedFacilitiesUiState.Content(
            facilities =
                listOf(
                    ConnectedFacility(
                        id = FacilityId("facility-boundary-gangnam"),
                        name = "필라테스 더 밸런스 강남점",
                        connectedOn = LocalDate(2023, 12, 1),
                    ),
                    ConnectedFacility(
                        id = FacilityId("facility-boundary-seocho"),
                        name = "에이원 휘트니스 서초",
                        connectedOn = LocalDate(2024, 2, 15),
                    ),
                    ConnectedFacility(
                        id = FacilityId("facility-boundary-gyodae"),
                        name = "요가스테이 교대점",
                        connectedOn = LocalDate(2024, 5, 20),
                    ),
                ),
        )

    val facilitiesLongName =
        ConnectedFacilitiesUiState.Content(
            facilities =
                listOf(
                    ConnectedFacility(
                        id = FacilityId("facility-boundary-long-name"),
                        name = "필라테스와 요가 및 재활 운동을 함께 운영하는 클래스잇다 프리미엄 센터 강남 본점",
                        connectedOn = LocalDate(2024, 12, 31),
                    ),
                ),
        )

    private val preferences =
        NotificationPreferences(
            isReservationAndScheduleEnabled = true,
            isFacilityNoticeEnabled = false,
            isChatAndMessageEnabled = true,
            isBenefitAndEventEnabled = false,
            isNightMarketingEnabled = true,
        )

    val notificationsAllowed =
        NotificationSettingsUiState.Content(
            permission = DeviceNotificationPermission.ALLOWED,
            preferences = preferences,
        )

    val notificationsBlocked =
        NotificationSettingsUiState.Content(
            permission = DeviceNotificationPermission.BLOCKED,
            preferences = preferences,
        )

    val notificationsUnknown =
        NotificationSettingsUiState.Content(
            permission = DeviceNotificationPermission.UNKNOWN,
            preferences = preferences,
        )

    val notificationsUpdating =
        NotificationSettingsUiState.Updating(
            permission = DeviceNotificationPermission.ALLOWED,
            preferences = preferences,
            type = NotificationSettingType.RESERVATION_AND_SCHEDULE,
            requestedEnabled = true,
        )

    val notificationsUpdateFailed =
        NotificationSettingsUiState.UpdateFailed(
            permission = DeviceNotificationPermission.ALLOWED,
            preferences = preferences,
            type = NotificationSettingType.RESERVATION_AND_SCHEDULE,
            requestedEnabled = false,
            reason = MyPageFailureReason.NETWORK,
        )
}
