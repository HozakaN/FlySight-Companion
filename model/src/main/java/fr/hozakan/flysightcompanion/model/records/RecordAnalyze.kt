package fr.hozakan.flysightcompanion.model.records

import fr.hozakan.flysightcompanion.model.ConfigFile

sealed interface RecordAnalyze {
    //    val recordMd5: String,
//    val configFile: ConfigFile,
    val options: AnalyzeOptions
    val exitTime: Double
    val dataPoints: ComputableDataPoints
//    val sensors: List<Sensor>

    data class Error(val message: String) : RecordAnalyze {
        override val options: AnalyzeOptions = AnalyzeOptions()
        override val exitTime: Double = 0.0
        override val dataPoints: ComputableDataPoints = emptyList()
    }

    data class Success(
        override val options: AnalyzeOptions,
        override val exitTime: Double,
        override val dataPoints: ComputableDataPoints
    ) : RecordAnalyze

    companion object {
        fun success(
            options: AnalyzeOptions,
            exitTime: Double,
            dataPoints: ComputableDataPoints
        ) : RecordAnalyze {
            return Success(options, exitTime, dataPoints)
        }

        fun error(message: String): RecordAnalyze {
            return Error(message)
        }
    }
}

val dummyAnalyze = RecordAnalyze.success(AnalyzeOptions(), 0.0, emptyList())
