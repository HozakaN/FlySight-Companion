package fr.hozakan.flysightcompanion.recordsmodule.ui.plot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.ui.PlotDisplayPreference
import fr.hozakan.flysightcompanion.model.ui.accelerationColor
import fr.hozakan.flysightcompanion.model.ui.accelerationDownColor
import fr.hozakan.flysightcompanion.model.ui.accelerationForwardColor
import fr.hozakan.flysightcompanion.model.ui.accelerationMagnitudeColor
import fr.hozakan.flysightcompanion.model.ui.accelerationRightColor
import fr.hozakan.flysightcompanion.model.ui.courseAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.courseColor
import fr.hozakan.flysightcompanion.model.ui.courseRateColor
import fr.hozakan.flysightcompanion.model.ui.diveAngleColor
import fr.hozakan.flysightcompanion.model.ui.diveRateColor
import fr.hozakan.flysightcompanion.model.ui.dragCoefficientColor
import fr.hozakan.flysightcompanion.model.ui.elevationColor
import fr.hozakan.flysightcompanion.model.ui.energyRateColor
import fr.hozakan.flysightcompanion.model.ui.glideRatioColor
import fr.hozakan.flysightcompanion.model.ui.horizontalAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.horizontalSpeedColor
import fr.hozakan.flysightcompanion.model.ui.liftCoefficientColor
import fr.hozakan.flysightcompanion.model.ui.numberOfSatellitesColor
import fr.hozakan.flysightcompanion.model.ui.speedAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.sphericalErrorProbabilityColor
import fr.hozakan.flysightcompanion.model.ui.totalEnergyColor
import fr.hozakan.flysightcompanion.model.ui.totalSpeedColor
import fr.hozakan.flysightcompanion.model.ui.verticalAccuracyColor
import fr.hozakan.flysightcompanion.model.ui.verticalSpeedColor
import androidx.core.graphics.toColorInt

@Composable
fun PlotSettingsScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: PlotSettingsViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        var selectedParam by remember { mutableStateOf<PlotDisplayPreference?>(null) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_item_elevation)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.elevationColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.Elevation }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_item_horizontal_speed)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.horizontalSpeedColor)),
                                shape = CircleShape
                            )
                            .clickable {
//                                selectedParam = state.plotDisplayPreferences.horizontalSpeedColor
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.HorizontalSpeed }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_item_vertical_speed)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.verticalSpeedColor)),
                                shape = CircleShape
                            )
                            .clickable {
//                                selectedParam = state.plotDisplayPreferences.verticalSpeedColor
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.VerticalSpeed }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_total_speed)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.totalSpeedColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.TotalSpeed }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_course)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.courseColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.Course }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_course_rate)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.courseRateColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.CourseRate }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_course_accuracy)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.courseAccuracyColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.CourseAccuracy }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_glide_ratio)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.glideRatioColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.GlideRatio }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_dive_angle)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.diveAngleColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.DiveAngle }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_dive_rate)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.diveRateColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.DiveRate }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_horizontal_accuracy)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.horizontalAccuracyColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.HorizontalAccuracy }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_vertical_accuracy)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.verticalAccuracyColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.VerticalAccuracy }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_speed_accuracy)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.speedAccuracyColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.SpeedAccuracy }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_number_of_satellites)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.numberOfSatellitesColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.NumberOfSatellites }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_acceleration)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.accelerationColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.Acceleration }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_local_acceleration_forward)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.accelerationForwardColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.AccelerationForward }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_local_acceleration_right)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.accelerationRightColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.AccelerationRight }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_local_acceleration_down)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.accelerationDownColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.AccelerationDown }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_local_acceleration_magnitude)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.accelerationMagnitudeColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.AccelerationMagnitude }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_total_energy)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.totalEnergyColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.TotalEnergy }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_energy_rate)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.energyRateColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.EnergyRate }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_lift_coefficient)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.liftCoefficientColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.LiftCoefficient }
                            }
                    )
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_drag_coefficient)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(state.plotDisplayPreferences.dragCoefficientColor)),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.DragCoefficient }
                            }
                    )
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.left_plot_wingsuit_sep)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .requiredSize(24.dp)
                            .background(
                                color = Color(state.plotDisplayPreferences.sphericalErrorProbabilityColor.toColorInt()),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedParam = state.plotDisplayPreferences.first { it is PlotDisplayPreference.SphericalErrorProbability }
                            }
                    )
                }
            }
        }
        selectedParam?.let {
            PlotColorPickerDialog(
                currentColor = Color(it.colorHex.toColorInt()),
                onDismissRequest = { selectedParam = null },
                onColorSelected = { color ->
                    viewModel.updatePlotColor(it, color)
                    selectedParam = null
//                    viewModel.updateLeftPlotItemColor(it, color)
                }
            )
        }
    }
}