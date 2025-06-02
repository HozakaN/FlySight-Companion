package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController

@Composable
fun PlaneDisplaySessionPlayer(
    controller: SessionController,
    onExitClicked: () -> Unit
) {
    var hMSL by remember { mutableIntStateOf(0) }
    var dzElev by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        controller
            .gnssFlow
            .collect { data ->
                hMSL = data.hMsl - dzElev
            }
    }


}