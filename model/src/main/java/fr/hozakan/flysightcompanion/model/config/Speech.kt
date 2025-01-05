package fr.hozakan.flysightcompanion.model.config

data class Speech(
    val mode: SpeechMode,
    val unit: UnitSystem,
    val value: Int
)