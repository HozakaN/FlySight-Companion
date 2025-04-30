package fr.hozakan.flysightcompanion.model.session.configuration

import fr.hozakan.flysightcompanion.model.DisplayableConfig

data class SessionConfiguration(
    override val name: String,
    val description: String,
    val sessionType: SessionType
) : DisplayableConfig {
    companion object {
        fun default() = SessionConfiguration(
            name = "Default",
            description = "",
            sessionType = SessionType.Visual
        )
    }
}
