package fr.hozakan.flysightcompanion.sessionmodule.model

import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile

sealed interface SessionControllerState {
    data object Idle : SessionControllerState
    data class Playing(
        val sessionProfile: SessionProfile
    ) : SessionControllerState
}