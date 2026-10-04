package com.geotagphotogenerator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

internal suspend fun findInitialDeviceLocation(context: Context): MapCoordinate? {
    if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }

    val locationManager = context.getSystemService(LocationManager::class.java) ?: return null
    val enabledProviders = locationManager.getProviders(true)
    val providers = listOf(
        LocationManager.NETWORK_PROVIDER,
        LocationManager.GPS_PROVIDER,
    ).filter(enabledProviders::contains)

    for (provider in providers) {
        val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            withTimeoutOrNull(12_000) {
                locationManager.awaitCurrentLocation(context, provider)
            }
        } else {
            locationManager.getLastKnownLocation(provider)
                ?.takeIf { location -> location.time >= System.currentTimeMillis() - 120_000 }
        }
        if (location != null) {
            return MapCoordinate(location.latitude, location.longitude)
        }
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.R)
private suspend fun LocationManager.awaitCurrentLocation(
    context: Context,
    provider: String,
): Location? = suspendCancellableCoroutine { continuation ->
    if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        continuation.resume(null)
        return@suspendCancellableCoroutine
    }

    val cancellationSignal = CancellationSignal()
    continuation.invokeOnCancellation { cancellationSignal.cancel() }
    getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
        if (continuation.isActive) {
            continuation.resume(location)
        }
    }
}
