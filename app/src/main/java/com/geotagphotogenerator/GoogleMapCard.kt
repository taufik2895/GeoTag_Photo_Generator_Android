package com.geotagphotogenerator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_MAP_CENTER = LatLng(-6.2088, 106.8456)
private const val DEFAULT_MAP_ZOOM = 10f

@Composable
internal fun GoogleMapCard(
    modifier: Modifier = Modifier,
    mapHeight: Dp = 280.dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Google Maps",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (BuildConfig.MAPS_API_KEY_CONFIGURED) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(DEFAULT_MAP_CENTER, DEFAULT_MAP_ZOOM)
            }
            val mapUiSettings = remember {
                MapUiSettings(zoomControlsEnabled = true)
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
            )
            Text(
                text = "Pan and zoom to explore. Coordinate selection is a separate next step.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Google Maps is not configured",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Add a restricted Maps API key as MAPS_API_KEY in local.properties to load the real map.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
