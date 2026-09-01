package com.pheeeew.feature.map.overlay

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.feature.map.SighReleaseState
import pheeeew.shared.generated.resources.Res
import pheeeew.shared.generated.resources.ic_my_location
import pheeeew.shared.generated.resources.ic_refresh
import pheeeew.shared.generated.resources.ic_settings

@Composable
fun MapOverlay(
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    breathControl: @Composable () -> Unit,
    onZoomInClick: () -> Unit,
    onZoomOutClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
    curLocation: String? = null,
    errorMessage: String? = null,
    sighReleaseState: SighReleaseState = SighReleaseState.Idle,
    onRetrySigh: () -> Unit = {},
    onCancelSigh: () -> Unit = {},
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
                onRetry = (sighReleaseState as? SighReleaseState.Error)?.takeIf { it.canRetry }?.let { onRetrySigh },
                onNew = (sighReleaseState as? SighReleaseState.Error)?.let { onCancelSigh },
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

        breathControl()
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
                breathControl = {},
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
                breathControl = {},
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
                breathControl = {},
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
                breathControl = {},
                onZoomInClick = {},
                onZoomOutClick = {},
                onMyLocationClick = {},
                errorMessage = "GPS 수신이 원활하지 않습니다.",
            )
        }
    }
}
