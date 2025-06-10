package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.ui.PlotBottomItem
import fr.hozakan.flysightcompanion.model.ui.PlotLeftItem

@Composable
fun RecordDetailMenuActions(
    onPlotSettingsClicked: ()  -> Unit
) {

}

@Composable
fun RecordDetailMenuActionsOld(
    onPlotSettingsClicked: ()  -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: RecordDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var leftSubMenuExpanded by remember { mutableStateOf(false) }
    var bottomSubMenuExpanded by remember { mutableStateOf(false) }
    var localAccelerationSubMenuExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            expanded = !expanded
        }
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = stringResource(
                R.string.record_detail_menu
            )
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(text = "Left") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Left submenu"
                )
            },
            onClick = {
                leftSubMenuExpanded = true
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = "Bottom") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Bottom submenu"
                )
            },
            onClick = {
                bottomSubMenuExpanded = true
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.record_detail_menu_plot_settings)) },
            onClick = {
                expanded = false
                onPlotSettingsClicked()
            }
        )
    }
    DropdownMenu(
        expanded = leftSubMenuExpanded,
        onDismissRequest = {
            leftSubMenuExpanded = false
        }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_item_elevation)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.Elevation in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.Elevation)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.Elevation)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_item_horizontal_speed)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.HorizontalSpeed in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.HorizontalSpeed)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.HorizontalSpeed)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_item_vertical_speed)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.VerticalSpeed in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.VerticalSpeed)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.VerticalSpeed)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_total_speed)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.TotalSpeed in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.TotalSpeed)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.TotalSpeed)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_course)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.Course in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.Course)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.Course)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_course_rate)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.CourseRate in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.CourseRate)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.CourseRate)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_course_accuracy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.CourseAccuracy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.CourseAccuracy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.CourseAccuracy)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_glide_ratio)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.GlideRatio in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.GlideRatio)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.GlideRatio)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_dive_angle)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.DiveAngle in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.DiveAngle)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.DiveAngle)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_dive_rate)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.DiveRate in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.DiveRate)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.DiveRate)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_horizontal_accuracy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.HorizontalAccuracy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.HorizontalAccuracy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.HorizontalAccuracy)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_vertical_accuracy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.VerticalAccuracy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.VerticalAccuracy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.VerticalAccuracy)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_speed_accuracy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.SpeedAccuracy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.SpeedAccuracy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.SpeedAccuracy)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_number_of_satellites)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.NumberOfSatellites in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.NumberOfSatellites)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.NumberOfSatellites)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_acceleration)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.Acceleration in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.Acceleration)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.Acceleration)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_acceleration_local)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "Local acceleration submenu"
                )
            },
            onClick = {
                leftSubMenuExpanded = false
                localAccelerationSubMenuExpanded = true
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_total_energy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.TotalEnergy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.TotalEnergy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.TotalEnergy)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_energy_rate)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.EnergyRate in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.EnergyRate)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.EnergyRate)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_lift_coefficient)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.LiftCoefficient in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.LiftCoefficient)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.LiftCoefficient)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_drag_coefficient)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.DragCoefficient in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.DragCoefficient)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.DragCoefficient)
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_wingsuit_sep)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.Wingsuit.SphericalErrorProbability in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.Wingsuit.SphericalErrorProbability)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.Wingsuit.SphericalErrorProbability)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_speed__score_accuracy)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.Speed.SpeedScoreAccuracy in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.Speed.SpeedScoreAccuracy)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.Speed.SpeedScoreAccuracy)
            }
        )
    }
    DropdownMenu(
        expanded = localAccelerationSubMenuExpanded,
        onDismissRequest = {
            localAccelerationSubMenuExpanded = false
        }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.misc_back)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.misc_back)
                )
            },
            onClick = {
                localAccelerationSubMenuExpanded = false
                leftSubMenuExpanded = true
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_local_acceleration_forward)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.LocalAcceleration.AccelerationForward in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationForward)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationForward)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_local_acceleration_right)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.LocalAcceleration.AccelerationRight in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationRight)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationRight)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_local_acceleration_down)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.LocalAcceleration.AccelerationDown in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationDown)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationDown)
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.left_plot_local_acceleration_magnitude)) },
            leadingIcon = {
                Checkbox(
                    checked = PlotLeftItem.LocalAcceleration.AccelerationMagnitude in state.plotLeftItems,
                    onCheckedChange = {
                        viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationMagnitude)
                    }
                )
            },
            onClick = {
                viewModel.updatePlotLeftItems(PlotLeftItem.LocalAcceleration.AccelerationMagnitude)
            }
        )
    }
    DropdownMenu(
        expanded = bottomSubMenuExpanded,
        onDismissRequest = {
            bottomSubMenuExpanded = false
        }
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.bottom_plot_item_time)) },
            leadingIcon = {
                RadioButton(
                    selected = state.plotBottomItem == PlotBottomItem.Time,
                    onClick = {
                        viewModel.updatePlotBottomItems(PlotBottomItem.Time)
                        bottomSubMenuExpanded = false
                    }
                )
            },
            onClick = {
                viewModel.updatePlotBottomItems(PlotBottomItem.Time)
                bottomSubMenuExpanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.bottom_plot_item_horizontal_distance)) },
            leadingIcon = {
                RadioButton(
                    selected = state.plotBottomItem == PlotBottomItem.HorizontalDistance,
                    onClick = {
                        viewModel.updatePlotBottomItems(PlotBottomItem.HorizontalDistance)
                        bottomSubMenuExpanded = false
                    }
                )
            },
            onClick = {
                viewModel.updatePlotBottomItems(PlotBottomItem.HorizontalDistance)
                bottomSubMenuExpanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.bottom_plot_item_total_distance)) },
            leadingIcon = {
                RadioButton(
                    selected = state.plotBottomItem == PlotBottomItem.TotalDistance,
                    onClick = {
                        viewModel.updatePlotBottomItems(PlotBottomItem.TotalDistance)
                        bottomSubMenuExpanded = false
                    }
                )
            },
            onClick = {
                viewModel.updatePlotBottomItems(PlotBottomItem.TotalDistance)
                bottomSubMenuExpanded = false
            }
        )
    }
}