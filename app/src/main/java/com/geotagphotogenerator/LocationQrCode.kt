package com.geotagphotogenerator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val qrBitmapSize = 512

internal fun buildGoogleMapsQrPayload(coordinate: MapCoordinate): String =
    "https://maps.google.com/?q=${coordinate.latitude},${coordinate.longitude}&t=h&z=18"

internal fun createLocationQrMatrix(
    coordinate: MapCoordinate,
    size: Int = qrBitmapSize,
): BitMatrix = QRCodeWriter().encode(
    buildGoogleMapsQrPayload(coordinate),
    BarcodeFormat.QR_CODE,
    size,
    size,
)

private fun createLocationQrBitmap(payload: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        payload,
        BarcodeFormat.QR_CODE,
        qrBitmapSize,
        qrBitmapSize,
    )
    val pixels = IntArray(qrBitmapSize * qrBitmapSize)
    for (y in 0 until qrBitmapSize) {
        for (x in 0 until qrBitmapSize) {
            pixels[y * qrBitmapSize + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return createBitmap(qrBitmapSize, qrBitmapSize, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, qrBitmapSize, 0, 0, qrBitmapSize, qrBitmapSize)
    }
}

private sealed interface QrDisplayState {
    data object Empty : QrDisplayState
    data class Generating(val payload: String) : QrDisplayState
    data class Ready(val payload: String, val bitmap: Bitmap) : QrDisplayState
    data class Failed(val payload: String) : QrDisplayState
}

@Composable
internal fun LocationQrCodeCard(
    selectedCoordinate: MapCoordinate?,
    modifier: Modifier = Modifier,
) {
    val payload = selectedCoordinate?.let(::buildGoogleMapsQrPayload)
    var displayState by remember(payload) {
        mutableStateOf(
            if (payload == null) QrDisplayState.Empty else QrDisplayState.Generating(payload),
        )
    }

    LaunchedEffect(payload) {
        if (payload == null) {
            displayState = QrDisplayState.Empty
            return@LaunchedEffect
        }

        displayState = QrDisplayState.Generating(payload)
        val bitmapReference = AtomicReference<Bitmap?>()
        try {
            val bitmap = withContext(Dispatchers.Default) {
                createLocationQrBitmap(payload).also(bitmapReference::set)
            }
            currentCoroutineContext().ensureActive()
            displayState = QrDisplayState.Ready(payload, bitmap)
            bitmapReference.compareAndSet(bitmap, null)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: WriterException) {
            displayState = QrDisplayState.Failed(payload)
        } finally {
            bitmapReference.getAndSet(null)?.recycle()
        }
    }

    DisposableEffect(displayState) {
        val bitmap = (displayState as? QrDisplayState.Ready)?.bitmap
        onDispose { bitmap?.recycle() }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Location QR code",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
            )
            when (val state = displayState) {
                QrDisplayState.Empty -> Text(
                    text = "Select a location on the map to generate its QR code.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is QrDisplayState.Generating -> if (state.payload == payload) {
                    Text(
                        text = "Generating QR code…",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is QrDisplayState.Ready -> if (state.payload == payload) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = state.bitmap.asImageBitmap(),
                            contentDescription = "QR code for the selected coordinates",
                            modifier = Modifier.size(220.dp),
                        )
                    }
                    Text(
                        text = state.payload,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                is QrDisplayState.Failed -> if (state.payload == payload) {
                    Text(
                        text = "The QR code could not be generated.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
