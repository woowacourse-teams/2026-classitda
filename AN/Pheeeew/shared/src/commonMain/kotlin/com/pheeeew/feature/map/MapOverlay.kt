package com.pheeeew.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppColors
import com.pheeeew.core.designsystem.theme.AppTheme
import pheeeew.shared.generated.resources.Res
import pheeeew.shared.generated.resources.ic_my_location
import pheeeew.shared.generated.resources.ic_refresh
import pheeeew.shared.generated.resources.ic_settings

@Composable
fun MapOverlay(
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSighLongPress: () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    curLocation: String? = null,
    errorMessage: String? = null,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OverlayIconButton(icon = Res.drawable.ic_settings, contentDescription = "설정", onClick = onSettingsClick)
            OverlayIconButton(icon = Res.drawable.ic_refresh, contentDescription = "새로고침", onClick = onRefreshClick)
        }

        if (errorMessage != null) {
            MapErrorBanner(
                message = errorMessage,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ZoomControl(onZoomInClick = onZoomInClick, onZoomOutClick = onZoomOutClick)
            OverlayIconButton(
                icon = Res.drawable.ic_my_location,
                contentDescription = "현재 위치로 이동",
                onClick = onMyLocationClick,
            )
        }

        SighButton(
            onLongPressRelease = onSighLongPress,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        Text(
            text = "꾹 눌러 한숨 던지기",
            style = AppTheme.typography.caption,
            color = AppColors.Cream100,
        )
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Preview
@Composable
private fun MapOverlayPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            MapOverlay(
                onSettingsClick = {},
                onRefreshClick = {},
                onSighLongPress = {},
                onZoomInClick = {},
                onZoomOutClick = {},
                onMyLocationClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun MapOverlayNetworkErrorPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            MapOverlay(
                onSettingsClick = {},
                onRefreshClick = {},
                onSighLongPress = {},
                onZoomInClick = {},
                onZoomOutClick = {},
                onMyLocationClick = {},
                errorMessage = "인터넷 연결 상태를 확인해주세요!",
            )
        }
    }
}

@Preview
@Composable
private fun MapOverlayPermissionErrorPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            MapOverlay(
                onSettingsClick = {},
                onRefreshClick = {},
                onSighLongPress = {},
                onZoomInClick = {},
                onZoomOutClick = {},
                onMyLocationClick = {},
                errorMessage = "설정에서 위치 권한을 '허용'으로 변경해주세요.",
            )
        }
    }
}

@Preview
@Composable
private fun MapOverlayGpsErrorPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            MapOverlay(
                onSettingsClick = {},
                onRefreshClick = {},
                onSighLongPress = {},
                onZoomInClick = {},
                onZoomOutClick = {},
                onMyLocationClick = {},
                errorMessage = "GPS 수신이 원활하지 않습니다.",
            )
        }
    }
}
