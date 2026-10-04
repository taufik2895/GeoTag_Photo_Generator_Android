package com.geotagphotogenerator

import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapProperties
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

internal enum class MapDisplayType {
    NORMAL,
    SATELLITE,
    TERRAIN,
    HYBRID,
}

internal enum class AddressStatus {
    IDLE,
    LOADING,
    RESOLVED,
    UNAVAILABLE,
    NOT_FOUND,
    ERROR,
    TIMEOUT,
}

internal data class MapCardState(
    val selectedCoordinate: MapCoordinate?,
    val deviceLocation: MapCoordinate?,
    val mapDisplayType: MapDisplayType,
    val addressText: String?,
    val addressStatus: AddressStatus,
    val isLocatingDevice: Boolean,
    val locationMessage: String?,
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
    mapState: MapCardState,
    onCoordinateSelected: (MapCoordinate) -> Unit = {},
    onMapDisplayTypeChanged: (MapDisplayType) -> Unit,
    onUseDeviceLocation: () -> Unit,
) {
    val usesGoogleMaps = BuildConfig.MAPS_API_KEY_CONFIGURED
    val selectedCoordinate = mapState.selectedCoordinate
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (BuildConfig.MAPS_API_KEY_CONFIGURED) "Google Maps" else "Development map",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapDisplayType.values().forEach { displayType ->
                FilterChip(
                    selected = mapState.mapDisplayType == displayType,
                    enabled = usesGoogleMaps || displayType == MapDisplayType.NORMAL,
                    onClick = { onMapDisplayTypeChanged(displayType) },
                    label = { Text(displayType.name.lowercase().replaceFirstChar(Char::uppercase)) },
                )
            }
        }

        if (!usesGoogleMaps) {
            Text(
                text = "Only Normal map appearance is available in the development map.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (usesGoogleMaps) {
            ProductionGoogleMap(
                modifier = Modifier.fillMaxWidth().height(mapHeight),
                selectedCoordinate = selectedCoordinate,
                deviceLocation = mapState.deviceLocation,
                mapDisplayType = mapState.mapDisplayType,
                onCoordinateSelected = onCoordinateSelected,
            )
        } else {
            OsmdroidDevelopmentMap(
                modifier = Modifier.fillMaxWidth().height(mapHeight),
                selectedCoordinate = selectedCoordinate,
                deviceLocation = mapState.deviceLocation,
                onCoordinateSelected = onCoordinateSelected,
            )
        }

        TextButton(
            onClick = onUseDeviceLocation,
            enabled = !mapState.isLocatingDevice,
        ) {
            Text(
                if (mapState.isLocatingDevice) "Finding device location…"
                else "Use device location",
            )
        }

        mapState.locationMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                when (mapState.addressStatus) {
                    AddressStatus.LOADING -> Text(
                        text = "Resolving address…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AddressStatus.RESOLVED -> mapState.addressText?.let { address ->
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    AddressStatus.UNAVAILABLE -> Text(
                        text = "Address unavailable. The selected coordinates remain valid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AddressStatus.NOT_FOUND -> Text(
                        text = "No address found. The selected coordinates remain valid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AddressStatus.ERROR -> Text(
                        text = "Address lookup failed. The selected coordinates remain valid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    AddressStatus.TIMEOUT -> Text(
                        text = "Address lookup timed out. The selected coordinates remain valid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    AddressStatus.IDLE -> Unit
                }
            }
        }
    }
}

@Composable
private fun ProductionGoogleMap(
    modifier: Modifier,
    selectedCoordinate: MapCoordinate?,
    deviceLocation: MapCoordinate?,
    mapDisplayType: MapDisplayType,
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

    LaunchedEffect(deviceLocation, selectedCoordinate) {
        if (selectedCoordinate == null) {
            val initialCenter = deviceLocation ?: defaultMapCoordinate
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                initialCenter.toLatLng(),
                defaultMapZoom.toFloat(),
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = mapUiSettings,
        properties = MapProperties(mapType = mapDisplayType.googleMapType),
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
    deviceLocation: MapCoordinate?,
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
                deviceLocation?.toGeoPoint() ?: defaultMapCoordinate.toGeoPoint(),
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

    LaunchedEffect(deviceLocation, selectedCoordinate) {
        if (deviceLocation != null && selectedCoordinate == null) {
            mapView.controller.setCenter(deviceLocation.toGeoPoint())
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

private val MapDisplayType.googleMapType: MapType
    get() = when (this) {
        MapDisplayType.NORMAL -> MapType.NORMAL
        MapDisplayType.SATELLITE -> MapType.SATELLITE
        MapDisplayType.TERRAIN -> MapType.TERRAIN
        MapDisplayType.HYBRID -> MapType.HYBRID
    }
