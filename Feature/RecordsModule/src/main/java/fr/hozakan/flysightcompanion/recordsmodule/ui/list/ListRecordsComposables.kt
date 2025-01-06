package fr.hozakan.flysightcompanion.recordsmodule.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.designsystem.theme.FlySightTheme
import fr.hozakan.flysightcompanion.designsystem.widget.FText
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.records.Record

@Composable
fun ListRecordsMenuActions(
    onCreateConfigFile: () -> Unit
) {
    IconButton(
        onClick = onCreateConfigFile
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
        )
    }
}

@Composable
fun ListRecordsScreen(
    onRecordSelected: (Record) -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: ListRecordsViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        if (state.records.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                FText(
                    text = stringResource(R.string.list_records_no_records),
                    configuration = FlySightTheme.typography.plainScreenTextLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records) { record ->
                    RecordListItem(
                        record = record,
                        onSelected = { onRecordSelected(record) },
                        onDeleteRecordClicked = {
                            viewModel.deleteRecord(record)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordListItem(
    record: Record,
    onSelected: () -> Unit,
    onDeleteRecordClicked: () -> Unit
) {
    Card(
        onClick = onSelected,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(132.dp)
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.phoneFilePath
                )
                Spacer(modifier = Modifier.weight(1f))

                var menuOpened by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        modifier = Modifier.requiredSize(24.dp),
                        onClick = {
                            menuOpened = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(
                                R.string.list_device_item_configuration_menu_content_description
                            )
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpened,
                        onDismissRequest = { menuOpened = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(
                                        R.string.list_records_delete_record
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            },
                            onClick = {
                                menuOpened = false
                                onDeleteRecordClicked()
                            }
                        )
                    }
                }
            }
        }
    }
}

