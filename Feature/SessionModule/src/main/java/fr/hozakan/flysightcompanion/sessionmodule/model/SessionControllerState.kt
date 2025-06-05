package fr.hozakan.flysightcompanion.sessionmodule.model

import fr.hozakan.flysightcompanion.model.session.profile.SessionProfile
import fr.hozakan.flysightcompanion.model.session.profile.SessionType

sealed interface SessionControllerState {
    data object Idle : SessionControllerState
    data class Playing(
        val sessionType: SessionType,
        val sessionProfile: SessionProfile
    ) : SessionControllerState
}