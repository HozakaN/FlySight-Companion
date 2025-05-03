package fr.hozakan.flysightcompanion.sessionmodule.model

import fr.hozakan.flysightcompanion.model.session.configuration.SessionProfile

sealed interface PlayerState {
    data object Idle : PlayerState
    data class Playing(
        val sessionProfile: SessionProfile
    ) : PlayerState
}