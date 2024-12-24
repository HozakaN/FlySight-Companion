package fr.hozakan.flysightcompanion.model.config

enum class AlarmType(
    val value: Int,
    val text: String
) {
    NoAlarm(0, "No alarm"),
    Beep(1, "Beep"),
    ChirpUp(2, "Chirp up"),
    ChirpDown(3, "Chirp down"),
    PlayFile(4, "Play file");

    companion object {
        fun fromText(text: String): AlarmType? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): AlarmType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}