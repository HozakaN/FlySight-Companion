package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.hozakan.flysightcompanion.designsystem.theme.TextConfiguration
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.ACRO_MAX_EXIT_HEIGHT
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.ACRO_MIN_EXIT_HEIGHT
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.PERF_MAX_EXIT_HEIGHT
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.PERF_MIN_EXIT_HEIGHT
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.acroSliderRange
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController.Companion.perfSliderRange
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PlaneDisplaySessionPlayer(
    controller: PlaneDisplaySessionController,
    onExitClicked: () -> Unit
) {

    val elevation by controller.elevation.collectAsState()
    val dzElevation by controller.dzElevation.collectAsState()
    var showDzElevationDialog by remember { mutableStateOf(false) }

    // Calculate slider value based on elevation
    val sliderValue = remember(elevation) {
        val cappedElevation = min(3600f, max(2900f, elevation?.toFloat() ?: 2900f))
        cappedElevation
    }

    val discipline by controller.discipline.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {


        val colorBlindOption by controller.colorBlindOption.collectAsState()
        val currentMaxHeight = maxHeight
        // Main content with slider on left
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Button(
                    onClick = onExitClicked
                ) {
                    Text(text = "Exit")
                }
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = colorBlindOption,
                        onCheckedChange = { enabled ->
                            controller.updateColorBlindOption(enabled)
                        }
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Text("Color blind")
                }
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = discipline == 1,
                        onCheckedChange = { isAcro ->
                            controller.updateDiscipline(if (isAcro) 1 else 0)
                        }
                    )
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    if (discipline == 0) {
                        Text(text = "Perf")
                    } else {
                        Text(text = "Acro")
                    }
                }
            }
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val shouldJump by remember(elevation, discipline) {
                    mutableStateOf(
                        when (discipline) {
                            0 -> elevation in PERF_MIN_EXIT_HEIGHT..PERF_MAX_EXIT_HEIGHT
                            else -> elevation in ACRO_MIN_EXIT_HEIGHT..ACRO_MAX_EXIT_HEIGHT
                        }
                    )
                }

                var textColor by remember { mutableStateOf(Color.Green) }

                LaunchedEffect(colorBlindOption) {
                    while (true) {
                        val isJumpable = when (discipline) {
                            0 -> elevation in PERF_MIN_EXIT_HEIGHT..PERF_MAX_EXIT_HEIGHT
                            else -> elevation in ACRO_MIN_EXIT_HEIGHT..ACRO_MAX_EXIT_HEIGHT
                        }
                        textColor = if (isJumpable) {
                            if (colorBlindOption) {
                                if (textColor == Color.Green) Color(0xFF4D85BD) // Colorblind-friendly alternative to green
                                else Color.Green
                            } else {
                                Color.Green
                            }
                        } else {
                            if (colorBlindOption) {
                                if (textColor == Color.Red) Color(0xFFFF8800) // Colorblind-friendly alternative to red
                                else Color.Red
                            } else {
                                Color.Red
                            }
                        }
                        delay(1_000L)
                    }
                }

                // Colorblind-friendly version
                FText(
                    text = if (shouldJump) "Jump!" else "Don\'t jump!",
//                    color = if (shouldJump) Color.Green else Color.Red,
                    color = textColor,
                    configuration = TextConfiguration.Default.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (colorBlindOption) {
                    // Colorblind-friendly version
                    FText(
                        text = "$elevation meters",
                        color = Color(0xFF4D85BD), // Colorblind-friendly alternative to green
                        configuration = TextConfiguration.Default.copy(
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Large elevation with regular green
                FText(
                    text = "$elevation meters",
                    color = Color.Green,
                    configuration = TextConfiguration.Default.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.requiredHeight(24.dp))

                // Drop zone elevation row with edit button
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FText(
                        text = "DZ Elevation: $dzElevation m",
                        configuration = TextConfiguration.Default.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.requiredWidth(8.dp))

                    IconButton(
                        onClick = {
                            showDzElevationDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change DZ Elevation"
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f)
            )
        }
    }

// Dialog for changing DZ elevation
    if (showDzElevationDialog) {

        var tempDzElevation by remember(dzElevation) { mutableStateOf("$dzElevation") }

        AlertDialog(
            onDismissRequest = { showDzElevationDialog = false },
            title = {
                FText(
                    text = "Set Drop Zone Elevation",
                    configuration = TextConfiguration.Default.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempDzElevation,
                        onValueChange = { newValue ->
                            // Only allow numeric input
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                tempDzElevation = newValue
                            }
                        },
                        label = {
                            FText(
                                text = "Elevation (m)",
                                configuration = TextConfiguration.Default
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempDzElevation.isNotEmpty()) {
                            controller.updateDzElev(tempDzElevation.toInt())
                        }
                        showDzElevationDialog = false
                    }
                ) {
                    FText(text = "Save", configuration = TextConfiguration.Default)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDzElevationDialog = false }
                ) {
                    FText(text = "Cancel", configuration = TextConfiguration.Default)
                }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }
}