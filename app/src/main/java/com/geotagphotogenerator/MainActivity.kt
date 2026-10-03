package com.geotagphotogenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.geotagphotogenerator.ui.theme.GeoTagPhotoGeneratorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoTagPhotoGeneratorTheme {
                GeoTagPhotoGeneratorApp()
            }
        }
    }
}

@Composable
private fun GeoTagPhotoGeneratorApp() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(text = "GeoTag Photo Generator")
    }
}

@Preview(showBackground = true)
@Composable
private fun GeoTagPhotoGeneratorAppPreview() {
    GeoTagPhotoGeneratorTheme {
        GeoTagPhotoGeneratorApp()
    }
}
