package fr.hozakan.flysightble.model.config

sealed class AltitudeUnit(
    val value: Int,
    val text: String
) {
    data object Metric : AltitudeUnit(0, "m")
    data object Imperial : AltitudeUnit(1, "ft")
}