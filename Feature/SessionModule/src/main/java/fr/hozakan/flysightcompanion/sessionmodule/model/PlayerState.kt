package fr.hozakan.flysightcompanion.sessionmodule.model

import fr.hozakan.flysightcompanion.model.session.configuration.SessionConfiguration

sealed interface PlayerState {
    data object Idle : PlayerState
    data class Playing(
        val sessionConfiguration: SessionConfiguration
    ) : PlayerState
}