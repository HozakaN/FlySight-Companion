package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.runtime.Stable
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController

@Stable
data class SessionPlayerState(
    val controller: SessionController?
)