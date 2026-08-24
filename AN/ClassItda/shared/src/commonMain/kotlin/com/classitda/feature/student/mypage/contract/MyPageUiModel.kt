package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.FacilityId
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import org.jetbrains.compose.resources.StringResource

data class MyPageSummaryUiModel(
    val profile: MemberProfileUiModel,
    val isInstructorSignupBannerVisible: Boolean,
)

data class ConnectedFacilityUiModel(
    val id: FacilityId,
    val name: String,
    val connectedOnLabel: String,
)

data class NotificationSettingUiModel(
    val type: NotificationSettingType,
    val title: StringResource,
    val description: StringResource,
    val enabled: Boolean,
)

data class NotificationSettingsUiModel(
    val permission: DeviceNotificationPermission,
    val items: List<NotificationSettingUiModel>,
)
