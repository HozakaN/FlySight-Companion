package fr.hozakan.flysightcompanion.recordsmodule.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory

@Composable
fun RecordDetailMenuActions(
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
fun RecordDetailScreen(
    recordName: String
) {
    val factory = LocalViewModelFactory.current

    val viewModel: RecordDetailViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(recordName) {
        viewModel.loadRecord(recordName)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = "record ${state.recordFile.phoneFilePath}"
                )
                Spacer(modifier = Modifier.requiredHeight(16.dp))
                Text(
                    text = "content length : ${state.content.length}"
                )
            }
        }
    }
}