package fr.hozakan.flysightble.model.config

enum class DynamicModel(
    val value: Int,
    val text: String
) {
    Portable(0, "Portable"),
    Stationary(2, "Stationary"),
    Pedestrian(3, "Pedestrian"),
    Automotive(4, "Automotive"),
    Sea(5, "Sea"),
    Airborne1g(6, "Airborne with < 1G acceleration"),
    Airborne2g(7, "Airborne with < 2G acceleration"),
    Airborne4g(8, "Airborne with < 4G acceleration");

    companion object {
        fun fromText(text: String): DynamicModel? {
            return entries.firstOrNull { it.text == text }
        }
        fun fromValue(value: Int): DynamicModel? {
            return entries.firstOrNull { it.value == value }
        }
    }
}