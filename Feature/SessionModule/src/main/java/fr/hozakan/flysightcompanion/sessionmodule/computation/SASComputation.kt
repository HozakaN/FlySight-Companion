package fr.hozakan.flysightcompanion.sessionmodule.computation

import fr.hozakan.flysightcompanion.model.ConfigFile
import fr.hozakan.flysightcompanion.model.GnssData



private val sasTable = intArrayOf(
    1024, 1077, 1135, 1197,
    1265, 1338, 1418, 1505,
    1600, 1704, 1818, 1944
)

internal fun getSpeedMultiplicator(
    config: ConfigFile,
    gnssData: GnssData
): Int = if (config.useSAS) {
    if (gnssData.hMsl < 0) {
        sasTable[0]
    } else {
        1024
    }
} else if (gnssData.hMsl >= 11534336L) {
    sasTable[11]
} else {
    val h = gnssData.hMsl / 1024
    val i = h / 1024
    val j = h.mod(1024)
    val y1 = sasTable[i]
    val y2 = sasTable[i + 1]
    y1 + ((y2 - y1) * j) / 1024
}