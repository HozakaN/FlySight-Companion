package fr.hozakan.flysightcompanion.model.session.configuration

import fr.hozakan.flysightcompanion.model.records.RecordFile

sealed class SessionSource(val sessionSourceType: SessionSourceType) {
    data object Local : SessionSource(SessionSourceType.Local)
    data class FlySight(
        val fsId: String,
        val fsName: String
    ) : SessionSource(SessionSourceType.FlySight)

    data class Record(
        val file: RecordFile
    ) : SessionSource(SessionSourceType.Record)
}

enum class SessionSourceType {
    Local,
    FlySight,
    Record;

    companion object {}
}