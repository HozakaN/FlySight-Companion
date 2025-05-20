package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData
import fr.hozakan.flysightcompanion.model.config.AlarmType
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayGrid
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItem
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayItemBundle
import fr.hozakan.flysightcompanion.model.session.configuration.DisplayableCapability
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.ui.SpeedOrientation
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionEvent
import fr.hozakan.flysightcompanion.sessionmodule.business.player.TimeMutableSource
import fr.hozakan.flysightcompanion.sessionmodule.business.player.VideoController
import fr.hozakan.flysightcompanion.sessionmodule.business.player.VideoControllerImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.collections.emptyMap
import kotlin.math.abs

@Composable
fun SessionPlayerMenuActions(
//    onCreatePlayFile: () -> Unit
) {
    IconButton(
        onClick = {} //onCreatePlayFile
    ) {
//        Icon(
//            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
//            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
//        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionPlayerScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionPlayerViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.lockDisplay()
    }

    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout)
                .padding(8.dp)
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface
        ) {
            SessionPlayerScreenInternal(
                state = state,
                onExitClicked = {
                    viewModel.onExitClicked()
                },
                resetExitDetection = {
                    viewModel.resetExitDetection()
                }
            )
        }
    }
}

@Composable
private fun SessionPlayerScreenInternal(
    state: SessionPlayerState,
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    val player = state.controller
    if (player == null) return
    val displayGrid = player.profile.displayGrid
    var uiLocked by remember { mutableStateOf(true) }

    Column {
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
                    controller = player
                )

                DisplayGrid.InlineRight -> InlinePlayerScreen(
                    inlinePlayerDirection = InlinePlayerDirection.Right,
                    controller = player
                )

                DisplayGrid.TwoByTwo -> InlinePlayerScreen(
                    inlinePlayerDirection = InlinePlayerDirection.Left,
                    controller = player
                )

                DisplayGrid.TwoOnEachSide -> SideDisplayItemsPlayerScreen(
                    controller = player,
                    caseNumber = 2
                )

                DisplayGrid.ThreeOnEachSide -> SideDisplayItemsPlayerScreen(
                    controller = player,
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
                LockedContent(
                    onExitClicked = onExitClicked,
                    resetExitDetection = resetExitDetection
                )
            }
        }
        val timeMutableSource = player.timeMutableSource
        timeMutableSource?.let { source ->
            TimeControlContainer(
                modifier = Modifier/*.weight(1f)*/,
                timeMutableSource = source
            )
        }
    }
}

@Composable
private fun LockedContent(
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

@Composable
fun LockContainer(
    locked: Boolean,
    onUnlocked: () -> Unit,
    onUiTouchChanged: (Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {

        val scope = rememberCoroutineScope()
        val alphaPointerAnimatable = remember { Animatable(0.5f) }
        val alphaCursiveAnimatable = remember { Animatable(0f) }

        val translationX = remember { Animatable(0f) }
        val containerWidth = with(LocalDensity.current) {
            maxWidth.toPx()
        }

        //Cursive
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val color = remember(surfaceVariant) { surfaceVariant.copy(alpha = 0.5f) }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alphaCursiveAnimatable.value
                }
                .requiredSize(height = 60.dp, width = maxWidth),
            shape = RoundedCornerShape(120.dp),
            color = color,
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Slide to unlock",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        //Pointer
        Surface(
            modifier = Modifier
                .requiredSize(60.dp)
                .graphicsLayer {
                    this.alpha = alphaPointerAnimatable.value
                    this.translationX = translationX.value
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            scope.launch {
                                launch {
                                    alphaPointerAnimatable.animateTo(1f)
                                }
                                launch {
                                    alphaCursiveAnimatable.animateTo(1f)
                                }
                            }
                            onUiTouchChanged(true)
                        },
                        onDragEnd = {
                            scope.launch {
                                if (translationX.value >= containerWidth - 60.dp.toPx()) {
                                    onUnlocked()
                                }
                                launch {
                                    alphaPointerAnimatable.animateTo(0.5f)
                                }
                                launch {
                                    //Do it in sequence
                                    translationX.animateTo(0f)
                                    alphaCursiveAnimatable.animateTo(0f)
                                }
                            }
                            onUiTouchChanged(false)
                        },
                        onDragCancel = {
                            scope.launch {
                                launch {
                                    alphaPointerAnimatable.animateTo(0.5f)
                                }
                                launch {
                                    //Do it in sequence
                                    translationX.animateTo(0f)
                                    alphaCursiveAnimatable.animateTo(0f)
                                }
                            }
                            onUiTouchChanged(false)
                        },
                        onDrag = { _, dragAmount ->
                            scope.launch {
                                translationX.snapTo(
                                    (translationX.value + dragAmount.x).coerceAtLeast(
                                        0f
                                    ).coerceAtMost(containerWidth - 60.dp.toPx())
                                )
                            }
                        }
                    )
                },
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.Red)
        ) {
            Icon(
                modifier = Modifier.requiredSize(40.dp),
                imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = ""
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun TimeControlContainer(
    modifier: Modifier = Modifier,
    timeMutableSource: TimeMutableSource
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val sliderPosition by timeMutableSource.currentTime.collectAsState()
        val startValue by timeMutableSource.startValue.collectAsState()
        val endValue by timeMutableSource.endValue.collectAsState()
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = {
                    timeMutableSource.moveTo(it)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                onValueChangeFinished = {
                    timeMutableSource.start()
                },
                valueRange = startValue..endValue
            )
            Text(text = String.format("%.2f", sliderPosition))
        }
    }
}

