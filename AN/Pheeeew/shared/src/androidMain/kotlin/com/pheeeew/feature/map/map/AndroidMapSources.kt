package com.pheeeew.feature.map.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.google.gson.JsonObject
import com.pheeeew.domain.model.location.CurrentLocation
import com.pheeeew.feature.map.SighMarker
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circlePitchScale
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.fillColor
import org.maplibre.android.style.layers.PropertyFactory.fillOpacity
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** 앱 소유 GeoJSON source와 레이어를 설치하고 상태만 교체합니다. */
internal object AndroidMapSources {
    const val MARKER_ID_PROPERTY = "sigh-id"

    private const val FEATURE_KIND_PROPERTY = "location-kind"
    private const val FEATURE_KIND_ACCURACY = "accuracy"
    private const val FEATURE_KIND_POINT = "point"
    private const val STAR_BITMAP_SIZE = 96
    private const val ACCURACY_CIRCLE_VERTEX_COUNT = 64
    private const val EARTH_RADIUS_METERS = 6_371_008.8

    fun install(style: Style) {
        installSighLayers(style)
        installCurrentLocationLayers(style)
    }

    fun updateSighs(
        style: Style,
        markers: List<SighMarker>,
    ) {
        val features =
            markers.map { marker ->
                val properties =
                    JsonObject().apply {
                        addProperty(MARKER_ID_PROPERTY, marker.id)
                    }
                Feature.fromGeometry(
                    Point.fromLngLat(marker.longitude, marker.latitude),
                    properties,
                    marker.id,
                )
            }

        style
            .getSourceAs<GeoJsonSource>(MapDarkStyle.SIGH_SOURCE_ID)
            ?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    fun updateCurrentLocation(
        style: Style,
        location: CurrentLocation?,
    ) {
        val features =
            if (location == null) {
                emptyList()
            } else {
                buildList {
                    if (location.accuracyMeters > 0f) {
                        val accuracyProperties =
                            JsonObject().apply {
                                addProperty(FEATURE_KIND_PROPERTY, FEATURE_KIND_ACCURACY)
                            }
                        add(
                            Feature.fromGeometry(
                                accuracyPolygon(location),
                                accuracyProperties,
                            ),
                        )
                    }

                    val pointProperties =
                        JsonObject().apply {
                            addProperty(FEATURE_KIND_PROPERTY, FEATURE_KIND_POINT)
                        }
                    add(
                        Feature.fromGeometry(
                            Point.fromLngLat(location.longitude, location.latitude),
                            pointProperties,
                        ),
                    )
                }
            }

        style
            .getSourceAs<GeoJsonSource>(MapDarkStyle.CURRENT_LOCATION_SOURCE_ID)
            ?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private fun installSighLayers(style: Style) {
        if (style.getImage(MapDarkStyle.SIGH_IMAGE_ID) == null) {
            style.addImage(MapDarkStyle.SIGH_IMAGE_ID, createSighStarBitmap())
        }
        if (style.getSource(MapDarkStyle.SIGH_SOURCE_ID) == null) {
            style.addSource(
                GeoJsonSource(
                    MapDarkStyle.SIGH_SOURCE_ID,
                    FeatureCollection.fromFeatures(emptyList()),
                ),
            )
        }
        if (style.getLayer(MapDarkStyle.SIGH_LAYER_ID) == null) {
            style.addLayer(
                SymbolLayer(MapDarkStyle.SIGH_LAYER_ID, MapDarkStyle.SIGH_SOURCE_ID)
                    .withProperties(
                        iconImage(MapDarkStyle.SIGH_IMAGE_ID),
                        iconSize(0.58f),
                        iconAnchor(Property.ICON_ANCHOR_CENTER),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                    ),
            )
        }
    }

    private fun installCurrentLocationLayers(style: Style) {
        if (style.getSource(MapDarkStyle.CURRENT_LOCATION_SOURCE_ID) == null) {
            style.addSource(
                GeoJsonSource(
                    MapDarkStyle.CURRENT_LOCATION_SOURCE_ID,
                    FeatureCollection.fromFeatures(emptyList()),
                ),
            )
        }

        if (style.getLayer(MapDarkStyle.CURRENT_LOCATION_ACCURACY_LAYER_ID) == null) {
            style.addLayer(
                FillLayer(
                    MapDarkStyle.CURRENT_LOCATION_ACCURACY_LAYER_ID,
                    MapDarkStyle.CURRENT_LOCATION_SOURCE_ID,
                ).withFilter(
                    Expression.eq(
                        Expression.get(FEATURE_KIND_PROPERTY),
                        Expression.literal(FEATURE_KIND_ACCURACY),
                    ),
                ).withProperties(
                    fillColor(Color.parseColor(MapDarkStyle.LOCATION_BLUE)),
                    fillOpacity(0.16f),
                ),
            )
        }

        if (style.getLayer(MapDarkStyle.CURRENT_LOCATION_BORDER_LAYER_ID) == null) {
            style.addLayer(
                CircleLayer(
                    MapDarkStyle.CURRENT_LOCATION_BORDER_LAYER_ID,
                    MapDarkStyle.CURRENT_LOCATION_SOURCE_ID,
                ).withFilter(
                    Expression.eq(
                        Expression.get(FEATURE_KIND_PROPERTY),
                        Expression.literal(FEATURE_KIND_POINT),
                    ),
                ).withProperties(
                    circleRadius(9f),
                    circleColor(Color.WHITE),
                    circleOpacity(1f),
                    circlePitchScale(Property.CIRCLE_PITCH_SCALE_MAP),
                ),
            )
        }

        if (style.getLayer(MapDarkStyle.CURRENT_LOCATION_LAYER_ID) == null) {
            style.addLayer(
                CircleLayer(
                    MapDarkStyle.CURRENT_LOCATION_LAYER_ID,
                    MapDarkStyle.CURRENT_LOCATION_SOURCE_ID,
                ).withFilter(
                    Expression.eq(
                        Expression.get(FEATURE_KIND_PROPERTY),
                        Expression.literal(FEATURE_KIND_POINT),
                    ),
                ).withProperties(
                    circleRadius(6f),
                    circleColor(Color.parseColor(MapDarkStyle.LOCATION_BLUE)),
                    circleOpacity(1f),
                    circlePitchScale(Property.CIRCLE_PITCH_SCALE_MAP),
                ),
            )
        }
    }

    private fun createSighStarBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(STAR_BITMAP_SIZE, STAR_BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = STAR_BITMAP_SIZE / 2f

        val glowPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader =
                    RadialGradient(
                        center,
                        center,
                        center,
                        intArrayOf(
                            Color.argb(180, 255, 184, 77),
                            Color.argb(75, 255, 184, 77),
                            Color.TRANSPARENT,
                        ),
                        floatArrayOf(0f, 0.52f, 1f),
                        Shader.TileMode.CLAMP,
                    )
            }
        canvas.drawCircle(center, center, center, glowPaint)

        canvas.drawPath(
            starPath(
                center = center,
                majorRadius = 31f,
                diagonalRadius = 24f,
                innerRadius = 15f,
            ),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(MapDarkStyle.WARM_YELLOW)
                style = Paint.Style.FILL
            },
        )
        canvas.drawPath(
            starPath(
                center = center,
                majorRadius = 20f,
                diagonalRadius = 15f,
                innerRadius = 10f,
            ),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(MapDarkStyle.IVORY)
                style = Paint.Style.FILL
            },
        )
        return bitmap
    }

    private fun starPath(
        center: Float,
        majorRadius: Float,
        diagonalRadius: Float,
        innerRadius: Float,
    ): Path =
        Path().apply {
            repeat(16) { index ->
                val radius =
                    when {
                        index % 2 != 0 -> innerRadius
                        index % 4 == 0 -> majorRadius
                        else -> diagonalRadius
                    }
                val angle = -PI / 2.0 + index * PI / 8.0
                val x = center + (cos(angle) * radius).toFloat()
                val y = center + (sin(angle) * radius).toFloat()
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }

    private fun accuracyPolygon(location: CurrentLocation): Polygon {
        val latitudeRadians = Math.toRadians(location.latitude)
        val longitudeRadians = Math.toRadians(location.longitude)
        val angularDistance = location.accuracyMeters / EARTH_RADIUS_METERS

        val ring =
            (0..ACCURACY_CIRCLE_VERTEX_COUNT).map { index ->
                val bearing = index.toDouble() / ACCURACY_CIRCLE_VERTEX_COUNT * 2.0 * PI
                val latitude =
                    asin(
                        sin(latitudeRadians) * cos(angularDistance) +
                            cos(latitudeRadians) * sin(angularDistance) * cos(bearing),
                    )
                val longitude =
                    longitudeRadians +
                        atan2(
                            sin(bearing) * sin(angularDistance) * cos(latitudeRadians),
                            cos(angularDistance) - sin(latitudeRadians) * sin(latitude),
                        )

                Point.fromLngLat(Math.toDegrees(longitude), Math.toDegrees(latitude))
            }

        return Polygon.fromLngLats(listOf(ring))
    }
}
