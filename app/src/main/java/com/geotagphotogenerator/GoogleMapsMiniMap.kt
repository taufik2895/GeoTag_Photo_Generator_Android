package com.geotagphotogenerator

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap as GoogleMapSdk
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap as GoogleMapComposable
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

private const val satelliteMiniMapZoom = 16f

private sealed interface MiniMapSnapshotState {
    data object Loading : MiniMapSnapshotState
    data class Ready(
        val coordinate: MapCoordinate,
        val bitmap: Bitmap,
    ) : MiniMapSnapshotState
    data object Failed : MiniMapSnapshotState
}

@Composable
internal fun GoogleMapsMiniMapCard(
    selectedCoordinate: MapCoordinate?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Satellite mini-map",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Always satellite; independent of the interactive map style.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when {
                selectedCoordinate == null -> Text(
                    text = "Select a location on the map to prepare its satellite mini-map.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                !BuildConfig.MAPS_API_KEY_CONFIGURED -> Text(
                    text = "Google Maps satellite snapshot is blocked until MAPS_API_KEY is " +
                        "configured. The development map cannot provide this production snapshot.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                else -> GoogleMapsSatelliteSnapshot(
                    coordinate = selectedCoordinate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
@OptIn(MapsComposeExperimentalApi::class)
private fun GoogleMapsSatelliteSnapshot(
    coordinate: MapCoordinate,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            coordinate.toLatLng(),
            satelliteMiniMapZoom,
        )
    }
    val markerState = rememberUpdatedMarkerState(position = coordinate.toLatLng())
    var mapLoaded by remember { mutableStateOf(false) }
    var googleMap by remember { mutableStateOf<GoogleMapSdk?>(null) }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    var snapshotState by remember(coordinate) {
        mutableStateOf<MiniMapSnapshotState>(MiniMapSnapshotState.Loading)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .onSizeChanged { mapSize = it },
    ) {
        GoogleMapComposable(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapType = MapType.SATELLITE),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                indoorLevelPickerEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = false,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
            ),
            onMapLoaded = { mapLoaded = true },
        ) {
            Marker(
                state = markerState,
                title = "Selected location",
                snippet = "${coordinate.latitude}, ${coordinate.longitude}",
            )
        }

        (snapshotState as? MiniMapSnapshotState.Ready)
            ?.takeIf { it.coordinate == coordinate }
            ?.let { snapshot ->
                Image(
                    bitmap = snapshot.bitmap.asImageBitmap(),
                    contentDescription =
                        "Satellite mini-map with a marker at the selected location",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }
    }

    MapEffect(coordinate) { map ->
        googleMap = map
        map.mapType = GoogleMapSdk.MAP_TYPE_SATELLITE
    }

    LaunchedEffect(coordinate, googleMap, mapLoaded, mapSize) {
        val map = googleMap ?: return@LaunchedEffect
        if (mapSize.width <= 0 || mapSize.height <= 0) {
            return@LaunchedEffect
        }

        snapshotState = MiniMapSnapshotState.Loading
        if (!mapLoaded) {
            delay(20_000)
            if (!mapLoaded) {
                snapshotState = MiniMapSnapshotState.Failed
            }
            return@LaunchedEffect
        }

        val target = coordinate.toLatLng()
        val bitmapReference = AtomicReference<Bitmap?>()
        try {
            map.mapType = GoogleMapSdk.MAP_TYPE_SATELLITE
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(target, satelliteMiniMapZoom),
                durationMs = 300,
            )
            currentCoroutineContext().ensureActive()

            val bitmap = withTimeout(15_000) {
                map.awaitSnapshot(bitmapReference)
            }
            if (bitmap == null) {
                snapshotState = MiniMapSnapshotState.Failed
                return@LaunchedEffect
            }

            currentCoroutineContext().ensureActive()
            snapshotState = MiniMapSnapshotState.Ready(coordinate, bitmap)
            bitmapReference.compareAndSet(bitmap, null)
        } catch (exception: TimeoutCancellationException) {
            snapshotState = MiniMapSnapshotState.Failed
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: RuntimeException) {
            snapshotState = MiniMapSnapshotState.Failed
        } finally {
            bitmapReference.getAndSet(null)?.recycle()
        }
    }

    DisposableEffect(snapshotState) {
        val bitmap = (snapshotState as? MiniMapSnapshotState.Ready)?.bitmap
        onDispose { bitmap?.recycle() }
    }

    DisposableEffect(Unit) {
        onDispose {
            googleMap = null
            mapLoaded = false
        }
    }

    when (snapshotState) {
        MiniMapSnapshotState.Loading -> Text(
            text = "Preparing satellite snapshot…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is MiniMapSnapshotState.Ready -> Unit
        MiniMapSnapshotState.Failed -> Text(
            text = "Google Maps could not create the satellite snapshot. Try selecting the " +
                "location again.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private fun MapCoordinate.toLatLng() = LatLng(latitude, longitude)

private suspend fun GoogleMapSdk.awaitSnapshot(
    bitmapReference: AtomicReference<Bitmap?>,
): Bitmap? =
    suspendCancellableCoroutine { continuation ->
        try {
            snapshot { bitmap ->
                if (continuation.isActive) {
                    if (bitmap != null) {
                        bitmapReference.set(bitmap)
                    }
                    continuation.resume(bitmap)
                } else {
                    bitmap?.recycle()
                }
            }
        } catch (exception: RuntimeException) {
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
    }
