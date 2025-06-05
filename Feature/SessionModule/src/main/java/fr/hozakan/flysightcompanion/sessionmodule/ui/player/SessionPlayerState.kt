package fr.hozakan.flysightcompanion.sessionmodule.ui.player

import androidx.compose.runtime.Stable
import fr.hozakan.flysightcompanion.sessionmodule.business.controller.SessionController

@Stable
data class SessionPlayerState(
    val controller: SessionController?
)