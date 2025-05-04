package fr.hozakan.flysightcompanion.sessionmodule.business.player

import fr.hozakan.flysightcompanion.model.config.Alarm
import fr.hozakan.flysightcompanion.model.config.Volume
import java.util.Locale

sealed class SessionEvent(
    val name: String
) {
    data class AlarmEvent(
        val alarm: Alarm
    ) : SessionEvent("AlarmEvent")

    data class PlayTextEvent(
        val text: String
    ) : SessionEvent("PlayTextEvent")

    data class PlayFileEvent(
        val fileName: String
    ) : SessionEvent("PlayFileEvent")
}