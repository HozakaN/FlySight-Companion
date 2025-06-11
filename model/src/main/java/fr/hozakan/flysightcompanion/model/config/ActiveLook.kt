package fr.hozakan.flysightcompanion.model.config

data class ActiveLook(
    val deviceId: String,
    val mode: ActiveLookMode,
    val rate: Int,
    val lines: List<ActiveLookLine>
)