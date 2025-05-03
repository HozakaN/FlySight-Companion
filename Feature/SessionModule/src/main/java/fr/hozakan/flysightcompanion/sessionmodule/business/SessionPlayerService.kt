package fr.hozakan.flysightcompanion.sessionmodule.business

import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionPlayer
import fr.hozakan.flysightcompanion.sessionmodule.model.PlayerState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import kotlinx.coroutines.flow.StateFlow

interface SessionPlayerService {
    val state: StateFlow<PlayerState>
    val sessionPlayer: StateFlow<SessionPlayer?>
    suspend fun playSession(sessionProfile: SessionProfile, sessionSource: SessionSource)
    suspend fun stopSession()
}