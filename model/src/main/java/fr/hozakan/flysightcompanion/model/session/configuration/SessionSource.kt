package fr.hozakan.flysightcompanion.model.session.configuration

sealed class SessionSource(val sessionSourceType: SessionSourceType) {
    data object Local : SessionSource(SessionSourceType.Local)
    data class FlySight(
        val fsId: String,
        val fsName: String
    ) : SessionSource(SessionSourceType.FlySight)

    data class Record(val fileName: String) : SessionSource(SessionSourceType.Record)
}

enum class SessionSourceType {
    Local,
    FlySight,
    Record;

    companion object {}
}