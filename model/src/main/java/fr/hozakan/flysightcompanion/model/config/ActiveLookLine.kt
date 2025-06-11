package fr.hozakan.flysightcompanion.model.config

data class ActiveLookLine(
    val type: ActiveLookLineType,
    val unitSystem: UnitSystem,
    val decimal: Int
)
