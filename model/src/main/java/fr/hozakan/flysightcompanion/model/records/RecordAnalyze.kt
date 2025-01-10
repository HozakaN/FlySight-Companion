package fr.hozakan.flysightcompanion.model.records

import fr.hozakan.flysightcompanion.model.ConfigFile

data class RecordAnalyze(
//    val recordMd5: String,
//    val configFile: ConfigFile,
    val options: AnalyzeOptions,
    val exitTime: Double,
    val dataPoints: ComputableDataPoints,
//    val sensors: List<Sensor>
)