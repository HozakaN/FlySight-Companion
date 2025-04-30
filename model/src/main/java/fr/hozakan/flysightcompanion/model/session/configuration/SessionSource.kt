package fr.hozakan.flysightcompanion.model.session.configuration

sealed class SessionSource(val staticSessionSource: StaticSessionSource) {
    data object Local : SessionSource(StaticSessionSource.Local)
    data class FlySight(val fsId: String) : SessionSource(StaticSessionSource.FlySight)
    data class File(val fileName: String) : SessionSource(StaticSessionSource.File)
}

enum class StaticSessionSource {
    Local,
    FlySight,
    File;

    companion object {}
}