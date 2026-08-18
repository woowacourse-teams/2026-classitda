package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.repository.student.mypage.MyPageFailureReason

sealed interface NotificationSettingsUiState {
    data object Loading : NotificationSettingsUiState

    data class Content(
        val permission: DeviceNotificationPermission,
        val preferences: NotificationPreferences,
    ) : NotificationSettingsUiState

    data class Updating(
        val permission: DeviceNotificationPermission,
        val preferences: NotificationPreferences,
        val type: NotificationSettingType,
        val requestedEnabled: Boolean,
    ) : NotificationSettingsUiState

    data class UpdateFailed(
        val permission: DeviceNotificationPermission,
        val preferences: NotificationPreferences,
        val type: NotificationSettingType,
        val requestedEnabled: Boolean,
        val reason: MyPageFailureReason,
    ) : NotificationSettingsUiState

    data class Error(
        val reason: MyPageFailureReason,
    ) : NotificationSettingsUiState
}

sealed interface NotificationSettingsAction {
    data object Back : NotificationSettingsAction

    data object Retry : NotificationSettingsAction

    data class Toggle(
        val type: NotificationSettingType,
        val enabled: Boolean,
    ) : NotificationSettingsAction
}
