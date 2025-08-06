package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import fr.hozakan.flysightcompanion.composablecommons.BatteryLevelContainer
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.math.computeGlideRatio
import fr.hozakan.flysightcompanion.framework.math.computeGroundSpeed
import fr.hozakan.flysightcompanion.framework.math.computeInverseGlideRatio
import fr.hozakan.flysightcompanion.framework.tooling.triple
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.DeviceConnectionState
import fr.hozakan.flysightcompanion.model.DeviceMode
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.session.Flare
import fr.hozakan.flysightcompanion.model.session.profile.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItem
import fr.hozakan.flysightcompanion.model.session.profile.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.profile.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.model.ui.SpeedOrientation
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.detector.FlareState
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc.PpcHudSessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionEvent
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc.PpcHudVideoControllerImpl
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.BatteryLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun HudSessionPlayer(
    controller: PpcHudSessionController,
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    val displayGrid = controller.profile.displayGrid
    var uiLocked by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .padding(8.dp),
    ) {
        var displayUnlockUi by remember { mutableStateOf(false) }
        var counter by remember { mutableIntStateOf(0) }
        var uiTouched by remember { mutableStateOf(false) }

        LaunchedEffect(counter, uiTouched, uiLocked) {
            if (uiLocked && !uiTouched && counter > 0) {
                displayUnlockUi = true
                while (displayUnlockUi && isActive) {
                    delay(5_000)
                    if (uiLocked && !uiTouched) {
                        displayUnlockUi = false
                    }
                }
            }
        }

        LaunchedEffect(uiLocked, counter, uiTouched) {
            if (!uiLocked && !uiTouched) {
                val intermediateCounter = counter
                delay(10_000)
                if (!uiLocked && intermediateCounter == counter && !uiTouched) {
                    uiLocked = true
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (!displayUnlockUi) {
                            counter++
                        }
                    }
                }
        ) {
            when (displayGrid) {
                DisplayGrid.InlineLeft -> InlinePlayerScreen(
                    inlinePlayerDirection = InlinePlayerDirection.Left,
                    controller = controller
                )

                DisplayGrid.InlineRight -> InlinePlayerScreen(
                    inlinePlayerDirection = InlinePlayerDirection.Right,
                    controller = controller
                )

                DisplayGrid.TwoByTwo -> TwoByTwoGridPlayerScreen(
                    controller = controller
                )

                DisplayGrid.TwoOnEachSide -> SideDisplayItemsPlayerScreen(
                    controller = controller,
                    caseNumber = 2
                )

                DisplayGrid.ThreeOnEachSide -> SideDisplayItemsPlayerScreen(
                    controller = controller,
                    caseNumber = 3
                )
            }
            if (displayUnlockUi) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    LockContainer(
                        locked = uiLocked,
                        onUnlocked = {
                            uiLocked = false
                            displayUnlockUi = false
                        },
                        onUiTouchChanged = { touched ->
                            uiTouched = touched
                        }
                    )
                }
            }
            if (!uiLocked) {
                PpcHudLockedContent(
                    onExitClicked = onExitClicked,
                    resetExitDetection = resetExitDetection
                )
            }
        }
        val timeMutableSource = controller.timeMutableSource
        timeMutableSource?.let { source ->
            TimeControlContainer(
                modifier = Modifier/*.weight(1f)*/,
                timeMutableSource = source
            )
        }
    }
}

@Composable
private fun TwoByTwoGridPlayerScreen(
    controller: PpcHudSessionController
) {
    val displayItems = controller.profile.displayItems
    Row(
//        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 2 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
            }
        }
    }
}

