package fr.hozakan.flysightcompanion.model.config

enum class AlarmType(
    val value: Int
) {
    NoAlarm(0),
    Beep(1),
    ChirpUp(2),
    ChirpDown(3),
    PlayFile(4);

    companion object {
        fun fromValue(value: Int): AlarmType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}