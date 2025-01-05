package fr.hozakan.flysightcompanion.fsdevicemodule.ui.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.designsystem.R
import fr.hozakan.flysightcompanion.model.FileState

@Composable
fun DeviceFileScreen(
    deviceId: String,
    filePath: String,
    onNavigateUp: () -> Unit
) {
    val factory = LocalViewModelFactory.current

    val viewModel: FileScreenViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(deviceId, filePath) {
        viewModel.init(deviceId, filePath)
    }

    BackHandler(enabled = true) {
        onNavigateUp()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {

        val content = when (val fileContent = state.fileContent) {
            is FileState.Error -> stringResource(R.string.misc_error)
            FileState.Loading -> stringResource(R.string.misc_loading)
            FileState.Nothing -> stringResource(R.string.misc_unknown)
            is FileState.Success -> fileContent.content
        }
        LazyColumn {
            item {
                Text(text = content)
            }
        }
    }
}