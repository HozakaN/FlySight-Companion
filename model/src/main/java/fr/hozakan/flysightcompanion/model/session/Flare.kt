package fr.hozakan.flysightcompanion.model.session

import fr.hozakan.flysightcompanion.model.GnssData

data class Flare(
    val flareData: List<GnssData>,
    val gain: Int, // Gain in meters
)