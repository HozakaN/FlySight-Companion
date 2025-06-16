package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind.FlyBlindSessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind.FlyBlindVideoController
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun FlyBlindSessionPlayer(
    controller: FlyBlindSessionController,
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    val videoController = remember { controller.videoController as FlyBlindVideoController }

    val elevation by controller.elevation.collectAsState()

    val distance by videoController.distance.collectAsState()
    val heading by controller.headingToRefPoint.collectAsState()
    val headingStr by videoController.headingStr.collectAsState()
    val displayMap by videoController.displayMap

    FlyBlindSessionContainer(
        modifier = Modifier
            .fillMaxSize(),
        controller = controller,
        elevation = elevation,
        distance = distance,
        heading = heading,
        headingStr = headingStr,
        displayMap = displayMap,
        onExitClicked = onExitClicked,
        resetExitDetection = resetExitDetection
    )

}

@Composable
private fun FlyBlindSessionContainer(
    modifier: Modifier = Modifier,
    controller: FlyBlindSessionController,
    elevation: Int,
    distance: String,
    heading: Double,
    headingStr: String,
    displayMap: Boolean,
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    LockManagerContainer(
        modifier = modifier,
        lockedContent = {
            FlyBlindLockedContent(
                onExitClicked = onExitClicked,
                resetExitDetection = resetExitDetection
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (displayMap) {
                    val gnssData by controller.gnssFlow.collectAsState(initial = null)
                    val exitFound = controller.exitFound.collectAsState()
                    val referencePoint = controller.referencePoint
                    val cameraPositionState = rememberCameraPositionState()

                    val gpsData = gnssData

                    // Calculate appropriate zoom level based on points
                    val calculatedZoom = remember(gpsData) {
                        if (gpsData != null) {
                            calculateZoomLevel(
                                LatLng(gpsData.lat, gpsData.lon),
                                LatLng(
                                    referencePoint.coords.latitude,
                                    referencePoint.coords.longitude
                                )
                            )
                        } else {
                            13f // Default zoom if no gpsData
                        }
                    }

                    val cameraZoom by animateFloatAsState(
                        targetValue = calculatedZoom,
                        animationSpec = tween(durationMillis = 1_500)
                    )

                    LaunchedEffect(gpsData, cameraZoom) {
                        val data = gpsData
                        if (data != null) {
                            // Calculate the center point between the reference and current position
                            val refLat = referencePoint.coords.latitude
                            val refLng = referencePoint.coords.longitude
                            val centerLat = (data.lat + refLat) / 2
                            val centerLng = (data.lon + refLng) / 2

                            cameraPositionState.move(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(
                                        LatLng(centerLat, centerLng),
                                        cameraZoom
                                    )
                                )
                            )
                        }
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { onClick() },
                        properties = MapProperties(
                            isMyLocationEnabled = false,
                            mapType = MapType.HYBRID,
                            isBuildingEnabled = false
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = true,
                            mapToolbarEnabled = false,
                            indoorLevelPickerEnabled = false,
                            myLocationButtonEnabled = false,
                            rotationGesturesEnabled = false,
                            scrollGesturesEnabled = false,
                            scrollGesturesEnabledDuringRotateOrZoom = false,
                            tiltGesturesEnabled = false,
                            zoomGesturesEnabled = false
                        )
                    ) {
                        gpsData?.let { data ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(
                                        data.lat,
                                        data.lon
                                    )
                                ),
                                title = "Current Position"
                            )
                        }

                        val jumpPath = remember { mutableStateListOf<LatLng>() }
                        LaunchedEffect(gpsData) {
                            gpsData?.let { data ->
                                jumpPath.add(LatLng(data.lat, data.lon))
                                if (jumpPath.size > 1000) {
                                    jumpPath.removeAt(0)
                                }
                            }
                        }

                        if (jumpPath.size > 1) {
                            Polyline(
                                points = jumpPath,
                                color = Color.Red,
                                width = 5f
                            )
                        }

                        controller.referencePoint.let { refPoint ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(
                                        refPoint.coords.latitude,
                                        refPoint.coords.longitude
                                    )
                                ),
                                title = refPoint.name,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )
                        }
                    }
                }
            }
            Column {

                FlyBlindInfoContainer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    heading = heading,
                    headingStr = headingStr,
                    distance = distance,
                    isBellyFlying = controller.configuration.isBellyFlying,
                )
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                val timeMutableSource = controller.timeMutableSource
                timeMutableSource?.let { source ->
                    TimeControlContainer(
                        timeMutableSource = source
                    )
                }
            }
        }
    }
}

@Composable
fun FlyBlindInfoContainer(
    modifier: Modifier = Modifier,
    heading: Double,
    headingStr: String,
    distance: String,
    isBellyFlying: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FText(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(start = 32.dp),
            textAlign = TextAlign.Start,
            text = headingStr,
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )
        Spacer(modifier = Modifier.requiredHeight(32.dp))
        OrientableArrow(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(start = 64.dp),
            heading = heading
        )
        Spacer(modifier = Modifier.requiredHeight(32.dp))
        FText(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(start = 32.dp),
            textAlign = TextAlign.Start,
            text = distance,
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )
        Spacer(modifier = Modifier.requiredHeight(32.dp))
        FText(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(start = 32.dp),
            textAlign = TextAlign.Start,
            text = if (isBellyFlying) "Belly fly" else "Back fly",
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )
    }
}

@Preview
@Composable
fun HeadingContainerPreview() {
    FlyBlindInfoContainer(
        modifier = Modifier.fillMaxSize(),
        heading = 0.0,
        headingStr = "0.00 deg",
        distance = "0 NM",
        isBellyFlying = true
    )
}

@Composable
fun OrientableArrow(
    modifier: Modifier = Modifier,
    heading: Double
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = 4f
                    scaleY = 4f
                    rotationZ = heading.toFloat()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "",
                tint = Color.Green,
            )
        }
    }
}

@Composable
private fun FlyBlindLockedContent(
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.displayCutout)
    ) {
        FloatingActionButton(
            onClick = onExitClicked,
            containerColor = MaterialTheme.colorScheme.error,
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.requiredHeight(8.dp))
        Button(
            onClick = resetExitDetection,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            FText(
                text = "Reset exit detection",
                configuration = FlySightTheme.typography.plainScreenTextLarge
            )
        }
    }
}

/**
 * Calculate an appropriate zoom level to show both points with at least 5 sq km visible
 */
private fun calculateZoomLevel(point1: LatLng, point2: LatLng): Float {
    // Calculate distance between points
    val distance = haversineDistance(
        point1.latitude, point1.longitude,
        point2.latitude, point2.longitude
    )

    // Calculate zoom based on distance
    // Google Maps zoom: ~0 is global view, ~20 is building level detail
    // Each zoom level halves the visible area

    // Ensure minimum 5 sq km visible (approx. 2.2km x 2.2km)
    val minVisibleDistance = 2200.0 // meters

    // If points are too close, use min distance to ensure 5 sq km visible
    val adjustedDistance = maxOf(distance, minVisibleDistance)

    // Empirical formula for zoom based on distance
    // Lower value = more zoomed out
    return when {
        distance < 500 -> 16f
        distance < 1000 -> 15f
        distance < 2000 -> 14f
        distance < 4000 -> 13f
        distance < 8000 -> 12f
        distance < 16000 -> 11f
        distance < 32000 -> 10f
        else -> 9f
    }
}

/**
 * Calculate distance between two points using Haversine formula
 * Returns distance in meters
 */
private fun haversineDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371000.0 // Earth radius in meters

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)

    val c = 2 * asin(sqrt(a))

    return R * c
}