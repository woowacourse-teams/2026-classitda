package com.classitda.feature.student.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.my_page_notification_settings
import classitda.shared.generated.resources.notification_settings_back
import classitda.shared.generated.resources.notification_settings_benefit_description
import classitda.shared.generated.resources.notification_settings_benefit_title
import classitda.shared.generated.resources.notification_settings_chat_description
import classitda.shared.generated.resources.notification_settings_chat_title
import classitda.shared.generated.resources.notification_settings_device_permission_description
import classitda.shared.generated.resources.notification_settings_device_permission_title
import classitda.shared.generated.resources.notification_settings_error_description
import classitda.shared.generated.resources.notification_settings_error_title
import classitda.shared.generated.resources.notification_settings_facility_notice_description
import classitda.shared.generated.resources.notification_settings_facility_notice_title
import classitda.shared.generated.resources.notification_settings_loading
import classitda.shared.generated.resources.notification_settings_night_marketing_description
import classitda.shared.generated.resources.notification_settings_night_marketing_title
import classitda.shared.generated.resources.notification_settings_permission_allowed
import classitda.shared.generated.resources.notification_settings_permission_blocked
import classitda.shared.generated.resources.notification_settings_permission_unknown
import classitda.shared.generated.resources.notification_settings_reservation_description
import classitda.shared.generated.resources.notification_settings_reservation_title
import classitda.shared.generated.resources.notification_settings_retry
import classitda.shared.generated.resources.notification_settings_service_group
import classitda.shared.generated.resources.notification_settings_state_off
import classitda.shared.generated.resources.notification_settings_state_on
import classitda.shared.generated.resources.notification_settings_state_saving_off
import classitda.shared.generated.resources.notification_settings_state_saving_on
import classitda.shared.generated.resources.notification_settings_update_failed
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.mypage.DeviceNotificationPermission
import com.classitda.domain.model.student.mypage.NotificationPreferences
import com.classitda.domain.model.student.mypage.NotificationSettingType
import com.classitda.feature.student.mypage.contract.NotificationSettingsAction
import com.classitda.feature.student.mypage.contract.NotificationSettingsUiState
import com.classitda.feature.student.mypage.preview.MyPageSettingsBoundaryFixture
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val serviceNotificationTypes =
    listOf(
        NotificationSettingType.RESERVATION_AND_SCHEDULE,
    )

