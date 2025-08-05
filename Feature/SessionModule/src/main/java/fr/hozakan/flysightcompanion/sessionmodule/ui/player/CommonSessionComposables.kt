package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.hozakan.flysightcompanion.composablecommons.DropdownContainer
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.math.meterSecondToKmHour
import fr.hozakan.flysightcompanion.model.session.profile.ReferencePoint
import fr.hozakan.flysightcompanion.model.ui.SpeedOrientation
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.source.TimeMutableSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun OrientableArrow(
    modifier: Modifier = Modifier,
    logoSize: Dp = 24.dp,
    heading: Double,
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
                modifier = Modifier.requiredSize(logoSize),
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "",
                tint = Color.Green,
            )
        }
    }
}

@Composable
internal fun SpeedContainer(
    orientation: SpeedOrientation,
    player: SessionController,
    suffix: String = ""
) {
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
            text = "${
                when (orientation) {
                    SpeedOrientation.Horizontal -> remember(gnssData?.gSpeed) { gnssData?.gSpeed?.meterSecondToKmHour() }
                    SpeedOrientation.Vertical -> remember(gnssData?.velD) { gnssData?.velD?.meterSecondToKmHour() }
                    SpeedOrientation.Total -> remember(gnssData?.speed) { gnssData?.speed?.meterSecondToKmHour() }
                }
            }",
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )

        FText(
            text = " $suffix",
            configuration = FlySightTheme.typography.sessionPlayerText,
            color = Color.Green
        )
    }
}

@Composable
internal fun TagAndValueContainer(tag: String, value: String, suffix: String = "") {
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
            configuration = FlySightTheme.typography.sessionPlayerValue,
            color = Color.Green
        )
        if (suffix.isNotBlank()) {
            FText(
                text = suffix,
                configuration = FlySightTheme.typography.sessionPlayerText,
                color = Color.Green
            )
        }
    }
}

@Composable
internal fun ReferencePointSelector(
    referencePoints: List<ReferencePoint>,
    selectedReferencePoint: ReferencePoint?,
    onReferencePointSelected: (ReferencePoint?) -> Unit
) {
    val selectableValues = remember(referencePoints) {
        referencePoints.map { it.name }
    }

    DropdownContainer(
        label = "Reference Point",
        selectedValue = selectedReferencePoint?.name ?: "Select a reference point",
        options = selectableValues,
        onSelectionChanged = { newSelection ->
            onReferencePointSelected(referencePoints.firstOrNull { it.name == newSelection })
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun LockContainer(
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

class LockManagerScope(
    /**
     * For surfaces suppressing click support, helps propagate clicks to the the LockManagerContainer
     */
    val onClick: () -> Unit
)

@Composable
internal fun LockManagerContainer(
    modifier: Modifier = Modifier,
    lockedContent: @Composable () -> Unit,
    content: @Composable LockManagerScope.() -> Unit
) {

    var uiLocked by remember { mutableStateOf(true) }

    var counter by remember { mutableIntStateOf(0) }
    var uiTouched by remember { mutableStateOf(false) }

    var displayUnlockUi by remember { mutableStateOf(false) }

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

    val scope = remember {
        LockManagerScope { counter++ }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    if (!displayUnlockUi) {
                        counter++
                    }
                }
            }
    ) {
        content(scope)

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
            lockedContent()
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
internal fun TimeControlContainer(
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
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
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
                Spacer(modifier = Modifier.requiredWidth(8.dp))
                val paused by timeMutableSource.paused.collectAsState()
                Box(
                    modifier = Modifier
                        .requiredSize(56.dp)
                        .clickable {
                            if (paused) {
                                timeMutableSource.start()
                            } else {
                                timeMutableSource.pause()
                            }
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (paused) {
                            Icons.Default.PlayArrow
                        } else {
                            Icons.Default.Pause
                        },
                        contentDescription = ""
                    )
                }
            }
            Text(text = String.format("%.2f", sliderPosition))
        }
    }
}