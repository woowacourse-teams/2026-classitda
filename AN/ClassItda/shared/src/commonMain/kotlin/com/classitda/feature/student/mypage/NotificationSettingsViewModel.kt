package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.NotificationSettingsAction
import com.classitda.feature.student.mypage.contract.NotificationSettingsUiState
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class NotificationSettingsViewModel(
    private val repository: MyPageRepository,
    private val mapper: MyPageUiMapper,
    private val devicePermission: DeviceNotificationPermission = DeviceNotificationPermission.UNKNOWN,
) : ViewModel() {
    private val _uiState = MutableStateFlow<NotificationSettingsUiState>(NotificationSettingsUiState.Loading)
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    private var isLoading = false

    init {
        loadPreferences()
    }

    fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.Retry -> retry()
            is NotificationSettingsAction.Toggle -> toggle(action.type, action.enabled)
            NotificationSettingsAction.Back -> Unit
        }
    }

    private fun retry() {
        when (val state = _uiState.value) {
            is NotificationSettingsUiState.UpdateFailed -> toggle(state.type, state.requestedEnabled)
            is NotificationSettingsUiState.Error -> loadPreferences()
            else -> Unit
        }
    }

    private fun loadPreferences() {
        if (isLoading) return
        isLoading = true
        _uiState.value = NotificationSettingsUiState.Loading

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getNotificationPreferences()) {
                    is MyPageResult.Success -> contentState(result.value)
                    is MyPageResult.Failure -> NotificationSettingsUiState.Error(result.reason)
                }
            isLoading = false
        }
    }

    private fun toggle(
        type: NotificationSettingType,
        requestedEnabled: Boolean,
    ) {
        val current =
            when (val state = _uiState.value) {
                is NotificationSettingsUiState.Content -> state.preferences
                is NotificationSettingsUiState.UpdateFailed -> state.preferences
                else -> return
            }
        if (_uiState.value is NotificationSettingsUiState.Updating) return

        val optimistic = current.withSetting(type, requestedEnabled)
        _uiState.value =
            NotificationSettingsUiState.Updating(
                permission = devicePermission,
                preferences = optimistic,
                type = type,
                requestedEnabled = requestedEnabled,
                uiModel = mapper.mapNotificationSettings(devicePermission, optimistic),
            )
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateNotificationSetting(type, requestedEnabled)) {
                    is MyPageResult.Success -> {
                        contentState(result.value)
                    }

                    is MyPageResult.Failure -> {
                        NotificationSettingsUiState.UpdateFailed(
                            permission = devicePermission,
                            preferences = current,
                            type = type,
                            requestedEnabled = requestedEnabled,
                            reason = result.reason,
                            uiModel = mapper.mapNotificationSettings(devicePermission, current),
                        )
                    }
                }
        }
    }

    private fun contentState(preferences: NotificationPreferences): NotificationSettingsUiState.Content =
        NotificationSettingsUiState.Content(
            permission = devicePermission,
            preferences = preferences,
            uiModel = mapper.mapNotificationSettings(devicePermission, preferences),
        )

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
}
