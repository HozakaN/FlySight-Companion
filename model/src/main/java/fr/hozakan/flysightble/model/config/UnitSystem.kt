package fr.hozakan.flysightble.model.config

enum class UnitSystem(
    val value: Int,
    val unitName: String,
    val speedText: String,
    val distanceText: String
) {
    Metric(0, "Metric", "km/h", "m"),
    Imperial(1, "Imperial", "mph", "ft");

    companion object {
        fun fromUnitName(unitName: String): UnitSystem? {
            return entries.firstOrNull { it.unitName == unitName }
        }
        fun fromSpeedText(text: String): UnitSystem? {
            return entries.firstOrNull { it.speedText == text }
        }
        fun fromDistanceText(text: String): UnitSystem? {
            return entries.firstOrNull { it.distanceText == text }
        }
        fun fromValue(value: Int): UnitSystem? {
            return entries.firstOrNull { it.value == value }
        }
    }
}