package com.geotagphotogenerator

import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.LuminanceSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationQrCodeTest {
    @Test
    fun referenceCoordinateEncodesExactMapsUrl() {
        assertQrPayload(
            coordinate = MapCoordinate(-7.19005, 107.90158),
            expected = "https://maps.google.com/?q=-7.19005,107.90158&t=h&z=18",
        )
    }

    @Test
    fun userExamplePreservesCoordinatePrecisionAndSeparatesMapOptions() {
        assertQrPayload(
            coordinate = MapCoordinate(-6.9705992, 107.7648696),
            expected = "https://maps.google.com/?q=-6.9705992,107.7648696&t=h&z=18",
        )
    }

    @Test
    fun anotherCoordinatePreservesFullDoubleValues() {
        assertQrPayload(
            coordinate = MapCoordinate(-6.207455739618553, 107.18673706054688),
            expected =
                "https://maps.google.com/?q=-6.207455739618553,107.18673706054688&t=h&z=18",
        )
    }

    private fun assertQrPayload(coordinate: MapCoordinate, expected: String) {
        assertEquals(expected, buildGoogleMapsQrPayload(coordinate))
        val matrix = createLocationQrMatrix(coordinate, size = 512)
        val decoded = MultiFormatReader().decode(
            BinaryBitmap(HybridBinarizer(MatrixLuminanceSource(matrix))),
        )
        assertEquals(expected, decoded.text)
    }
}

private class MatrixLuminanceSource(
    private val matrix: BitMatrix,
) : LuminanceSource(matrix.width, matrix.height) {
    private val luminance = ByteArray(matrix.width * matrix.height) { index ->
        val x = index % matrix.width
        val y = index / matrix.width
        if (matrix[x, y]) 0 else 0xff.toByte()
    }

    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        val result = row?.takeIf { it.size >= width } ?: ByteArray(width)
        System.arraycopy(luminance, y * width, result, 0, width)
        return result
    }

    override fun getMatrix(): ByteArray = luminance
}
