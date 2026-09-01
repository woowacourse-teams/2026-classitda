package com.pheeeew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.core.navigation.Screen
import com.pheeeew.feature.map.MapScreen
import com.pheeeew.feature.setting.SettingsScreen
import com.pheeeew.feature.splash.SplashScreen

@Composable
fun App(appVersion: String) {
    AppTheme {
        var screen by remember { mutableStateOf(Screen.Splash) }
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            when (screen) {
                Screen.Splash -> {
                    SplashScreen(
                        onFinished = { screen = Screen.Map },
                    )
                }

                Screen.Map -> {
                    MapScreen(
                        onSettingsClick = { screen = Screen.Settings },
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        onBackClick = { screen = Screen.Map },
                        onThemeSettingClick = {},
                        onLocationPermissionClick = {},
                        onContactClick = {},
                        onOpenSourceLicenseClick = {},
                        onPrivacyPolicyClick = {},
                        onLocationPolicyClick = {},
                        onCreditsClick = {},
                        appVersion = appVersion,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    App(appVersion = "1.0.0")
}

//TODO: 충돌 해결을 위한 임시 주석 
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.FilledIconButton
//import androidx.compose.material3.IconButtonDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.semantics.contentDescription
//import androidx.compose.ui.semantics.semantics
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.pheeeew.core.designsystem.DesignSystemColors
//import com.pheeeew.core.permission.LocationPermissionStatus
//import com.pheeeew.di.LocationDependencies
//import com.pheeeew.domain.model.location.LocationState
//import com.pheeeew.feature.map.MapPoint
//import com.pheeeew.feature.map.MapUiState
//import com.pheeeew.feature.map.SighMarker
//import com.pheeeew.feature.map.map.BreathMap
//import com.pheeeew.feature.map.map.MapCameraCommand
//import com.pheeeew.feature.map.map.MapDarkStyle
//import kotlinx.coroutines.CancellationException
//import kotlinx.coroutines.launch
//
//@Composable
//@Preview
//fun App() {
//    AppContent(
//        locationDependencies = null,
//        locationState = LocationState.Loading,
//    )
//}
//
//@Composable
//fun App(locationDependencies: LocationDependencies) {
//    val locationState by locationDependencies.repository.locationState.collectAsState()
//    AppContent(
//        locationDependencies = locationDependencies,
//        locationState = locationState,
//    )
//}
//
//@Composable
//private fun AppContent(
//    locationDependencies: LocationDependencies?,
//    locationState: LocationState,
//) {
//    val coroutineScope = rememberCoroutineScope()
//    var cameraCommand by remember { mutableStateOf<MapCameraCommand?>(null) }
//    var nextCameraCommandId by remember { mutableStateOf(0L) }
//    var isRequestingLocation by remember { mutableStateOf(false) }
//
//    fun sendCameraCommand(create: (Long) -> MapCameraCommand) {
//        nextCameraCommandId += 1L
//        cameraCommand = create(nextCameraCommandId)
//    }
//
//    val currentLocation = (locationState as? LocationState.Available)?.location
//    val mapState =
//        MapUiState(
//            currentLocation = currentLocation,
//            locationState = locationState,
//            fallbackCenter = DEFAULT_MAP_POINT,
//            // 지도 핀 렌더링 확인을 위한 임시 샘플 1개입니다.
//            sighMarkers = PREVIEW_SIGH_MARKERS,
//            focusRequest = null,
//        )
//
//    MaterialTheme {
//        Box(modifier = Modifier.fillMaxSize()) {
//            BreathMap(
//                state = mapState,
//                cameraCommand = cameraCommand,
//                onSighClick = {},
//                onMapError = {},
//                modifier = Modifier.fillMaxSize(),
//            )
//
//            Column(
//                modifier =
//                    Modifier
//                        .align(Alignment.CenterEnd)
//                        .padding(end = 16.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                MapControlButton(
//                    icon = MapControlIcon.ZoomIn,
//                    contentDescription = "지도 확대",
//                    onClick = {
//                        sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = 1.0) }
//                    },
//                )
//                MapControlButton(
//                    icon = MapControlIcon.ZoomOut,
//                    contentDescription = "지도 축소",
//                    onClick = {
//                        sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = -1.0) }
//                    },
//                )
//            }
//
//            MapControlButton(
//                icon = if (isRequestingLocation) MapControlIcon.Loading else MapControlIcon.CurrentLocation,
//                contentDescription = "현재 위치로 이동",
//                enabled = locationDependencies != null && !isRequestingLocation,
//                modifier =
//                    Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(end = 16.dp, bottom = 32.dp),
//                onClick = {
//                    val dependencies = locationDependencies ?: return@MapControlButton
//                    coroutineScope.launch {
//                        isRequestingLocation = true
//                        try {
//                            val currentStatus = dependencies.permissionController.currentStatus()
//                            if (currentStatus != LocationPermissionStatus.Granted) {
//                                dependencies.permissionController.requestPermission()
//                            }
//                            dependencies.repository.refreshCurrentLocation()
//                            if (dependencies.repository.locationState.value is LocationState.Available) {
//                                sendCameraCommand { id ->
//                                    MapCameraCommand.MoveToCurrentLocation(
//                                        id = id,
//                                        zoom = MapDarkStyle.FOCUS_ZOOM,
//                                    )
//                                }
//                            }
//                        } catch (cancellation: CancellationException) {
//                            throw cancellation
//                        } catch (_: Exception) {
//                            // 위치나 권한 값은 로그에 남기지 않습니다. 지도는 현재 카메라를 유지합니다.
//                        } finally {
//                            isRequestingLocation = false
//                        }
//                    }
//                },
//            )
//        }
//    }
//}
//
//@Composable
//private fun MapControlButton(
//    icon: MapControlIcon,
//    contentDescription: String,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    onClick: () -> Unit,
//) {
//    FilledIconButton(
//        onClick = onClick,
//        enabled = enabled,
//        modifier =
//            modifier
//                .size(48.dp)
//                .border(
//                    width = 1.dp,
//                    color = Color(hexColor(DesignSystemColors.MAP_CONTROL_BORDER_HEX)),
//                    shape = CircleShape,
//                ).semantics { this.contentDescription = contentDescription },
//        colors =
//            IconButtonDefaults.filledIconButtonColors(
//                containerColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_BACKGROUND_HEX)).copy(alpha = 0.94f),
//                contentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX)),
//                disabledContainerColor =
//                    Color(hexColor(DesignSystemColors.MAP_CONTROL_BACKGROUND_HEX)).copy(alpha = 0.72f),
//                disabledContentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX)).copy(alpha = 0.36f),
//            ),
//    ) {
//        when (icon) {
//            MapControlIcon.ZoomIn,
//            MapControlIcon.ZoomOut,
//                -> ZoomControlGlyph(showVerticalStroke = icon == MapControlIcon.ZoomIn)
//
//            MapControlIcon.CurrentLocation -> Text("⌖", fontSize = 22.sp)
//
//            MapControlIcon.Loading -> Text("…", fontSize = 22.sp)
//        }
//    }
//}
//
//@Composable
//private fun ZoomControlGlyph(showVerticalStroke: Boolean) {
//    val contentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX))
//    Canvas(modifier = Modifier.size(24.dp)) {
//        val strokeWidth = 2.dp.toPx()
//        val inset = 3.dp.toPx()
//        drawLine(
//            color = contentColor,
//            start = Offset(inset, center.y),
//            end = Offset(size.width - inset, center.y),
//            strokeWidth = strokeWidth,
//            cap = StrokeCap.Round,
//        )
//        if (showVerticalStroke) {
//            drawLine(
//                color = contentColor,
//                start = Offset(center.x, inset),
//                end = Offset(center.x, size.height - inset),
//                strokeWidth = strokeWidth,
//                cap = StrokeCap.Round,
//            )
//        }
//    }
//}
//
//private enum class MapControlIcon {
//    ZoomIn,
//    ZoomOut,
//    CurrentLocation,
//    Loading,
//}
//
//private fun hexColor(hex: String): Long = 0xFF000000 or hex.removePrefix("#").toLong(16)
//
//private val DEFAULT_MAP_POINT =
//    MapPoint(
//        id = "default-location",
//        latitude = 37.5505,
//        longitude = 127.0373,
//    )
//
//private val PREVIEW_SIGH_MARKERS =
//    listOf(
//        SighMarker(
//            id = "preview-sigh",
//            latitude = DEFAULT_MAP_POINT.latitude,
//            longitude = DEFAULT_MAP_POINT.longitude,
//        ),
//    )
