package fr.hozakan.flysightcompanion.model.config

enum class UnitSystem(
    val value: Int
) {
    Metric(0),
    Imperial(1);

    companion object {
//        fun fromUnitName(unitName: String): UnitSystem? {
//            return entries.firstOrNull { it.unitName == unitName }
//        }
//        fun fromSpeedText(text: String): UnitSystem? {
//            return entries.firstOrNull { it.speedText == text }
//        }
//        fun fromDistanceText(text: String): UnitSystem? {
//            return entries.firstOrNull { it.distanceText == text }
//        }
        fun fromValue(value: Int): UnitSystem? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
