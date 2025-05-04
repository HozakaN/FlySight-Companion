package fr.hozakan.flysightcompanion.sessionmodule.business

import fr.hozakan.flysightcompanion.sessionmodule.business.player.SessionController
import fr.hozakan.flysightcompanion.sessionmodule.model.SessionControllerState
import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionSource
import kotlinx.coroutines.flow.StateFlow

interface SessionControllerService {
    val state: StateFlow<SessionControllerState>
    val sessionController: StateFlow<SessionController?>
    suspend fun playSession(sessionProfile: SessionProfile, sessionSource: SessionSource)
    suspend fun stopSession()
}