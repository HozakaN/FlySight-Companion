package fr.hozakan.flysightcompanion.sessionmodule.ui.play

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionPlayer

@Stable
data class SessionPlayerState(
    val player: SessionPlayer?
)