package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.hozakan.flysightcompanion.framework.compose.LocalViewModelFactory
import fr.hozakan.flysightcompanion.model.session.profile.SessionType
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.flyblind.FlyBlindSessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.plane_display.PlaneDisplaySessionController
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.ppc.PpcHudSessionController

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionPlayerScreen() {
    val factory = LocalViewModelFactory.current

    val viewModel: SessionPlayerViewModel = viewModel(factory = factory)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.lockDisplay()
    }

//    Scaffold { paddingValues ->

        Surface(
            modifier = Modifier
                .fillMaxSize(),
//                .windowInsetsPadding(WindowInsets.displayCutout),
//                .padding(8.dp),
//                .padding(paddingValues),
            color = MaterialTheme.colorScheme.surface
        ) {
            SessionPlayerScreenInternal(
                state = state,
                onExitClicked = {
                    viewModel.onExitClicked()
                },
                resetExitDetection = {
                    viewModel.resetExitDetection()
                }
            )
//        }
    }
}

@Composable
private fun SessionPlayerScreenInternal(
    state: SessionPlayerState,
    onExitClicked: () -> Unit,
    resetExitDetection: () -> Unit
) {
    val sessionController = state.controller
    if (sessionController == null) return
    when (sessionController.type) {
        SessionType.Hud -> HudSessionPlayer(
            controller = sessionController as PpcHudSessionController,
            onExitClicked = onExitClicked,
            resetExitDetection = resetExitDetection
        )
        SessionType.PlaneDisplay -> PlaneDisplaySessionPlayer(
            controller = sessionController as PlaneDisplaySessionController,
            onExitClicked = onExitClicked
        )

        SessionType.FlyBlind -> FlyBlindSessionPlayer(
            controller = sessionController as FlyBlindSessionController,
            onExitClicked = onExitClicked,
            resetExitDetection = resetExitDetection
        )
        SessionType.SpaceInvaders -> {}
        SessionType.FlyToDraw -> {}
    }

}