@Composable
private fun SideDisplayItemsPlayerScreen(
    controller: PpcHudSessionController,
    caseNumber: Int
) {
    val displayItems = controller.profile.displayItems
    Row {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                if (caseNumber == 2) {
                    displayItems.firstOrNull { it.caseIndex == 0 && it.indexInCase == 2 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                if (caseNumber == 2) {
                    displayItems.firstOrNull { it.caseIndex == 1 && it.indexInCase == 2 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
            if (caseNumber == 3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (controller.profile.showGridLines) {
                                Modifier.border(width = 2.dp, color = Color.Green)
                            } else {
                                Modifier
                            }
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 0 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                    displayItems.firstOrNull { it.caseIndex == 2 && it.indexInCase == 1 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
        }
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        SessionMainContainer(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            controller = controller
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                if (caseNumber == 2) {
                    displayItems.firstOrNull { it.caseIndex == 3 && it.indexInCase == 2 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (controller.profile.showGridLines) {
                            Modifier.border(width = 2.dp, color = Color.Green)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 0 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 1 }?.let { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
                if (caseNumber == 2) {
                    displayItems.firstOrNull { it.caseIndex == 4 && it.indexInCase == 2 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
            if (caseNumber == 3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (controller.profile.showGridLines) {
                                Modifier.border(width = 2.dp, color = Color.Green)
                            } else {
                                Modifier
                            }
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    displayItems.firstOrNull { it.caseIndex == 5 && it.indexInCase == 0 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                    displayItems.firstOrNull { it.caseIndex == 5 && it.indexInCase == 1 }
                        ?.let { item ->
                            DisplayCapabilityContainer(
                                item = item,
                                config = controller.profile.configFile,
                                player = controller
                            )
                        }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DisplayCapabilityContainer(
    item: DisplayItem,
    config: ConfigFile,
    player: PpcHudSessionController
) {
    val gnssData: GnssData? by player.gnssFlow.collectAsState(initial = null)
    when (item.displayableCapability) {
        DisplayableCapability.HorizontalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Horizontal,
            player = player,
            suffix = "km/h"
        )

        DisplayableCapability.VerticalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Vertical,
            player = player,
            suffix = "km/h"
        )

        DisplayableCapability.TotalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Total,
            player = player,
            suffix = "km/h"
        )

        DisplayableCapability.Elevation -> TagAndValueContainer(
            tag = "Elv",
            value = "${gnssData?.hMsl?.minus(config.dzElev)}",
            suffix = "m"
        )

        DisplayableCapability.Altitude -> TagAndValueContainer(
            tag = "Alt",
            value = "${gnssData?.hMsl}",
            suffix = "m"
        )

        DisplayableCapability.DistanceToReferencePoint -> {
            val refPointId =
                (item.bag as DisplayItemBundle.DistanceToRefPointBundle?)?.referencePoint?.id
            val refPointDistance by player.referencePointDistances
                .map { map ->
                    map.filter { mapEntry -> mapEntry.key == refPointId }.map { it.value }
                }.collectAsState(initial = emptyList())
            val distance = refPointDistance.firstOrNull()

            TagAndValueContainer(
                tag = "RefPt",
                value = if (distance != null) String.format("%.2f", distance) else "--",
                suffix = "NM"
            )
        }

        DisplayableCapability.Latitude -> TagAndValueContainer(
            tag = "Lat",
            value = "${gnssData?.lat}"
        )

        DisplayableCapability.Longitude -> TagAndValueContainer(
            tag = "Lon",
            value = "${gnssData?.lon}"
        )

        DisplayableCapability.GlideRatio -> TagAndValueContainer(
            tag = "GR",
            value = remember(gnssData?.velN, gnssData?.velE, gnssData?.velD) {
                String.format(
                    "%.2f", computeGlideRatio(
                        gnssData?.velD ?: 0,
                        computeGroundSpeed(
                            gnssData?.velN?.toDouble() ?: 0.0,
                            gnssData?.velE?.toDouble() ?: 0.0
                        )
                    )
                )
            }
        )

        DisplayableCapability.InverseGlideRatio -> TagAndValueContainer(
            tag = "IGR",
            value = remember(gnssData?.velN, gnssData?.velE, gnssData?.velD) {
                String.format(
                    "%.2f", computeInverseGlideRatio(
                        gnssData?.velD ?: 0,
                        computeGroundSpeed(
                            gnssData?.velN?.toDouble() ?: 0.0,
                            gnssData?.velE?.toDouble() ?: 0.0
                        )
                    )
                )
            }
        )

        DisplayableCapability.DiveAngle -> TagAndValueContainer(
            tag = "DiveA",
            value = "${gnssData?.lon}",
            suffix = "°"
        )

        DisplayableCapability.VelN -> TagAndValueContainer(
            tag = "velN",
            value = "${gnssData?.velN}",
            suffix = "m/s"
        )

        DisplayableCapability.VelE -> TagAndValueContainer(
            tag = "velE",
            value = "${gnssData?.velE}",
            suffix = "m/s"
        )

        DisplayableCapability.VelD -> TagAndValueContainer(
            tag = "velD",
            value = "${gnssData?.velD}",
            suffix = "m/s"
        )

        DisplayableCapability.TimeInWindow -> {
            val timer by player.timeInWindow.collectAsState()
            TagAndValueContainer(
                tag = "PPC time",
                value = String.format("%.1f", timer),
                suffix = "s"
            )
        }

        DisplayableCapability.DistanceInWindow -> {
            val distance by player.distanceInWindow.collectAsState()
            TagAndValueContainer(
                tag = "PPC distance",
                value = String.format("%d", distance),
                suffix = "m"
            )
        }

        DisplayableCapability.SpeedInWindow -> {
            val speed by player.speedInWindow.collectAsState()
            TagAndValueContainer(
                tag = "PPC speed",
                value = String.format("%d", speed),
                suffix = "km/h"
            )
        }

        DisplayableCapability.FlareCount -> {
            val flares by player.registeredFlares.collectAsState()
            TagAndValueContainer(
                tag = "Flares",
                value = "${flares.size}"
            )
        }

        DisplayableCapability.LastFlareResult -> {
            val flares by player.registeredFlares.collectAsState()
            val lastFlare = remember(flares) { flares.lastOrNull() }
            TagAndValueContainer(
                tag = "UP",
                value = "${lastFlare?.gain ?: "--"}",
                suffix = "m"
            )
        }
    }
}

enum class InlinePlayerDirection {
    Left,
    Right
}

@Composable
private fun InlinePlayerScreen(
    controller: PpcHudSessionController,
    inlinePlayerDirection: InlinePlayerDirection
) {
    val displayItems = controller.profile.displayItems
    Row {
        if (inlinePlayerDirection == InlinePlayerDirection.Right) {
            SessionMainContainer(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(8.dp),
                controller = controller
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .then(
                    if (controller.profile.showGridLines) {
                        Modifier.border(width = 2.dp, color = Color.Green)
                    } else {
                        Modifier
                    }
                ),
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn {
                items(displayItems) { item ->
                    DisplayCapabilityContainer(
                        item = item,
                        config = controller.profile.configFile,
                        player = controller
                    )
                }
            }
        }
        if (inlinePlayerDirection == InlinePlayerDirection.Left) {
            SessionMainContainer(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(8.dp),
                controller = controller
            )
        }
    }
}

@Composable
private fun SessionMainContainer(
    modifier: Modifier = Modifier,
    controller: PpcHudSessionController
) {
    var alarmMessage by remember { mutableStateOf("") }

    // Animation values
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(alarmMessage) {
        val message = alarmMessage
        if (message.isNotBlank()) {
            launch {
                // Reset animations to initial values
                scale.snapTo(1f)
                alpha.snapTo(1f)
                // Run animations in parallel
                launch {
                    scale.animateTo(
                        targetValue = 2.5f,
                        animationSpec = tween(durationMillis = 2000)
                    )
                }

                launch {
                    // Start fading out after a short delay
                    delay(500)
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 1500)
                    )
                    // Hide alarm when animation completes
                    alarmMessage = ""
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        controller.sessionEvents.collectLatest { event ->
            alarmMessage = when (val evt = event) {
                is SessionEvent.AlarmEvent -> {
                    when (evt.alarm.alarmType) {
                        AlarmType.NoAlarm -> ""
                        AlarmType.Beep -> ""
                        AlarmType.ChirpUp -> ""
                        AlarmType.ChirpDown -> ""
                        AlarmType.PlayFile -> evt.alarm.alarmFile
                    }
                }

                is SessionEvent.ExitFound -> "Exit detected"
                is SessionEvent.PerformanceLaneStart -> "Lane start"
                is SessionEvent.PlayFileEvent -> ""
                is SessionEvent.PlayTextEvent -> ""
                SessionEvent.CompetitionWindowEntered -> "Competition window entered"
                SessionEvent.CompetitionWindowExited -> "Competition window exited"
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (controller.profile.showPerformanceLane) {
            PerformanceLaneContainer(controller) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (controller.profile.showMap) {
                        GMapContainer(controller)
                    }
                    val heading by controller.heading.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        OrientableArrow(
                            heading = heading,
                            logoSize = 40.dp
                        )
                    }

                    val deviceState by controller.deviceState.collectAsState()

                    val hasFix by controller.hasFix.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.TopStart
                    ) {

                        deviceState?.let { (pair1, pair2) ->
                            val connectionState = pair1.first
                            val deviceMode = pair1.second
                            val batteryLevel = pair2.first
                            val isCharging = pair2.second

                            Column {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            color = Color.Black,
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BatteryLevelContainer(batteryLevel = batteryLevel, isCharging = isCharging)
                                    if (connectionState != DeviceConnectionState.Connected) {
                                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                                        Text(
                                            text = when (connectionState) {
                                                DeviceConnectionState.Connecting -> "Connecting"
                                                DeviceConnectionState.ConnectionError -> "Error"
                                                DeviceConnectionState.Disconnected -> "Disconnected"
                                                else -> {
                                                    ""
                                                }
                                            },
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                if (deviceMode != DeviceMode.Active) {
                                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color.Black,
                                                shape = RoundedCornerShape(32.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = deviceMode.name,
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                if (!hasFix) {
                                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color.Black,
                                                shape = RoundedCornerShape(32.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "NO FIX",
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        OrientableArrow(
                            heading = heading,
                            logoSize = 40.dp
                        )
                    }
                }
            }
        } else if (controller.profile.showMap) {
            GMapContainer(controller)
        }

        if (controller.profile.displayFlareDetector) {
            val flareState by controller.currentFlareState.collectAsState()
            when (flareState) {
                is FlareState.FlareDone,
                is FlareState.Flaring -> {
                    FlareContainer(
                        flareState
                    )
                }

                FlareState.Idle -> {}
            }
        }

        // Visual alarm overlay
        if (alarmMessage.isNotBlank()) {
            Text(
                text = alarmMessage,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun FlareContainer(
    flareState: FlareState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (flareState) {
            is FlareState.FlareDone -> {}
            is FlareState.Flaring -> {
                OngoingFlareContainer(
                    state = flareState
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun OngoingFlareContainer(
    state: FlareState.Flaring
) {
    if (state.flareData.isEmpty()) return

    val altitudeGain = state.altitudeGain

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val heightRepresentation = when {
            altitudeGain > 70 -> 150f
            else -> 90f
        }

        val maxTimeDiff = (state.timeSinceStart / 1000f)
        val widthRepresentation =
            when {
                maxTimeDiff < 10 -> 12
                maxTimeDiff >= 10 && maxTimeDiff < 17 -> 20
                maxTimeDiff >= 17 -> 30
                else -> 12
            }

        // How much is 1m in pixels
        val heightFactor = canvasHeight / heightRepresentation

        // If we have more than one data point, draw the gain line
        if (state.flareData.size > 1) {
            // Create points for the line
            val points = state.flareData.map { gnssData ->
                val gain = gnssData.hMsl - state.startAltitude

                // Calculate the x position based on time (0 to 20 seconds)
                val timeDiffMs = gnssData.iTow.toInt() - state.startTime

                val timeDiffSec = timeDiffMs / 1000f
                val x =
                    (timeDiffSec / widthRepresentation.toFloat()) * canvasWidth // Scale to canvas width
                val y = canvasHeight - (gain.toFloat() * heightFactor)

                Offset(x, y)
            }

            // Draw the gain line connecting all points
            if (points.size >= 2) {
                drawPoints(
                    points = points,
                    pointMode = PointMode.Polygon,
                    color = Color.Green,
                    strokeWidth = 20.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Draw gain text at the right of the last point
                val lastPoint = points.last()

                val distanceToLine = 8.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    "+${altitudeGain} m",
                    lastPoint.x + distanceToLine,
                    lastPoint.y - distanceToLine,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GREEN
                        textSize = 30.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }
        }
    }
}

@Composable
private fun GMapContainer(sessionController: PpcHudSessionController) {
    val gnssData by sessionController.gnssFlow.collectAsState(initial = null)

    val cameraPositionState = rememberCameraPositionState()


    val perfLanes by sessionController.performanceLanes.collectAsState()

    val exitFound by sessionController.exitFound.collectAsState()
    val gpsData = gnssData
    val cameraZoom by animateFloatAsState(
        targetValue = if (exitFound == null ||
            (gpsData != null && sessionController.profile.competitionWindowBottom > gpsData.hMsl)
        ) {
            13f
        } else {
            14f
        },
        animationSpec = tween(durationMillis = 1_500)
    )

    LaunchedEffect(gpsData, cameraZoom) {
        val data = gpsData
        if (data != null) {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(data.lat, data.lon),
                        cameraZoom
                    )
                )
            )
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
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

            sessionController.profile.referencePoint?.let { refPoint ->
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

            // Draw performance lanes
            if (sessionController.profile.showPerformanceLaneInMap) {
                perfLanes.forEach { lane ->
                    val points = lane.points.map { coord ->
                        LatLng(coord.latitude, coord.longitude)
                    }

                    if (points.size >= 2) {
                        with(LocalDensity.current) {
                            Polyline(
                                points = points,
                                color = if (lane.isReference) Color.Red else Color.Green,
                                width = 2.dp.toPx(),
                                pattern = if (lane.isReference) null else
                                    listOf(Dash(20f), Gap(10f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun PerformanceLaneContainer(
    sessionController: PpcHudSessionController,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val exitPoint by sessionController.exitFound.collectAsState()
    val laneStartPoint by sessionController.laneStartPoint.collectAsState()
    val distanceFromLanes by sessionController.distanceToCenter.collectAsState()

    // Define colors for different states
    val gray = Color.Gray
    val purple = Color.Magenta
    val green = Color.Green

    // Calculate bar colors based on state
    val leftBarColor by animateColorAsState(
        targetValue = when {
            exitPoint == null -> gray // Not exited yet
            laneStartPoint == null -> purple // Exited but no lane start
            distanceFromLanes == null -> green // Lane started but no distance info
            distanceFromLanes!! < -0.5f -> {
                // Compute color gradient from green to red based on distance
                // -0.5 = green, -0.95 = red
                val normalizedValue = ((distanceFromLanes!! + 0.5f) / -0.45f).coerceIn(0f, 1f)
                Color(
                    red = normalizedValue,
                    green = 1f - normalizedValue * 0.8f, // Keep some green component even at extreme values
                    blue = 0f,
                    alpha = 1f
                )
            }

            else -> green // Inside lane or right side
        }
    )

    val rightBarColor by animateColorAsState(
        targetValue = when {
            exitPoint == null -> gray // Not exited yet
            laneStartPoint == null -> purple // Exited but no lane start
            distanceFromLanes == null -> green // Lane started but no distance info
            distanceFromLanes!! > 0.5f -> {
                // Compute color gradient from green to red based on distance
                // 0.5 = green, 0.95 = red
                val normalizedValue = ((distanceFromLanes!! - 0.5f) / 0.45f).coerceIn(0f, 1f)
                Color(
                    red = normalizedValue,
                    green = 1f - normalizedValue * 0.8f, // Keep some green component even at extreme values
                    blue = 0f,
                    alpha = 1f
                )
            }

            else -> green // Inside lane or left side
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            // Left vertical bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .requiredWidth(40.dp)
                    .background(leftBarColor, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.requiredWidth(16.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
            Spacer(modifier = Modifier.requiredWidth(16.dp))
            // Right vertical bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .requiredWidth(40.dp)
                    .background(rightBarColor, RoundedCornerShape(16.dp))
            )
        }
        Spacer(modifier = Modifier.requiredHeight(16.dp))
        // Optional: Position indicator showing where user is between lanes
        distanceFromLanes?.let { distance ->
            // Convert distance (-1 to 1) to slider value (0 to 1)
            val sliderPosition = (distance + 1) / 2

            // Calculate color based on proximity to edges
            // Distance closer to 0 means center (green), closer to -1 or 1 means edges (red)
            val proximityToEdge = abs(distance).coerceIn(0f, 1f)
            val thumbColor = if (proximityToEdge > 0.5f) {
                // Gradually transition from green to red as we approach the edge
                val colorRatio = ((proximityToEdge - 0.5f) / 0.5f).coerceIn(0f, 1f)
                Color(
                    red = colorRatio,
                    green = 1f - colorRatio * 0.8f,
                    blue = 0f,
                    alpha = 1f
                )
            } else {
                // Center area is green
                Color.Green
            }

            Slider(
                value = sliderPosition,
                onValueChange = {},
                enabled = false,
                valueRange = 0f..1f,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .requiredHeight(16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = thumbColor,
                    disabledThumbColor = thumbColor,
                    activeTrackColor = Color.DarkGray,
                    inactiveTrackColor = Color.DarkGray,
                    disabledActiveTrackColor = Color.DarkGray,
                    disabledInactiveTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Preview(
    name = "Landscape Preview",
    widthDp = 640,
    heightDp = 360
)
@Composable
fun ThreeOnEachSidePlayerScreenPreview() {
    SideDisplayItemsPlayerScreen(
        controller = FakeSessionController(
            profile = fakeProfile
        ),
        caseNumber = 2
    )
}

@Preview(
    name = "Landscape Preview",
    widthDp = 640,
    heightDp = 360
)
@Composable
fun TwoByTwoGridPlayerScreenPreview() {
    TwoByTwoGridPlayerScreen(
        controller = FakeSessionController(
            profile = fakeProfile
        )
    )
}

@Preview(
    name = "Landscape Preview",
    widthDp = 640,
    heightDp = 360
)
@Composable
fun InlineLeftPlayerScreenPreview() {
    InlinePlayerScreen(
        inlinePlayerDirection = InlinePlayerDirection.Right,
        controller = FakeSessionController(
            profile = fakeProfile.copy(
                displayItems = listOf(
                    DisplayItem(
                        displayableCapability = DisplayableCapability.HorizontalSpeed,
                        caseIndex = 0,
                        indexInCase = 0
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.VerticalSpeed,
                        caseIndex = 0,
                        indexInCase = 1
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.TotalSpeed,
                        caseIndex = 0,
                        indexInCase = 2
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.Altitude,
                        caseIndex = 1,
                        indexInCase = 0
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.Elevation,
                        caseIndex = 1,
                        indexInCase = 1
                    ),
                    DisplayItem(
                        displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                        caseIndex = 1,
                        indexInCase = 2
                    )
                )
            )
        )
    )
}

class FakeSessionController(
    override val profile: SessionProfile
) : PpcHudSessionController {
    override val sessionEvents: SharedFlow<SessionEvent> = MutableSharedFlow()
    override val type: SessionType = SessionType.Hud
    override val performanceLanes: StateFlow<List<PpcHudVideoControllerImpl.PerformanceLine>> =
        MutableStateFlow(emptyList())
    override val gnssFlow: SharedFlow<GnssData> = MutableSharedFlow()
    override val laneStartPoint: StateFlow<GnssData?> = MutableStateFlow(null)
    override val heading: StateFlow<Double> = MutableStateFlow(0.0)
    override val timeMutableSource: TimeMutableSource? = null
    override val deviceState:StateFlow<Pair<Pair<DeviceConnectionState, DeviceMode>, Pair<BatteryLevel, Boolean>>?> =
        MutableStateFlow((DeviceConnectionState.Disconnected to DeviceMode.Active) to (90 to false))
    override val hasFix: StateFlow<Boolean> = MutableStateFlow(true)
    override val videoController: VideoController = fakeVideoController
    override val distanceToCenter: StateFlow<Float?> = MutableStateFlow(null)
    override val referencePointDistances: StateFlow<Map<String, Double>> =
        MutableStateFlow(emptyMap())
    override val timeInWindow: StateFlow<Float> = MutableStateFlow(0f)
    override val distanceInWindow: StateFlow<Int> = MutableStateFlow(0)
    override val speedInWindow: StateFlow<Int> = MutableStateFlow(0)

    override fun pause() {}

    override fun play() {}
    override fun play(callback: SessionController.SessionControllerCallback) {}
    override suspend fun resetDetectors() {}

    override fun destroy() {}
    override val currentFlareState: StateFlow<FlareState> = MutableStateFlow(FlareState.Idle)
    override val registeredFlares: StateFlow<List<Flare>> = MutableStateFlow(emptyList())
    override val exitFound: StateFlow<GnssData?> = MutableStateFlow(null)

}

private val fakeVideoController = object : VideoController {
    override fun destroy() {}
}

private val fakeProfile = SessionProfile.default()
    .copy(
        displayItems = listOf(
            DisplayItem(
                displayableCapability = DisplayableCapability.HorizontalSpeed,
                caseIndex = 0,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.VerticalSpeed,
                caseIndex = 0,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.TotalSpeed,
                caseIndex = 0,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 1,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 1,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 1,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 2,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 2,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 2,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 3,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 3,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 3,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 4,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 4,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 4,
                indexInCase = 2
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Altitude,
                caseIndex = 5,
                indexInCase = 0
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.Elevation,
                caseIndex = 5,
                indexInCase = 1
            ),
            DisplayItem(
                displayableCapability = DisplayableCapability.DistanceToReferencePoint,
                caseIndex = 5,
                indexInCase = 2
            ),
        )
    )


@Composable
private fun PpcHudLockedContent(
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    Column {
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