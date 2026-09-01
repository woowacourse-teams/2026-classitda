package com.pheeeew.feature.map.map

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pheeeew.domain.model.location.CurrentLocation
import com.pheeeew.feature.map.MapRenderState
import com.pheeeew.feature.map.SighMarker
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory.iconOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.SymbolLayer
import java.util.ArrayDeque

@Composable
internal actual fun NativeBreathMap(
    state: MapRenderState,
    cameraCommand: MapCameraCommand?,
    onSighClick: (String) -> Unit,
    onMapError: (MapError) -> Unit,
    onProjectionChanged: (MapProjectionSnapshot) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnSighClick by rememberUpdatedState(onSighClick)
    val currentOnMapError by rememberUpdatedState(onMapError)
    val currentOnProjectionChanged by rememberUpdatedState(onProjectionChanged)

    val hostResult =
        remember(context, lifecycleOwner) {
            runCatching {
                MapLibre.getInstance(context.applicationContext)
                AndroidBreathMapHost(
                    mapView = MapView(context).apply { onCreate(null) },
                    onSighClick = { id -> currentOnSighClick(id) },
                    onMapError = { error -> currentOnMapError(error) },
                    onProjectionChanged = { snapshot -> currentOnProjectionChanged(snapshot) },
                )
            }
        }
    val host = hostResult.getOrNull()

    if (host == null) {
        LaunchedEffect(hostResult.exceptionOrNull()) {
            currentOnMapError(MapError.RendererUnavailable)
        }
        Box(modifier)
        return
    }

    DisposableEffect(host, lifecycleOwner) {
        val lifecycleDelegate = AndroidMapLifecycleDelegate(host.mapView)
        lifecycleDelegate.attach(lifecycleOwner)

        onDispose {
            host.release()
            lifecycleDelegate.dispose(lifecycleOwner)
        }
    }

    AndroidView(
        factory = { host.mapView },
        modifier = modifier,
        update = {
            host.render(state, cameraCommand)
        },
    )
}

