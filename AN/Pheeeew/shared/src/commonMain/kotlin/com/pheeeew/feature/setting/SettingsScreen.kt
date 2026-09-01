package com.pheeeew.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppTheme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onThemeSettingClick: () -> Unit,
    onLocationPermissionClick: () -> Unit,
    onContactClick: () -> Unit,
    onOpenSourceLicenseClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onLocationPolicyClick: () -> Unit,
    onCreditsClick: () -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
    ) {
        SettingsTopBar(onBackClick = onBackClick)

        Text(
            text = "설정",
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "앱 설정") {
            SettingsMenuItem(title = "테마 설정", onClick = onThemeSettingClick)
            SettingsMenuItem(title = "위치 권한 설정", onClick = onLocationPermissionClick)
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "이용안내") {
            SettingsMenuItem(title = "앱 버전", trailingText = appVersion)
            SettingsMenuItem(title = "문의하기", onClick = onContactClick)
            SettingsMenuItem(title = "오픈소스 라이선스", onClick = onOpenSourceLicenseClick)
            SettingsMenuItem(title = "개인정보 처리방침", onClick = onPrivacyPolicyClick)
            //SettingsMenuItem(title = "위치정보 처리방침", onClick = onLocationPolicyClick)
        }

        Spacer(modifier = Modifier.height(32.dp))

        SettingsSection(title = "기타") {
            SettingsMenuItem(title = "Pheeeew를 만든 사람들", onClick = onCreditsClick)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            onBackClick = {},
            onThemeSettingClick = {},
            onLocationPermissionClick = {},
            onContactClick = {},
            onOpenSourceLicenseClick = {},
            onPrivacyPolicyClick = {},
            onLocationPolicyClick = {},
            onCreditsClick = {},
            appVersion = "1.0.0",
        )
    }
}
