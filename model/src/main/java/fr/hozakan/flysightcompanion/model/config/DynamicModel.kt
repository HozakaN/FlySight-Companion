package fr.hozakan.flysightcompanion.model.config

enum class DynamicModel(
    val value: Int
) {
    Portable(0),
    Stationary(2),
    Pedestrian(3),
    Automotive(4),
    Sea(5),
    Airborne1g(6),
    Airborne2g(7),
    Airborne4g(8);

    companion object {
        fun fromValue(value: Int): DynamicModel? {
            return entries.firstOrNull { it.value == value }
        }
    }
}