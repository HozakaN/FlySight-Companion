package fr.hozakan.flysightble.fsdevicemodule.ui.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightble.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightble.model.FileState

@Composable
fun FileScreenComposable(
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

        val content = when (state.fileContent) {
            is FileState.Error -> "Error"
            FileState.Loading -> "Loading"
            FileState.Nothing -> "Unknown"
            is FileState.Success -> (state.fileContent as FileState.Success).content
        }
        Text(
            modifier = Modifier.scrollable(
                state = rememberScrollState(),
                orientation = Orientation.Vertical
            ),
            text = content
        )
    }
}