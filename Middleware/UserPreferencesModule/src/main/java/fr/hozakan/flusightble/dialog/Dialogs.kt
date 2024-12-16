package fr.hozakan.flusightble.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightble.composablecommons.SimpleDialogActionBar
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

data class ConfigFileName(val name: String) : DialogResult

data object ConfigFileNameDialog : DialogItem {

    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            var configFileName by remember { mutableStateOf("") }
            var isDirty by remember { mutableStateOf(false) }
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = configFileName,
                        onValueChange = {
                            configFileName = it
                            isDirty = true
                        },
                        label = {
                            Text(text = "Config name")
                        },
                        isError = isDirty && configFileName.isBlank()
                    )
                    Spacer(modifier = Modifier.requiredHeight(8.dp))
                    SimpleDialogActionBar(
                        onDismissRequest = {
                            onResult(DialogResult.Dismiss)
                        },
                        onValidate = {
                            onResult(ConfigFileName(configFileName))
                        }
                    )
                }
            }
//        }
        }

    }
}