package fr.hozakan.flysightcompanion.dialogmodule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.hozakan.flysightcompanion.composablecommons.SimpleDialogActionBar
import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.defaultConfigFile

data class ConfigFileName(val name: String) : DialogResult
data class PickConfigurationDialogResult(val configFile: ConfigFile) : DialogResult

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
                        validateEnabled = configFileName.isNotBlank(),
                        onValidate = {
                            onResult(ConfigFileName(configFileName))
                        }
                    )
                }
            }
        }

    }
}

data class PickConfigurationDialog(
    val configProvider: () -> List<ConfigFile>
) : DialogItem {
    @Composable
    override fun Content(onResult: (DialogResult) -> Unit) {
        Dialog(
            onDismissRequest = {
                onResult(DialogResult.Dismiss)
            }
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Pick a new configuration for your FlySight",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.requiredHeight(16.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(configProvider()) { configFile ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(40.dp)
                                    .clickable { onResult(PickConfigurationDialogResult(configFile)) }
                                    .padding(start = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = configFile.name
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PickConfigurationDialogPreview() {
    PickConfigurationDialog {
        listOf(
            defaultConfigFile().copy(name = "Config 1"),
            defaultConfigFile().copy(name = "Config 2"),
            defaultConfigFile().copy(name = "Config 3"),
            defaultConfigFile().copy(name = "Config 4"),
        )
    }.Content {}
}