private class AndroidBreathMapHost(
    val mapView: MapView,
    private val onSighClick: (String) -> Unit,
    private val onMapError: (MapError) -> Unit,
    private val onProjectionChanged: (MapProjectionSnapshot) -> Unit,
) {
    private val camera = AndroidMapCamera()
    private var map: MapLibreMap? = null
    private var style: Style? = null
    private var latestState: MapRenderState? = null
    private val pendingCameraCommands = ArrayDeque<MapCameraCommand>()
    private var lastReceivedCameraCommandId: Long? = null
    private var renderedMarkers: List<SighMarker>? = null
    private var renderedCurrentLocation: CurrentLocation? = null
    private var hasRenderedCurrentLocation = false
    private var hasReportedStyleFailure = false
    private var released = false
    private var sighPulseAnimator: ValueAnimator? = null
    private var projectionRevision = 0L
    private var cameraIdle = true
    private var lastRenderedFocusId: String? = null
    private var lastPublishedPoints: Map<String, MapScreenPoint>? = null
    private var lastPublishedCameraIdle: Boolean? = null

    private val mapLoadFailureListener =
        MapView.OnDidFailLoadingMapListener {
            if (!released && !hasReportedStyleFailure) {
                hasReportedStyleFailure = true
                onMapError(MapError.StyleLoadFailed)
            }
        }

    private val mapClickListener =
        MapLibreMap.OnMapClickListener { latLng ->
            val currentMap = map ?: return@OnMapClickListener false
            val markerId =
                currentMap
                    .queryRenderedFeatures(
                        currentMap.projection.toScreenLocation(latLng),
                        MapDarkStyle.SIGH_LAYER_ID,
                    ).firstOrNull()
                    ?.getStringProperty(AndroidMapSources.MARKER_ID_PROPERTY)

            if (markerId.isNullOrBlank()) {
                false
            } else {
                onSighClick(markerId)
                true
            }
        }

    private val cameraMoveStartedListener =
        MapLibreMap.OnCameraMoveStartedListener { reason ->
            camera.onCameraMoveStarted(reason)
        }

    private val cameraMoveListener =
        MapLibreMap.OnCameraMoveListener {
            cameraIdle = false
            publishProjection(cameraIdle = false)
        }

    private val cameraIdleListener =
        MapLibreMap.OnCameraIdleListener {
            cameraIdle = true
            publishProjection(cameraIdle = true)
        }

    init {
        mapView.addOnDidFailLoadingMapListener(mapLoadFailureListener)
        mapView.getMapAsync { readyMap ->
            if (released) return@getMapAsync
            map = readyMap
            readyMap.setMinZoomPreference(MapDarkStyle.MINIMUM_ZOOM)
            readyMap.setMaxZoomPreference(MapDarkStyle.MAXIMUM_ZOOM)
            readyMap.uiSettings.apply {
                isScrollGesturesEnabled = true
                isZoomGesturesEnabled = true
                isRotateGesturesEnabled = true
                isTiltGesturesEnabled = true
                isLogoEnabled = true
                isAttributionEnabled = true
            }
            readyMap.addOnMapClickListener(mapClickListener)
            readyMap.addOnCameraMoveStartedListener(cameraMoveStartedListener)
            readyMap.addOnCameraMoveListener(cameraMoveListener)
            readyMap.addOnCameraIdleListener(cameraIdleListener)
            readyMap.setStyle(Style.Builder().fromUri(MapDarkStyle.STYLE_URL)) { loadedStyle ->
                if (released) return@setStyle
                hasReportedStyleFailure = false

                // A base-style schema change must not make the entire map disappear.
                runCatching { AndroidMapStyle.apply(loadedStyle) }

                val sourcesInstalled =
                    runCatching { AndroidMapSources.install(loadedStyle) }.isSuccess
                if (!sourcesInstalled) {
                    onMapError(MapError.RendererUnavailable)
                    return@setStyle
                }

                style = loadedStyle
                startSighPulse(loadedStyle)
                renderLatestState()
            }
        }
    }

    fun render(
        state: MapRenderState,
        cameraCommand: MapCameraCommand?,
    ) {
        latestState = state
        if (cameraCommand != null && cameraCommand.id != lastReceivedCameraCommandId) {
            lastReceivedCameraCommandId = cameraCommand.id
            pendingCameraCommands.addLast(cameraCommand)
        }
        renderLatestState()
    }

    fun release() {
        if (released) return
        released = true
        mapView.removeOnDidFailLoadingMapListener(mapLoadFailureListener)
        map?.removeOnMapClickListener(mapClickListener)
        map?.removeOnCameraMoveStartedListener(cameraMoveStartedListener)
        map?.removeOnCameraMoveListener(cameraMoveListener)
        map?.removeOnCameraIdleListener(cameraIdleListener)
        sighPulseAnimator?.cancel()
        sighPulseAnimator = null
        map = null
        style = null
        latestState = null
        pendingCameraCommands.clear()
    }

    private fun startSighPulse(style: Style) {
        val layer = style.getLayerAs<SymbolLayer>(MapDarkStyle.SIGH_LAYER_ID) ?: return
        sighPulseAnimator?.cancel()
        sighPulseAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 1_600L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { animator ->
                    if (released) return@addUpdateListener
                    val pulse = animator.animatedValue as Float
                    layer.setProperties(
                        iconSize(0.48f + (pulse * 0.20f)),
                        iconOpacity(0.72f + (pulse * 0.28f)),
                    )
                }
                start()
            }
    }

    private fun renderLatestState() {
        if (released) return
        val currentMap = map ?: return
        val currentStyle = style ?: return
        val state = latestState ?: return

        val markers = MapRenderRules.renderableSighMarkers(state.sighMarkers)
        if (markers != renderedMarkers) {
            AndroidMapSources.updateSighs(currentStyle, markers)
            renderedMarkers = markers
        }

        val currentLocation = MapRenderRules.currentLocation(state)
        if (!hasRenderedCurrentLocation || currentLocation != renderedCurrentLocation) {
            AndroidMapSources.updateCurrentLocation(currentStyle, currentLocation)
            renderedCurrentLocation = currentLocation
            hasRenderedCurrentLocation = true
        }

        if (state.focusRequest?.id != null && state.focusRequest.id != lastRenderedFocusId) {
            cameraIdle = false
            lastRenderedFocusId = state.focusRequest.id
        }
        camera.render(currentMap, state, cameraCommand = null)
        while (pendingCameraCommands.isNotEmpty()) {
            cameraIdle = false
            camera.render(currentMap, state, pendingCameraCommands.removeFirst())
        }
        publishProjection(cameraIdle = cameraIdle)
    }

    private fun publishProjection(cameraIdle: Boolean) {
        if (released) return
        val currentMap = map ?: return
        val state = latestState ?: return
        val targets =
            buildList {
                state.sighMarkers.forEach { add(MapPointTarget(it.id, it.latitude, it.longitude)) }
                state.focusRequest?.let { focus ->
                    if (none { it.id == focus.id }) add(MapPointTarget(focus.id, focus.latitude, focus.longitude))
                }
            }
        val points =
            targets.associate { target ->
                val point = currentMap.projection.toScreenLocation(LatLng(target.latitude, target.longitude))
                target.id to MapScreenPoint(target.id, point.x, point.y)
            }
        if (points == lastPublishedPoints && cameraIdle == lastPublishedCameraIdle) return
        lastPublishedPoints = points
        lastPublishedCameraIdle = cameraIdle
        projectionRevision += 1
        onProjectionChanged(MapProjectionSnapshot(projectionRevision, points, cameraIdle))
    }

    private data class MapPointTarget(
        val id: String,
        val latitude: Double,
        val longitude: Double,
    )
}

private class AndroidMapLifecycleDelegate(
    private val mapView: MapView,
) {
    private var started = false
    private var resumed = false
    private var destroyed = false

    private val observer =
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_RESUME -> resume()
                Lifecycle.Event.ON_PAUSE -> pause()
                Lifecycle.Event.ON_STOP -> stop()
                Lifecycle.Event.ON_DESTROY -> destroy()
                else -> Unit
            }
        }

    fun attach(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) resume()
    }

    fun dispose(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(observer)
        destroy()
    }

    private fun start() {
        if (destroyed || started) return
        mapView.onStart()
        started = true
    }

    private fun resume() {
        if (destroyed || resumed) return
        if (!started) start()
        mapView.onResume()
        resumed = true
    }

    private fun pause() {
        if (!resumed) return
        mapView.onPause()
        resumed = false
    }

    private fun stop() {
        pause()
        if (!started) return
        mapView.onStop()
        started = false
    }

    private fun destroy() {
        if (destroyed) return
        stop()
        mapView.onDestroy()
        destroyed = true
    }
}
