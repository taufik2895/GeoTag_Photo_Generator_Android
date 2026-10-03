package com.geotagphotogenerator

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException

private const val MAX_PREVIEW_DIMENSION_PX = 1280

@Composable
internal fun PhotoPreviewCard(
    photoUri: Uri?,
    photoMimeType: String?,
    requestId: Int,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
) {
    val contentResolver = LocalContext.current.contentResolver
    var previewState by remember(photoUri, requestId) {
        mutableStateOf<PreviewState>(
            if (photoUri == null) PreviewState.Empty else PreviewState.Loading,
        )
    }

    LaunchedEffect(photoUri, requestId) {
        val uri = photoUri ?: return@LaunchedEffect
        var pendingBitmap: Bitmap? = null

        try {
            withContext(Dispatchers.IO) {
                pendingBitmap = decodePhotoPreview(contentResolver, uri)
            }
            previewState = PreviewState.Ready(checkNotNull(pendingBitmap))
            pendingBitmap = null
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: FileNotFoundException) {
            previewState = PreviewState.Error(
                "This photo is no longer available. Select another photo.",
            )
        } catch (exception: SecurityException) {
            previewState = PreviewState.Error(
                "Access to this photo was denied. Select another photo.",
            )
        } catch (exception: IOException) {
            previewState = PreviewState.Error(
                "This photo could not be decoded. Select another photo.",
            )
        } catch (exception: IllegalArgumentException) {
            previewState = PreviewState.Error(
                "This photo has invalid image data. Select another photo.",
            )
        } catch (error: OutOfMemoryError) {
            previewState = PreviewState.Error(
                "This photo is too large to preview. Select a smaller photo.",
            )
        } finally {
            pendingBitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        }
    }

    val readyBitmap = (previewState as? PreviewState.Ready)?.bitmap
    DisposableEffect(readyBitmap) {
        onDispose {
            readyBitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = previewState) {
                PreviewState.Empty -> PreviewMessage(
                    title = "Photo preview",
                    message = "Select an existing photo to get started.",
                )

                PreviewState.Loading -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Preparing photo preview…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is PreviewState.Ready -> Image(
                    bitmap = state.bitmap.asImageBitmap(),
                    contentDescription = if (photoMimeType == null) {
                        "Selected photo preview"
                    } else {
                        "Selected photo preview, $photoMimeType"
                    },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )

                is PreviewState.Error -> PreviewMessage(
                    title = "Preview unavailable",
                    message = state.message,
                )
            }
        }
    }
}

@Composable
private fun PreviewMessage(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private sealed interface PreviewState {
    data object Empty : PreviewState
    data object Loading : PreviewState
    data class Ready(val bitmap: Bitmap) : PreviewState
    data class Error(val message: String) : PreviewState
}

private fun decodePhotoPreview(
    contentResolver: ContentResolver,
    uri: Uri,
): Bitmap {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return decodeWithImageDecoder(contentResolver, uri)
    }

    return decodeWithBitmapFactory(contentResolver, uri)
}

private fun decodeWithImageDecoder(
    contentResolver: ContentResolver,
    uri: Uri,
): Bitmap {
    val source = ImageDecoder.createSource(contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, imageInfo, _ ->
        val sourceSize = imageInfo.size
        if (sourceSize.width <= 0 || sourceSize.height <= 0) {
            throw IOException("The selected URI does not contain a decodable image.")
        }
        val scale = minOf(
            1.0,
            MAX_PREVIEW_DIMENSION_PX / maxOf(sourceSize.width, sourceSize.height).toDouble(),
        )
        decoder.setTargetSize(
            (sourceSize.width * scale).toInt().coerceAtLeast(1),
            (sourceSize.height * scale).toInt().coerceAtLeast(1),
        )
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
}

private fun decodeWithBitmapFactory(
    contentResolver: ContentResolver,
    uri: Uri,
): Bitmap {
    val orientation = contentResolver.openInputStream(uri)?.use { input ->
        ExifInterface(input).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    } ?: throw FileNotFoundException("The selected photo is no longer available.")

    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    val boundsStream = contentResolver.openInputStream(uri)
        ?: throw FileNotFoundException("The selected photo could not be opened.")
    boundsStream.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        throw IOException("The selected URI does not contain a decodable image.")
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDimension = MAX_PREVIEW_DIMENSION_PX,
        )
        inPreferredConfig = Bitmap.Config.ARGB_8888
        inScaled = false
    }
    val decodedBitmap = contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, options)
    } ?: throw IOException("The selected photo could not be decoded.")

    var ownsDecodedBitmap = true
    try {
        val orientedBitmap = applyExifOrientation(decodedBitmap, orientation)
        if (orientedBitmap !== decodedBitmap) {
            decodedBitmap.recycle()
        }
        ownsDecodedBitmap = false
        return orientedBitmap
    } finally {
        if (ownsDecodedBitmap && !decodedBitmap.isRecycled) {
            decodedBitmap.recycle()
        }
    }
}

private fun calculateSampleSize(
    width: Int,
    height: Int,
    maxDimension: Int,
): Int {
    var sampleSize = 1
    while (
        sampleSize <= Int.MAX_VALUE / 2 &&
        maxOf(width / (sampleSize * 2), height / (sampleSize * 2)) >= maxDimension
    ) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun applyExifOrientation(
    bitmap: Bitmap,
    orientation: Int,
): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return bitmap
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true,
    )
}
