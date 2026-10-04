package com.geotagphotogenerator

import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker as OsmdroidMarker

internal data class MapCoordinate(
    val latitude: Double,
    val longitude: Double,
)

internal val MapCoordinateSaver = Saver<MapCoordinate?, List<Double>>(
    save = { coordinate ->
        if (coordinate == null) emptyList() else listOf(coordinate.latitude, coordinate.longitude)
    },
    restore = { values ->
        if (values.size == 2) MapCoordinate(values[0], values[1]) else null
    },
)

internal val defaultMapCoordinate = MapCoordinate(-6.2088, 106.8456)
private const val defaultMapZoom = 10.0

@Composable
internal fun GoogleMapCard(
    modifier: Modifier = Modifier,
    mapHeight: Dp = 280.dp,
    selectedCoordinate: MapCoordinate?,
    onCoordinateSelected: (MapCoordinate) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (BuildConfig.MAPS_API_KEY_CONFIGURED) "Google Maps" else "Development map",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (BuildConfig.MAPS_API_KEY_CONFIGURED) {
            ProductionGoogleMap(
                modifier = Modifier.fillMaxWidth().height(mapHeight),
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = onCoordinateSelected,
            )
        } else {
            OsmdroidDevelopmentMap(
                modifier = Modifier.fillMaxWidth().height(mapHeight),
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = onCoordinateSelected,
            )
        }

        if (selectedCoordinate == null) {
            Text(
                text = "Tap the map to choose a location.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Selected location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Latitude: ${selectedCoordinate.latitude}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Longitude: ${selectedCoordinate.longitude}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProductionGoogleMap(
    modifier: Modifier,
    selectedCoordinate: MapCoordinate?,
    onCoordinateSelected: (MapCoordinate) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            selectedCoordinate?.toLatLng() ?: defaultMapCoordinate.toLatLng(),
            defaultMapZoom.toFloat(),
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(zoomControlsEnabled = true)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        onMapClick = { clickedLocation ->
            onCoordinateSelected(clickedLocation.toMapCoordinate())
        },
    ) {
        selectedCoordinate?.let { coordinate ->
            Marker(
                state = rememberUpdatedMarkerState(position = coordinate.toLatLng()),
                title = "Selected location",
            )
        }
    }
}

@Composable
private fun OsmdroidDevelopmentMap(
    modifier: Modifier,
    selectedCoordinate: MapCoordinate?,
    onCoordinateSelected: (MapCoordinate) -> Unit,
) {
    val context = LocalContext.current
    val currentOnCoordinateSelected = rememberUpdatedState(onCoordinateSelected)

    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(defaultMapZoom)
            controller.setCenter(
                selectedCoordinate?.toGeoPoint() ?: defaultMapCoordinate.toGeoPoint(),
            )
            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
            var downX = 0f
            var downY = 0f
            var isTapCandidate = false
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        isTapCandidate = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.x - downX
                        val deltaY = event.y - downY
                        if (deltaX * deltaX + deltaY * deltaY > touchSlop * touchSlop) {
                            isTapCandidate = false
                        }
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> isTapCandidate = false
                    MotionEvent.ACTION_UP -> {
                        if (isTapCandidate) {
                            projection.fromPixels(event.x.toInt(), event.y.toInt())
                                ?.let { point ->
                                    currentOnCoordinateSelected.value(
                                        MapCoordinate(point.latitude, point.longitude),
                                    )
                                }
                        }
                        isTapCandidate = false
                    }
                    MotionEvent.ACTION_CANCEL -> isTapCandidate = false
                }
                false
            }
        }
    }
    val mapContainer = remember(mapView) {
        FrameLayout(context).apply {
            clipChildren = true
            clipToPadding = true
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }
    }
    val selectedMarker = remember(mapView) {
        OsmdroidMarker(mapView).apply {
            setAnchor(OsmdroidMarker.ANCHOR_CENTER, OsmdroidMarker.ANCHOR_BOTTOM)
            title = "Selected location"
            setOnMarkerClickListener { _, _ -> true }
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapContainer },
        modifier = modifier,
        update = {
            val markerVisible = mapView.overlays.contains(selectedMarker)
            if (selectedCoordinate == null && markerVisible) {
                mapView.overlays.remove(selectedMarker)
                mapView.invalidate()
            } else {
                selectedCoordinate?.let { coordinate ->
                    val positionChanged =
                        selectedMarker.position.latitude != coordinate.latitude ||
                            selectedMarker.position.longitude != coordinate.longitude
                    selectedMarker.position = coordinate.toGeoPoint()
                    if (!markerVisible) {
                        mapView.overlays.add(selectedMarker)
                        mapView.invalidate()
                    } else if (positionChanged) {
                        mapView.invalidate()
                    }
                }
            }
        },
    )
}

private fun MapCoordinate.toLatLng() = LatLng(latitude, longitude)

private fun MapCoordinate.toGeoPoint() = GeoPoint(latitude, longitude)

private fun LatLng.toMapCoordinate() = MapCoordinate(latitude, longitude)

private fun GeoPoint.toMapCoordinate() = MapCoordinate(latitude, longitude)
