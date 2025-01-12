package fr.hozakan.flysightcompanion.recordsmodule.ui.plot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.CustomColors
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.recordsmodule.ui.hexCode
import timber.log.Timber

@Composable
fun PlotColorPickerDialog(
    currentColor: Color,
    availableColors: List<Color> = listOf(
        CustomColors.PlotColor1,
        CustomColors.PlotColor2,
        CustomColors.PlotColor3,
        CustomColors.PlotColor4,
        CustomColors.PlotColor5,
        CustomColors.PlotColor6,
        CustomColors.PlotColor7,
        CustomColors.PlotColor8,
        CustomColors.PlotColor9,
        CustomColors.PlotColor10,
        CustomColors.PlotColor11,
        CustomColors.PlotColor12,
        CustomColors.PlotColor13,
        CustomColors.PlotColor14,
        CustomColors.PlotColor15
    ),
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {

    var selectedColor by remember(currentColor) { mutableStateOf(currentColor) }
    var customPickerOpened by remember { mutableStateOf(false) }
    var customColorSelected by remember { mutableStateOf(false) }

    if (!customPickerOpened) {
        Dialog(
            onDismissRequest = onDismissRequest
        ) {
            val textColor = LocalContentColor.current
            Card {

                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        top = 48.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FText(
                            text = stringResource(R.string.color_picker_current_choice),
                            configuration = FlySightTheme.typography.cardTitle
                        )
                        Spacer(modifier = Modifier.requiredWidth(16.dp))
                        Box(
                            modifier = Modifier
                                .requiredSize(36.dp)
                                .background(
                                    color = currentColor,
                                    shape = CircleShape
                                )
                        )
                    }
                    Spacer(modifier = Modifier.requiredHeight(40.dp))
                    Row {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[0],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[0]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[0]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[1],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[1]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[1]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[2],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[2]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[2]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[3],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[3]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[3]
                                        customColorSelected = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(40.dp))
                    Row {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[4],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[4]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[4]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[5],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[5]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[5]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[6],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[6]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[6]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[7],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[7]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[7]
                                        customColorSelected = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(40.dp))
                    Row {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[8],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[8]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[8]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[9],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[9]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[9]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[10],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[10]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[10]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[11],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[11]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[11]
                                        customColorSelected = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.requiredHeight(40.dp))
                    Row {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[12],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[12]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[12]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[13],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[13]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[13]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .background(
                                        color = availableColors[14],
                                        shape = CircleShape
                                    )
                                    .borderIf(
                                        width = 2.dp,
                                        color = textColor,
                                        shape = CircleShape
                                    ) {
                                        selectedColor == availableColors[14]
                                    }
                                    .clickable {
                                        selectedColor = availableColors[14]
                                        customColorSelected = false
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (customColorSelected) selectedColor else textColor,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        customPickerOpened = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = stringResource(R.string.color_picker_custom_color),
                                    tint = if (customColorSelected) selectedColor else textColor
                                )
                            }
                        }
                    }
                    SimpleDialogActionBar(
                        onCancel = onDismissRequest,
                        showCancelButton = selectedColor != currentColor,
                        validateButtonText = if (selectedColor == currentColor) {
                            stringResource(R.string.misc_ok).uppercase()
                        } else {
                            stringResource(
                                R.string.misc_save
                            ).uppercase()
                        },
                        onValidate = {
                            if (selectedColor != currentColor) {
                                onColorSelected(selectedColor)
                            } else {
                                onDismissRequest()
                            }
                        }
                    )
                }
            }
        }
    } else {
        CustomColorPickerDialog(
            initialColor = selectedColor,
            onDismissRequest = { customPickerOpened = false },
            onColorSelected = {
                selectedColor = it
                customColorSelected = true
                customPickerOpened = false
            }
        )
    }
}

@Stable
fun Modifier.borderIf(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape,
    condition: () -> Boolean
) = if (condition()) border(width, SolidColor(color), shape) else this