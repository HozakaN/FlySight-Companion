package fr.hozakan.flysightcompanion.composablecommons

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * This composable handles empty values.
 * It uses the onValueChanged callback with a null value
 * when the coontent of the textfield is empty
 */
@Composable
fun EmptyIntTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    intValue: Int?,
    onValueChanged: (Int?) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        enabled = enabled,
        value = intValue?.toString() ?: "",
        onValueChange = {
            if (it.isEmpty()) {
                onValueChanged(null)
            } else {
                it.toIntOrNull()?.let { content ->
                    onValueChanged(content)
                }
            }
        },
        label = {
            Text(text = label)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Decimal
        )
    )
}