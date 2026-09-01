package com.pheeeew.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pheeeew.core.designsystem.DesignSystemColors
import com.pheeeew.di.LocationDependencies
import com.pheeeew.feature.map.map.BreathMap

@Composable
fun MapScreen(
    locationDependencies: LocationDependencies?,
    viewModel: MapViewModel = viewModel { MapViewModel(locationDependencies) },
) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            BreathMap(
                state = uiState.mapState,
                cameraCommand = uiState.cameraCommand,
                onSighClick = {},
                onMapError = {},
                modifier = Modifier.fillMaxSize(),
            )

            Column(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MapControlButton(
                    icon = MapControlIcon.ZoomIn,
                    contentDescription = "지도 확대",
                    onClick = viewModel::onZoomInClick,
                )
                MapControlButton(
                    icon = MapControlIcon.ZoomOut,
                    contentDescription = "지도 축소",
                    onClick = viewModel::onZoomOutClick,
                )
            }

            MapControlButton(
                icon = if (uiState.isRequestingLocation) MapControlIcon.Loading else MapControlIcon.CurrentLocation,
                contentDescription = "현재 위치로 이동",
                enabled = uiState.isLocationAvailable && !uiState.isRequestingLocation,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 32.dp),
                onClick = viewModel::onMyLocationClick,
            )
        }
    }
}

@Composable
private fun MapControlButton(
    icon: MapControlIcon,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .size(48.dp)
                .border(
                    width = 1.dp,
                    color = Color(hexColor(DesignSystemColors.MAP_CONTROL_BORDER_HEX)),
                    shape = CircleShape,
                ).semantics { this.contentDescription = contentDescription },
        colors =
            IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_BACKGROUND_HEX)).copy(alpha = 0.94f),
                contentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX)),
                disabledContainerColor =
                    Color(hexColor(DesignSystemColors.MAP_CONTROL_BACKGROUND_HEX)).copy(alpha = 0.72f),
                disabledContentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX)).copy(alpha = 0.36f),
            ),
    ) {
        when (icon) {
            MapControlIcon.ZoomIn,
            MapControlIcon.ZoomOut,
            -> ZoomControlGlyph(showVerticalStroke = icon == MapControlIcon.ZoomIn)

            MapControlIcon.CurrentLocation -> Text("⌖", fontSize = 22.sp)

            MapControlIcon.Loading -> Text("…", fontSize = 22.sp)
        }
    }
}

@Composable
private fun ZoomControlGlyph(showVerticalStroke: Boolean) {
    val contentColor = Color(hexColor(DesignSystemColors.MAP_CONTROL_CONTENT_HEX))
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val inset = 3.dp.toPx()
        drawLine(
            color = contentColor,
            start = Offset(inset, center.y),
            end = Offset(size.width - inset, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        if (showVerticalStroke) {
            drawLine(
                color = contentColor,
                start = Offset(center.x, inset),
                end = Offset(center.x, size.height - inset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

private enum class MapControlIcon {
    ZoomIn,
    ZoomOut,
    CurrentLocation,
    Loading,
}

private fun hexColor(hex: String): Long = 0xFF000000 or hex.removePrefix("#").toLong(16)

@Preview
@Composable
private fun MapScreenPreview() {
    MapScreen(locationDependencies = null)
}
