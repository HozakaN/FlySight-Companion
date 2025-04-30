package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory

@Composable
fun SessionPlayerMenuActions(
//    onCreatePlayFile: () -> Unit
) {
    IconButton(
        onClick = {} //onCreatePlayFile
    ) {
//        Icon(
//            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
//            contentDescription = stringResource(R.string.list_config_file_menu_action_new_config_file_content_description)
//        )
    }
}

@Composable
fun SessionPlayerScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionPlayerViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {


    }
}