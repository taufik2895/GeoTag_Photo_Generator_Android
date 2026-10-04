package com.geotagphotogenerator

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal data class AddressResolution(
    val address: String?,
    val geocoderAvailable: Boolean,
)

internal suspend fun resolveAddress(
    context: Context,
    coordinate: MapCoordinate,
): AddressResolution = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) {
        return@withContext AddressResolution(address = null, geocoderAvailable = false)
    }

    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.awaitAddresses(coordinate)
    } else {
        @Suppress("DEPRECATION")
        geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1)
    }

    val address = addresses
        ?.firstOrNull()
        ?.getAddressLine(0)
        ?.trim()
        ?.takeIf(String::isNotEmpty)
    AddressResolution(address = address, geocoderAvailable = true)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun Geocoder.awaitAddresses(
    coordinate: MapCoordinate,
): List<Address> = suspendCancellableCoroutine { continuation ->
    getFromLocation(
        coordinate.latitude,
        coordinate.longitude,
        1,
        object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (continuation.isActive) {
                    continuation.resume(addresses)
                }
            }

            override fun onError(errorMessage: String?) {
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        IOException(errorMessage ?: "Address lookup failed."),
                    )
                }
            }
        },
    )
}