@Composable
private fun TwoByTwoGridPlayerScreen(
    controller: SessionController
) {
    val displayItems = controller.profile.displayItems
    Row(
//        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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
    controller: SessionController,
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
    player: SessionController
) {
    val gnssData: GnssData? by player.gnssFlow.collectAsState(initial = null)
    when (item.displayableCapability) {
        DisplayableCapability.HorizontalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Horizontal,
            player = player
        )

        DisplayableCapability.VerticalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Vertical,
            player = player
        )

        DisplayableCapability.TotalSpeed -> SpeedContainer(
            orientation = SpeedOrientation.Total,
            player = player
        )

        DisplayableCapability.Elevation -> TagAndValueContainer(
            tag = "Elv",
            value = "${gnssData?.hMsl?.minus(config.dzElev)}"
        )

        DisplayableCapability.Altitude -> TagAndValueContainer(
            tag = "Alt",
            value = "${gnssData?.hMsl}"
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
                value = if (distance != null) String.format("%.2f NM", distance) else "-- NM"
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
            value = "${gnssData?.lon}"
        )

        DisplayableCapability.InverseGlideRatio -> TagAndValueContainer(
            tag = "IGR",
            value = "${gnssData?.lon}"
        )

        DisplayableCapability.DiveAngle -> TagAndValueContainer(
            tag = "DiveA",
            value = "${gnssData?.lon}"
        )
    }
}

@Composable
private fun TagAndValueContainer(tag: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FText(
            text = tag,
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        FText(
            text = value,
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
    }
}

@Composable
private fun SpeedContainer(orientation: SpeedOrientation, player: SessionController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "",
            modifier = Modifier.graphicsLayer {
                rotationZ = when (orientation) {
                    SpeedOrientation.Horizontal -> 0f
                    SpeedOrientation.Vertical -> 90f
                    SpeedOrientation.Total -> 45f
                }
            },
            tint = Color.Green
        )
        val gnssData by player.gnssFlow.collectAsState(initial = null)

        FText(
            text = "${when (orientation) {
                SpeedOrientation.Horizontal -> gnssData?.gSpeed
                SpeedOrientation.Vertical -> 90f
                SpeedOrientation.Total -> gnssData?.speed
            }} km/h",
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
    }
}

enum class InlinePlayerDirection {
    Left,
    Right
}

@Composable
private fun InlinePlayerScreen(
    controller: SessionController,
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
    controller: SessionController
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
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (controller.profile.showPerformanceLane) {
            PerformanceLaneContainer(controller) {
                if (controller.profile.showMap) {
                    GMapContainer(controller)
                }
            }
        } else if (controller.profile.showMap) {
            GMapContainer(controller)
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
private fun GMapContainer(sessionController: SessionController) {
    val gnssData by sessionController.gnssFlow.collectAsState(initial = null)

    val cameraPositionState = rememberCameraPositionState()


    val perfLanes by sessionController.performanceLanes.collectAsState()

    val gpsData = gnssData
    val cameraZoom by animateFloatAsState(
        targetValue = if (sessionController.exitDetected.value == null || (gpsData != null && sessionController.profile.competitionWindowBottom > gpsData.hMsl)) 13f else 16f,
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
    sessionController: SessionController,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val exitPoint by sessionController.exitDetected.collectAsState()
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
) : SessionController {
    override val sessionEvents: SharedFlow<SessionEvent> = MutableSharedFlow()
    override val performanceLanes: StateFlow<List<VideoControllerImpl.PerformanceLine>> =
        MutableStateFlow(emptyList())
    override val gnssFlow: SharedFlow<GnssData> = MutableSharedFlow()
    override val exitDetected: StateFlow<GnssData?> = MutableStateFlow(null)
    override val laneStartPoint: StateFlow<GnssData?> = MutableStateFlow(null)
    override val timeMutableSource: TimeMutableSource? = null
    override val videoController: VideoController = fakeVideoController
    override val distanceToCenter: StateFlow<Float?> = MutableStateFlow(null)
    override val referencePointDistances: StateFlow<Map<String, Double>> =
        MutableStateFlow(emptyMap())

    override fun pause() {}

    override fun play() {}
    override fun play(callback: SessionController.SessionControllerCallback) {}
    override fun resetExitDetection() {}

    override fun destroy() {}

}

private val fakeVideoController = object : VideoController {}

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


