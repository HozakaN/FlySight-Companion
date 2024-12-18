package fr.hozakan.flysightble.composablecommons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SimpleDialogActionBar(
    onDismissRequest: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
    showValidateButton: Boolean = true,
    validateEnabled: Boolean = true,
    validateButtonText: String = stringResource(id = R.string.misc_save).uppercase(),
    showCancelButton: Boolean = true,
    cancelButtonText: String = stringResource(id = R.string.misc_cancel).uppercase()
) {
    Row(
        modifier = modifier
            .requiredHeight(64.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (showCancelButton) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = cancelButtonText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (showValidateButton) {
            TextButton(
                onClick = onValidate, enabled = validateEnabled
            ) {
                Text(
                    text = validateButtonText,
                    color = if (validateEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun SimpleDialogActionBar3Button(
    onDismissRequest: () -> Unit,
    onNeutral: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
    showValidateButton: Boolean = true,
    validateEnabled: Boolean = true,
    validateButtonText: String = stringResource(id = R.string.misc_save).uppercase(),
    showNeutralButton: Boolean = true,
    neutralButtonEnabled: Boolean = true,
    neutralButtonText: String = stringResource(id = R.string.misc_neutral).uppercase(),
    showCancelButton: Boolean = true,
    cancelButtonText: String = stringResource(id = R.string.misc_cancel).uppercase()
) {
    if (!showNeutralButton) {
        SimpleDialogActionBar(
            onDismissRequest = onDismissRequest,
            onValidate = onValidate,
            modifier = modifier,
            showValidateButton = showValidateButton,
            validateEnabled = validateEnabled,
            validateButtonText = validateButtonText,
            showCancelButton = showCancelButton,
            cancelButtonText = cancelButtonText
        )
    } else {
        Row(
            modifier = modifier
                .requiredHeight(64.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            if (showCancelButton) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(
                        text = cancelButtonText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            TextButton(
                onClick = onNeutral
            ) {
                Text(
                    text = neutralButtonText,
                    color = if (neutralButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
            if (showValidateButton) {
                TextButton(
                    onClick = onValidate, enabled = validateEnabled
                ) {
                    Text(
                        text = validateButtonText,
                        color = if (validateEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}


@Composable
fun SimpleVerticalDialogActionBar(
    onDismissRequest: () -> Unit,
    onNeutral: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
    showValidateButton: Boolean = true,
    validateEnabled: Boolean = true,
    validateButtonText: String = stringResource(id = R.string.misc_save).uppercase(),
    showNeutralButton: Boolean = true,
    neutralButtonEnabled: Boolean = true,
    neutralButtonText: String = stringResource(id = R.string.misc_neutral).uppercase(),
    showCancelButton: Boolean = true,
    cancelButtonText: String = stringResource(id = R.string.misc_cancel).uppercase()
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        if (showCancelButton) {
            TextButton(
                modifier = Modifier.requiredHeight(32.dp),
                onClick = onDismissRequest
            ) {
                Text(
                    text = cancelButtonText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (showNeutralButton) {
            TextButton(
                modifier = Modifier.requiredHeight(32.dp),
                onClick = onNeutral,
                enabled = neutralButtonEnabled
            ) {
                Text(
                    text = neutralButtonText,
                    color = if (neutralButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
        if (showValidateButton) {
            TextButton(
                modifier = Modifier.requiredHeight(32.dp),
                onClick = onValidate, enabled = validateEnabled
            ) {
                Text(
                    text = validateButtonText,
                    color = if (validateEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
