package com.geotagphotogenerator

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geotagphotogenerator.ui.theme.GeoTagPhotoGeneratorTheme
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoTagPhotoGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    GeoTagPhotoGeneratorApp()
                }
            }
        }
    }
}

@Composable
private fun GeoTagPhotoGeneratorApp() {
    val contentResolver = LocalContext.current.contentResolver
    var selectedPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var selectedPhotoMimeType by rememberSaveable { mutableStateOf<String?>(null) }
    var photoPreviewRequestId by rememberSaveable { mutableStateOf(0) }
    var selectionError by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCoordinate by rememberSaveable(stateSaver = MapCoordinateSaver) {
        mutableStateOf<MapCoordinate?>(null)
    }
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                val mimeType = withContext(Dispatchers.IO) {
                    validateImageUri(contentResolver, uri)
                }
                selectedPhotoUri = uri
                selectedPhotoMimeType = mimeType
                photoPreviewRequestId += 1
                selectionError = null
            } catch (exception: IllegalArgumentException) {
                selectionError = "Choose a supported image file."
            } catch (exception: FileNotFoundException) {
                selectionError = "The selected image is no longer available. Choose another image."
            } catch (exception: SecurityException) {
                selectionError = "Access to the selected image was denied. Choose another image."
            } catch (exception: IOException) {
                selectionError = "The selected image could not be opened. Choose another image."
            }
        }
    }

    val selectPhoto = {
        selectionError = null
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val isCompact = maxWidth < 600.dp
        if (isCompact) {
            CompactLayout(
                selectedPhotoUri = selectedPhotoUri,
                selectedPhotoMimeType = selectedPhotoMimeType,
                photoPreviewRequestId = photoPreviewRequestId,
                selectionError = selectionError,
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = { selectedCoordinate = it },
                onSelectPhoto = selectPhoto,
            )
        } else {
            WideLayout(
                selectedPhotoUri = selectedPhotoUri,
                selectedPhotoMimeType = selectedPhotoMimeType,
                photoPreviewRequestId = photoPreviewRequestId,
                selectionError = selectionError,
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = { selectedCoordinate = it },
                onSelectPhoto = selectPhoto,
            )
        }
    }
}

@Composable
private fun CompactLayout(
    selectedPhotoUri: Uri?,
    selectedPhotoMimeType: String?,
    photoPreviewRequestId: Int,
    selectionError: String?,
    selectedCoordinate: MapCoordinate?,
    onCoordinateSelected: (MapCoordinate) -> Unit,
    onSelectPhoto: () -> Unit,
) {
    val workflowSteps = listOf(
        "Select existing photo",
        "Choose location",
        "Set date and time",
        "Generate and save",
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderCard()

            PhotoPreviewCard(
                photoUri = selectedPhotoUri,
                photoMimeType = selectedPhotoMimeType,
                requestId = photoPreviewRequestId,
                modifier = Modifier.fillMaxWidth(),
                height = 220.dp,
            )

            GoogleMapCard(
                modifier = Modifier.fillMaxWidth(),
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = onCoordinateSelected,
            )

            selectionError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            WorkflowSteps(workflowSteps)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSelectPhoto,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Select existing photo")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Review")
                }
            }
        }
    }
}

@Composable
private fun WideLayout(
    selectedPhotoUri: Uri?,
    selectedPhotoMimeType: String?,
    photoPreviewRequestId: Int,
    selectionError: String?,
    selectedCoordinate: MapCoordinate?,
    onCoordinateSelected: (MapCoordinate) -> Unit,
    onSelectPhoto: () -> Unit,
) {
    val workflowSteps = listOf(
        "Select existing photo",
        "Choose location",
        "Set date and time",
        "Generate and save",
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
        ) {
            NavigationRailItem(
                selected = true,
                onClick = { },
                icon = { },
                label = { Text("Workflow") },
            )
            NavigationRailItem(
                selected = false,
                onClick = { },
                icon = { },
                label = { Text("Map") },
            )
            NavigationRailItem(
                selected = false,
                onClick = { },
                icon = { },
                label = { Text("Export") },
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderCard()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PhotoPreviewCard(
                    photoUri = selectedPhotoUri,
                    photoMimeType = selectedPhotoMimeType,
                    requestId = photoPreviewRequestId,
                    modifier = Modifier.weight(1f),
                    height = 240.dp,
                )

                WorkflowSteps(workflowSteps, modifier = Modifier.weight(1f))
            }

            GoogleMapCard(
                modifier = Modifier.fillMaxWidth(),
                selectedCoordinate = selectedCoordinate,
                onCoordinateSelected = onCoordinateSelected,
            )

            selectionError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSelectPhoto,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Select existing photo")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Generate GeoTag")
                }
            }
        }
    }
}

private fun validateImageUri(contentResolver: ContentResolver, uri: Uri): String {
    val mimeType = contentResolver.getType(uri)
        ?: throw FileNotFoundException("The selected image is no longer available.")
    require(mimeType.startsWith("image/"))

    val imageStream = contentResolver.openInputStream(uri)
        ?: throw FileNotFoundException("The selected image could not be opened.")
    imageStream.use { }

    return mimeType
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "GeoTag Photo Generator",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Create a geo-tagged image from an existing photo and a selected map coordinate.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun WorkflowSteps(
    steps: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Workflow",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                shape = MaterialTheme.shapes.small,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GeoTagPhotoGeneratorAppPreview() {
    GeoTagPhotoGeneratorTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            GeoTagPhotoGeneratorApp()
        }
    }
}