@Composable
fun NotificationSettingsScreen(
    uiState: NotificationSettingsUiState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NotificationSettingsTopBar(
                onBack = { onAction(NotificationSettingsAction.Back) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            NotificationSettingsUiState.Loading -> {
                NotificationSettingsLoadingContent(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is NotificationSettingsUiState.Content -> {
                NotificationSettingsContent(
                    permission = uiState.permission,
                    preferences = uiState.preferences,
                    onToggle = { type, enabled ->
                        onAction(NotificationSettingsAction.Toggle(type, enabled))
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is NotificationSettingsUiState.Updating -> {
                NotificationSettingsContent(
                    permission = uiState.permission,
                    preferences = uiState.preferences,
                    updatingType = uiState.type,
                    onToggle = { type, enabled ->
                        onAction(NotificationSettingsAction.Toggle(type, enabled))
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is NotificationSettingsUiState.UpdateFailed -> {
                NotificationSettingsContent(
                    permission = uiState.permission,
                    preferences = uiState.preferences,
                    failedType = uiState.type,
                    onToggle = { type, enabled ->
                        onAction(NotificationSettingsAction.Toggle(type, enabled))
                    },
                    onRetry = { onAction(NotificationSettingsAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is NotificationSettingsUiState.Error -> {
                NotificationSettingsErrorContent(
                    onRetry = { onAction(NotificationSettingsAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsTopBar(onBack: () -> Unit) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.notification_settings_back),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = stringResource(Res.string.my_page_notification_settings),
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { heading() },
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun NotificationSettingsContent(
    permission: DeviceNotificationPermission,
    preferences: NotificationPreferences,
    onToggle: (NotificationSettingType, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    updatingType: NotificationSettingType? = null,
    failedType: NotificationSettingType? = null,
    onRetry: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.sectionGap),
    ) {
        item(key = "device-permission") {
            NotificationPermissionStatus(
                permission = permission,
            )
        }
        item(key = "service-group") {
            NotificationGroupHeader(
                title = stringResource(Res.string.notification_settings_service_group),
            )
        }
        items(
            items = serviceNotificationTypes,
            key = { type -> type.name },
        ) { type ->
            NotificationSettingItem(
                type = type,
                checked = preferences.isEnabled(type),
                isUpdating = updatingType == type,
                hasFailure = failedType == type,
                onToggle = onToggle,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun NotificationPermissionStatus(
    permission: DeviceNotificationPermission,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    val (statusTextRes, statusColor) =
        when (permission) {
            DeviceNotificationPermission.ALLOWED -> {
                Res.string.notification_settings_permission_allowed to MaterialTheme.colorScheme.primary
            }

            DeviceNotificationPermission.BLOCKED -> {
                Res.string.notification_settings_permission_blocked to MaterialTheme.colorScheme.error
            }

            DeviceNotificationPermission.UNKNOWN -> {
                Res.string.notification_settings_permission_unknown to MaterialTheme.colorScheme.onSurfaceVariant
            }
        }

    val permissionText = stringResource(statusTextRes)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = AppSpacing.screenPadding,
                    top = AppSpacing.lg,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.notification_settings_device_permission_title),
                modifier = Modifier.weight(1f),
                style = typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = permissionText,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = statusColor,
            )
        }
        Text(
            text = stringResource(Res.string.notification_settings_device_permission_description),
            style = typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationGroupHeader(title: String) {
    val typography = appTypography()

    Text(
        text = title,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    start = AppSpacing.screenPadding,
                    top = AppSpacing.xxl,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.md,
                ).semantics { heading() },
        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun NotificationSettingItem(
    type: NotificationSettingType,
    checked: Boolean,
    isUpdating: Boolean,
    hasFailure: Boolean,
    onToggle: (NotificationSettingType, Boolean) -> Unit,
    onRetry: (() -> Unit)?,
) {
    val typography = appTypography()
    val copy = type.displayCopy()
    val stateText =
        when {
            isUpdating && checked -> stringResource(Res.string.notification_settings_state_saving_on)
            isUpdating -> stringResource(Res.string.notification_settings_state_saving_off)
            checked -> stringResource(Res.string.notification_settings_state_on)
            else -> stringResource(Res.string.notification_settings_state_off)
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = checked,
                        enabled = !isUpdating,
                        role = Role.Switch,
                        onValueChange = { enabled -> onToggle(type, enabled) },
                    ).semantics(mergeDescendants = true) {
                        stateDescription = stateText
                        if (isUpdating) {
                            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                        }
                    }.padding(
                        horizontal = AppSpacing.screenPadding,
                        vertical = AppSpacing.xxl,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = stringResource(copy.title),
                    modifier = Modifier.fillMaxWidth(),
                    style = typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(copy.description),
                    modifier = Modifier.fillMaxWidth(),
                    style = typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .size(AppSpacing.xxl)
                            .clearAndSetSemantics {},
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = AppSpacing.xs / 2,
                )
            } else {
                Switch(
                    checked = checked,
                    onCheckedChange = null,
                    modifier = Modifier.clearAndSetSemantics {},
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        ),
                )
            }
        }
        if (hasFailure) {
            NotificationSettingFailure(onRetry = requireNotNull(onRetry))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun NotificationSettingFailure(onRetry: () -> Unit) {
    val typography = appTypography()
    val message = stringResource(Res.string.notification_settings_update_failed)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    error(message)
                    liveRegion = LiveRegionMode.Polite
                }.padding(
                    start = AppSpacing.screenPadding,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.lg,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        TextButton(onClick = onRetry) {
            Text(
                text = stringResource(Res.string.notification_settings_retry),
                style = typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NotificationSettingsLoadingContent(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.notification_settings_loading),
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationSettingsErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val errorTitle = stringResource(Res.string.notification_settings_error_title)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenPadding)
                .semantics { error(errorTitle) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = errorTitle,
            modifier = Modifier.fillMaxWidth(),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = stringResource(Res.string.notification_settings_error_description),
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        OutlinedButton(onClick = onRetry) {
            Text(
                text = stringResource(Res.string.notification_settings_retry),
                style = typography.labelLarge,
            )
        }
    }
}

private fun NotificationPreferences.isEnabled(type: NotificationSettingType): Boolean =
    when (type) {
        NotificationSettingType.RESERVATION_AND_SCHEDULE -> isReservationAndScheduleEnabled
        NotificationSettingType.FACILITY_NOTICE -> isFacilityNoticeEnabled
        NotificationSettingType.CHAT_AND_MESSAGE -> isChatAndMessageEnabled
        NotificationSettingType.BENEFIT_AND_EVENT -> isBenefitAndEventEnabled
        NotificationSettingType.NIGHT_MARKETING -> isNightMarketingEnabled
    }

private fun NotificationSettingType.displayCopy(): NotificationSettingCopy =
    when (this) {
        NotificationSettingType.RESERVATION_AND_SCHEDULE -> {
            NotificationSettingCopy(
                title = Res.string.notification_settings_reservation_title,
                description = Res.string.notification_settings_reservation_description,
            )
        }

        NotificationSettingType.FACILITY_NOTICE -> {
            NotificationSettingCopy(
                title = Res.string.notification_settings_facility_notice_title,
                description = Res.string.notification_settings_facility_notice_description,
            )
        }

        NotificationSettingType.CHAT_AND_MESSAGE -> {
            NotificationSettingCopy(
                title = Res.string.notification_settings_chat_title,
                description = Res.string.notification_settings_chat_description,
            )
        }

        NotificationSettingType.BENEFIT_AND_EVENT -> {
            NotificationSettingCopy(
                title = Res.string.notification_settings_benefit_title,
                description = Res.string.notification_settings_benefit_description,
            )
        }

        NotificationSettingType.NIGHT_MARKETING -> {
            NotificationSettingCopy(
                title = Res.string.notification_settings_night_marketing_title,
                description = Res.string.notification_settings_night_marketing_description,
            )
        }
    }

private data class NotificationSettingCopy(
    val title: StringResource,
    val description: StringResource,
)

private fun NotificationSettingsAction.previewLabel(): String =
    when (this) {
        NotificationSettingsAction.Back -> "Back"
        NotificationSettingsAction.Retry -> "Retry"
        is NotificationSettingsAction.Toggle -> "Toggle(type=${type.name}, requestedEnabled=$enabled)"
    }

@Preview(
    name = "F07 · 05-C · Student",
    group = "Screen/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsAllowed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Item updating · Student",
    group = "Screen/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_ItemUpdating_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsUpdating,
            onAction = {},
        )
    }
}

@Preview(
    name = "Update failed · Student",
    group = "Screen/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_UpdateFailed_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsUpdateFailed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Permission blocked · Student",
    group = "Screen/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_PermissionBlocked_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsBlocked,
            onAction = {},
        )
    }
}

@Preview(
    name = "Permission unknown · Student",
    group = "Boundary/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_PermissionUnknown_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsUnknown,
            onAction = {},
        )
    }
}

@Preview(
    name = "Long strings · Large font · Small screen",
    group = "Boundary/NotificationSettings/05-C",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun NotificationSettingsScreenPreview_LongStrings_LargeFont_SmallScreen() {
    AppTheme(theme = ThemeType.STUDENT) {
        NotificationSettingsScreen(
            uiState = MyPageSettingsBoundaryFixture.notificationsAllowed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Actions · Student · Interactive",
    group = "Harness/NotificationSettings/05-C",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun NotificationSettingsScreenPreview_Actions_Student_Interactive() {
    var lastAction by remember { mutableStateOf("None") }

    AppTheme(theme = ThemeType.STUDENT) {
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppSpacing.screenPadding,
                            vertical = AppSpacing.sm,
                        ),
                style = typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(modifier = Modifier.weight(1f)) {
                NotificationSettingsScreen(
                    uiState = MyPageSettingsBoundaryFixture.notificationsUpdateFailed,
                    onAction = { lastAction = it.previewLabel() },
                )
            }
        }
    }
}
