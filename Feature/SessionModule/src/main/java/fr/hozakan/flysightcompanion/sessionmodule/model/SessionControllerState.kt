package fr.hozakan.flysightcompanion.sessionmodule.model

import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile
import fr.hozakan.flysightcompanion.model.session.configuration.SessionType

sealed interface SessionControllerState {
    data object Idle : SessionControllerState
    data class Playing(
        val sessionType: SessionType,
        val sessionProfile: SessionProfile
    ) : SessionControllerState
}